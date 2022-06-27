package group.yzhs.alarm.service;


import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import group.yzhs.alarm.config.CollectorConfig;
import group.yzhs.alarm.config.WXPushConfig;
import group.yzhs.alarm.constant.SysConfigEnum;
import group.yzhs.alarm.factory.RuleCacheFacyory;
import group.yzhs.alarm.mapper.impl.*;
import group.yzhs.alarm.model.ProductionLine;
import group.yzhs.alarm.model.dto.iot.IotDcsReadDto;
import group.yzhs.alarm.model.dto.iot.IotReadNodeInfo;
import group.yzhs.alarm.model.dto.iot.IotResponse;
import group.yzhs.alarm.model.entity.*;
import group.yzhs.alarm.model.rule.BaseRule;
import group.yzhs.alarm.model.rule.trigger.TriggerRule;
import group.yzhs.alarm.service.alarmHandle.Handler;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/6/15 15:00
 */
@Service
@Data
@Slf4j
public class JudgementService {

    private String errormessage = "";


    @Autowired
    private List<Handler> handleList;


    @Autowired
    @Qualifier("app-thread")
    private ExecutorService executorService;

    @Autowired
    private WXPushConfig wxPushConfig;

    @Autowired
    private RestTemplate restTemplate;


    @Autowired
    private PointMapperImp pointMapperImp;
    @Autowired
    private AlarmRuleSwitchMapMapperImp alarmRuleSwitchMapMapperImp;
    @Autowired
    private SwitchMapperImp switchMapperImp;
    @Autowired
    private SwitchRuleMapperImp switchRuleMapperImp;
    @Autowired
    private AlarmRuleMapperImp alarmRuleMapperImp;
    @Autowired
    private SystemConfigMapperImp systemConfigMapperImp;
    /*报警point缓存*/
    Map<Long, BaseRule> ruleCaches = new ConcurrentHashMap<>();


    private Map<String, Handler> handlerPool = new HashMap<>();


    private Map<String, List<BaseRule>> rules = null;

    private CollectorConfig config = null;

    public JudgementService(CollectorConfig config) {
        this.config = config;
    }


