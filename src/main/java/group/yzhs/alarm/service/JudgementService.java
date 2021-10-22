package group.yzhs.alarm.service;


import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import group.yzhs.alarm.config.CollectorConfig;
import group.yzhs.alarm.config.WXPushConfig;
import group.yzhs.alarm.factory.RuleCacheFacyory;
import group.yzhs.alarm.mapper.impl.*;
import group.yzhs.alarm.model.ProductionLine;
import group.yzhs.alarm.model.dto.iot.IotDcsReadDto;
import group.yzhs.alarm.model.dto.iot.IotReadNodeInfo;
import group.yzhs.alarm.model.dto.iot.IotResponse;
import group.yzhs.alarm.model.entity.*;
import group.yzhs.alarm.model.rule.BaseRule;
import group.yzhs.alarm.service.alarmHandle.Handler;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
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

    /*报警point缓存*/
    Map<Long, BaseRule> ruleCaches = new ConcurrentHashMap<>();


    private Map<String, Handler> handlerPool = new HashMap<>();


    private Map<String, List<BaseRule>> rules = null;

    private CollectorConfig config = null;

    public JudgementService(CollectorConfig config) {
        this.config = config;
    }



    @Scheduled(fixedRate = 1000 * 5, initialDelay = 1000 * 10)
    public void run() {
        try {
            judge();
        } catch (Exception e) {
            log.error(e.getMessage(),e);
        }
    }


    public void judge() {


        /*
        //查询点位
        * 1获取iot实时数据
        * 2判断每个数据点的是否需要判断
        * 3进行判断处理
        * */
        /*查询数据点*/
        List<Point> pointList = pointMapperImp.list();
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

        ResponseEntity<IotResponse> iotResponse = restTemplate.postForEntity(config.getIoturl() + config.getIotread(), iotDcsReadDto, IotResponse.class);
        if (iotResponse.getStatusCode().equals(HttpStatus.OK)) {
            IotResponse iotResponseBody = iotResponse.getBody();
            if (iotResponseBody.getStatus().equals(HttpStatus.OK.value())) {
                if (!CollectionUtils.isEmpty(iotResponseBody.getData())) {
                    //key=<nodecode=tag>
                    Map<String, Double> lastDataFromIot = iotResponseBody.getData().stream().parallel().collect(Collectors.toMap(p -> p.getNode() + "=" + p.getMeasurePoint(), p -> p.getData().get(0).getValue(), (o, n) -> n));
                    List<AlarmRule> ruleslist = alarmRuleMapperImp.list();
                    //计算哪些报警规则已经移除，不需报警了
                    List<Long> distinctRules=new ArrayList<>();
                    distinctRules.addAll(ruleCaches.keySet());
                    distinctRules.removeAll(ruleslist.stream().map(BaseEntity::getId).distinct().collect(Collectors.toList()));
                    distinctRules.forEach(d->ruleCaches.remove(d));
                    //更新会添加报警规则设置
                    ruleslist.stream().parallel().forEach(r -> {
                        if (!ruleCaches.containsKey(r.getId())) {
                            ruleCaches.put(r.getId(), RuleCacheFacyory.create(r));
                        }else{
                            BaseRule baseRule=ruleCaches.get(r.getId());
                            //update set
                            BeanUtils.copyProperties(r,baseRule);
                        }
                        BaseRule baseRule=ruleCaches.get(r.getId());
                        //更新关联的点位信息
                        Point point =pointMapperImp.getById(r.getPointId());
                        baseRule.setIotNode(baseRule.getIotNode());
                        baseRule.setTag(point.getTag());

                    });

                    StringBuilder tempError = new StringBuilder();
                    ruleCaches.values().stream().parallel().forEach(rule -> {
                        /*检查是否需要报警*/
                        //开关规则查询
                        List<AlarmRuleSwitchMap> ruleSwitchMapList = alarmRuleSwitchMapMapperImp.list(Wrappers.<AlarmRuleSwitchMap>lambdaQuery().eq(AlarmRuleSwitchMap::getRefAlarmRuleId, rule.getId()));
                        if (!CollectionUtils.isEmpty(ruleSwitchMapList)) {
                            Map<Long, AlarmRuleSwitchMap> alarmRuleSwitchMap = ruleSwitchMapList.stream().collect(Collectors.toMap(AlarmRuleSwitchMap::getRefSwitchId, a -> a, (o, n) -> n));
                            //查询开关对应规则
                            List<SwitchRule> switchRuleList = switchRuleMapperImp.list(Wrappers.<SwitchRule>lambdaQuery().in(SwitchRule::getId, alarmRuleSwitchMap.keySet()));
                            if (!CollectionUtils.isEmpty(switchRuleList)) {
                                //检查规则是否都符合
                                boolean switchClose = switchRuleList.stream().allMatch(sr -> {
                                    //开关判断点位获取
                                    Point point = pointMapperImp.getById(sr.getPointId());
                                    if (ObjectUtils.isNotEmpty(point)) {
                                        Double lastValue = lastDataFromIot.get(point.getNodeCode() + "=" + point.getTag());
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
                                    } else {
                                        Switch aSwitch = switchMapperImp.getById(sr.getRefSwitchId());
                                        log.error("开关{}规则位号查找不到id={}", aSwitch.getName(), sr.getPointId());
                                        tempError.append(String.format("开关%s规则位号查找不到id=%d\n", aSwitch.getName(), sr.getPointId()));
                                        return true;
                                    }

                                });
                                if (switchClose) {
                                    //开关已经闭合，那么现在可以进行数据判断了
                                    Point point = pointMapperImp.getById(rule.getPointId());
                                    if(ObjectUtils.isNotEmpty(point)){
                                        Double lastValue = lastDataFromIot.get(point.getNodeCode() + "=" + point.getTag());
                                        if(ObjectUtils.isNotEmpty(lastValue)){
                                            rule.setValue(lastValue);
                                            getHandlerPool().get(rule.getAlarmMode()).handle(rule);
                                        }

                                    }


                                }
                            }
                        }

                    });

                    ;


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