    public void judge() {

        /*
        //查询点位
        * 1获取iot实时数据
        * 2判断每个数据点的是否需要判断
        * 3进行判断处理
        * */
        /*查询数据点*/
        List<Point> pointList = pointMapperImp.list().stream().filter(point -> !StringUtils.isEmpty(point.getTag())).collect(Collectors.toList());
        IotDcsReadDto iotDcsReadDto = new IotDcsReadDto();
        iotDcsReadDto.setSample(1);
        List<IotReadNodeInfo> points = new ArrayList<>();
        if (!CollectionUtils.isEmpty(pointList)) {
            pointList.forEach(p -> {
                IotReadNodeInfo point = IotReadNodeInfo.builder()
                        .measurePoint(p.getTag())
                        .node(p.getNodeCode())
                        .build();
                points.add(point);
            });
        } else {
            log.warn("have no any rules");
            return;
        }

        iotDcsReadDto.setPoints(points);
        //iot 数据获取
        SystemConfig iotConfig = systemConfigMapperImp.getOne(Wrappers.<SystemConfig>lambdaQuery().eq(SystemConfig::getCode, SysConfigEnum.SYS_CONFIG_ioturl.getCode()));
        ResponseEntity<IotResponse> iotResponse = restTemplate.postForEntity(iotConfig.getValue() + config.getIotread(), iotDcsReadDto, IotResponse.class);
        if (iotResponse.getStatusCode().equals(HttpStatus.OK)) {
            IotResponse iotResponseBody = iotResponse.getBody();
            if (iotResponseBody.getStatus().equals(HttpStatus.OK.value())) {
                if (!CollectionUtils.isEmpty(iotResponseBody.getData())) {
                    //key=<nodecode=tag>
                    Map<String, Double> lasterDataFromIot = iotResponseBody.getData().stream().parallel().collect(Collectors.toMap(p -> p.getNode() + "=" + p.getMeasurePoint(), p -> p.getData().get(0).getValue(), (o, n) -> n));
                    List<AlarmRule> ruleslist = alarmRuleMapperImp.list();
                    //计算哪些报警规则已经移除，不需报警了
                    List<Long> distinctRules = new ArrayList<>();
                    distinctRules.addAll(ruleCaches.keySet());
                    distinctRules.removeAll(ruleslist.stream().map(BaseEntity::getId).distinct().collect(Collectors.toList()));
                    //移除掉数据库删除的报警
                    distinctRules.forEach(d -> ruleCaches.remove(d));
                    //添加新的报警规则设置
                    List<Long> needRemoveRuleCache = new ArrayList<>();
                    ruleslist.parallelStream().forEach(r -> {
                        //新的报警规则
                        if (!ruleCaches.containsKey(r.getId())) {
                            ruleCaches.put(r.getId(), RuleCacheFacyory.create(r));
                            log.debug("add id={},temple={}", r.getId(), r.getAlarmTemple());
                            //log.debug("insert a new rule");
                        } else {

                            BaseRule baseRule = ruleCaches.get(r.getId());
                            //原先的报警设置的类型和当前数据库的是否匹配
                            if(baseRule.getAlarmMode().equals(r.getAlarmMode()) && baseRule.getAlarmSubMode().equals(r.getAlarmSubMode())){
                                //更新设置
                                BeanUtils.copyProperties(r, baseRule);
                            }else{
                                //重新创建放入
                                ruleCaches.put(r.getId(),RuleCacheFacyory.create(r));
                            }
                        }
                        BaseRule baseRule = ruleCaches.get(r.getId());
                        //更新关联的点位信息
                        Point point = pointMapperImp.getById(r.getPointId());
                        if (ObjectUtils.isNotEmpty(point)) {
                            baseRule.setIotNode(point.getNodeCode());
                            baseRule.setTag(point.getTag());
                        } else {
                            /**如果点位都不存在的，那么直接移除规则*/
                            log.debug("remove id={},temple={}", r.getId(), r.getAlarmTemple());
                            needRemoveRuleCache.add(r.getId());
                        }

                    });
                    //remove its
                    needRemoveRuleCache.forEach(id -> ruleCaches.remove(id));

                    StringBuilder tempError = new StringBuilder();
                    ruleCaches.values()/*.stream().filter(r->r.getTag().equals("AI5721P02")).parallel()*/.forEach(rule -> {
                        /*检查是否需要报警*/
                        //开关规则查询
                        boolean switchClose = true;
                        //查询改规则下的所有开关
                        List<AlarmRuleSwitchMap> ruleSwitchMapList = alarmRuleSwitchMapMapperImp.list(Wrappers.<AlarmRuleSwitchMap>lambdaQuery().eq(AlarmRuleSwitchMap::getRefAlarmRuleId, rule.getId()));

                        if (!CollectionUtils.isEmpty(ruleSwitchMapList)) {
                            //如果存在开关，那么就判断所有的开关是否全部都已经打开
                            //规则开关映射，key=switchId
                            Map<Long, AlarmRuleSwitchMap> alarmRuleSwitchMap = ruleSwitchMapList.stream().collect(Collectors.toMap(AlarmRuleSwitchMap::getRefSwitchId, a -> a, (o, n) -> n));
                            //查询开关对应规则
                            //查询所有规则对应的switch
                            List<SwitchRule> switchRuleList = switchRuleMapperImp.list(Wrappers.<SwitchRule>lambdaQuery().in(SwitchRule::getRefSwitchId, alarmRuleSwitchMap.keySet()));
                            if (!CollectionUtils.isEmpty(switchRuleList)) {
                                //检查规则是否都符合
                                switchClose = switchRuleList.stream().allMatch(sr -> {
                                    //开关判断点位获取
                                    Point point = pointMapperImp.getById(sr.getPointId());
                                    if (ObjectUtils.isEmpty(point)) {
                                        Switch aSwitch = switchMapperImp.getById(sr.getRefSwitchId());
                                        log.error("开关{}规则位号查找不到id={}", aSwitch.getName(), sr.getPointId());
                                        tempError.append(String.format("开关%s规则位号查找不到id=%d\n", aSwitch.getName(), sr.getPointId()));
                                        return true;
                                    } else {
                                        Double lastValue = lasterDataFromIot.get(point.getNodeCode() + "=" + point.getTag());

                                        if (ObjectUtils.isNotEmpty(lastValue)) {
                                            switch (sr.getRuleCode()) {
                                                case SWITCH_RULE_EQ: {
                                                    if (lastValue == sr.getLimitValue().doubleValue()) {
                                                        return true;
                                                    } else {
                                                        return false;
                                                    }
                                                }
                                                case SWITCH_RULE_GT: {
                                                    if (lastValue > sr.getLimitValue().doubleValue()) {
                                                        return true;
                                                    } else {
                                                        return false;
                                                    }
                                                }
                                                case SWITCH_RULE_LT: {
                                                    if (lastValue < sr.getLimitValue().doubleValue()) {
                                                        return true;
                                                    } else {
                                                        return false;
                                                    }

                                                }
                                                case SWITCH_RULE_LE: {
                                                    if (lastValue <= sr.getLimitValue().doubleValue()) {
                                                        return true;
                                                    } else {
                                                        return false;
                                                    }
                                                }
                                                case SWITCH_RULE_GE: {
                                                    if (lastValue >= sr.getLimitValue().doubleValue()) {
                                                        return true;
                                                    } else {
                                                        return false;
                                                    }
                                                }
                                                default: {
                                                    //默认需要报警
                                                    return true;
                                                }
                                            }

                                        } else {
                                            Switch aSwitch = switchMapperImp.getById(sr.getRefSwitchId());
                                            log.error("开关{}的规则位号数据无法获取={}", ObjectUtils.isNotEmpty(aSwitch) ? aSwitch.getName() : "名称未知", point.getNodeCode() + "=" + point.getTag());
                                            tempError.append(String.format("开关%s的规则位号%s数据无法获取\n", ObjectUtils.isNotEmpty(aSwitch) ? aSwitch.getName() : "名称未知", point.getNodeCode() + "=" + point.getTag()));
                                            return true;

                                        }
                                    }

                                });
                            }
                        }

                        //开关已经闭合，那么现在可以进行数据判断了
                        Point point = pointMapperImp.getById(rule.getPointId());
                        if (ObjectUtils.isNotEmpty(point)) {
                            Double latestValue = lasterDataFromIot.get(point.getNodeCode() + "=" + point.getTag());
                            if (ObjectUtils.isNotEmpty(latestValue)) {
                                rule.setValue(latestValue);
                                if (getHandlerPool().containsKey(rule.getAlarmMode())) {
                                    getHandlerPool().get(rule.getAlarmMode()).handle(rule, switchClose);
                                }

                            }

                        }

                    });
                    errormessage = tempError.toString();


                } else {
                    log.error("iot 没有接受到iot任何数据，请检查位号和节点编码");
                    errormessage = "没有接受到iot任何数据，请检查位号和节点编码";
                    return;
                }

            } else {
                log.error("iot 数据获取异常={}", iotResponseBody.getMessage());
                errormessage = iotResponseBody.getMessage();
            }

        } else {
            log.error("iot 数据获取异常,请检查网络");
            return;
        }


    }


    public synchronized Map<String, Handler> getHandlerPool() {
        if (CollectionUtils.isEmpty(handlerPool)) {
            if (!CollectionUtils.isEmpty(handleList)) {
                handlerPool = handleList.stream().collect(Collectors.toMap(h -> h.getCode(), h -> h, (o, n) -> n));
            }
        }
        return handlerPool;
    }


}
