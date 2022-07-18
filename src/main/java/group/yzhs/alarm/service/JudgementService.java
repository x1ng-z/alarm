package group.yzhs.alarm.service;


import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import group.yzhs.alarm.config.CollectorConfig;
import group.yzhs.alarm.config.WXPushConfig;
import group.yzhs.alarm.factory.RuleCacheFacyory;
import group.yzhs.alarm.listener.SessionListener;
import group.yzhs.alarm.mapper.impl.*;
import group.yzhs.alarm.model.dto.iot.IotMeasurePointValueDto;
import group.yzhs.alarm.model.entity.*;
import group.yzhs.alarm.model.rule.BaseRule;
import group.yzhs.alarm.service.alarmHandle.Handler;
import group.yzhs.alarm.service.iot.IotPlantService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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

    @Autowired
    private SessionListener sessionListener;

    @Autowired
    private IotPlantService iotPlantService;

    public static final String DELIMITER = "=";

    /**
     * 报警point缓存
     */
    Map<Long, BaseRule> ruleCaches = new ConcurrentHashMap<>();


    private Map<String, Handler> handlerPool = new HashMap<>();


    private Map<String, List<BaseRule>> rules = null;

    private CollectorConfig config = null;

    public JudgementService(CollectorConfig config) {
        this.config = config;
    }


    public void judge() {

        /*查询点位
         * 1获取iot实时数据
         * 2判断每个数据点的是否需要判断
         * 3进行判断处理
         * */

        /**
         * 查询数据点
         * */
        List<IotMeasurePointValueDto> iotLatestDatas = iotPlantService.getLatest();
        if (!CollectionUtils.isEmpty(iotLatestDatas)) {
            //key=<nodecode=tag>
            Map<String, Double> lasterDataFromIot = iotLatestDatas.stream().parallel().collect(Collectors.toMap(p -> p.getNode() + DELIMITER + p.getMeasurePoint(), p -> p.getData().get(0).getValue(), (o, n) -> n));
            //移除不需要报警的数据
            removeAbolishedAlarmRelu();
            //报警数据异常消息
            StringBuilder tempError = new StringBuilder();
            //
            ruleCaches.values()/*.stream().filter(r->r.getTag().equals("AI5721P02")).parallel()*/.forEach(rule -> {
                /*检查是否需要报警*/
                //开关规则查询
                boolean switchClose = isSwitchTurnOn(rule, lasterDataFromIot, tempError);
                Point point = pointMapperImp.getById(rule.getPointId());
                Double latestValue = lasterDataFromIot.get(point.getNodeCode() + DELIMITER + point.getTag());
                if (switchClose && ObjectUtils.isNotEmpty(point) && ObjectUtils.isNotEmpty(latestValue)) {
                    //开关已经闭合，那么现在可以进行数据判断了
                    //最新值
                    rule.setValue(latestValue);
                    if (getHandlerPool().containsKey(rule.getAlarmMode())) {
                        getHandlerPool().get(rule.getAlarmMode()).handle(rule);
                    }
                }
            });
            errormessage = tempError.toString();
        }

    }


    /**
     * 校验语音报警开关是否打开
     *
     * @param rule              规则
     * @param lasterDataFromIot iot最新数据
     * @param tempError         异常错误输出容器
     */
    private boolean isSwitchTurnOn(BaseRule rule, Map<String, Double> lasterDataFromIot, StringBuilder tempError) {
        boolean switchTurnOn = true;
        //查询改规则下的所有开关
        List<AlarmRuleSwitchMap> ruleSwitchMapList = alarmRuleSwitchMapMapperImp.list(Wrappers.<AlarmRuleSwitchMap>lambdaQuery().eq(AlarmRuleSwitchMap::getRefAlarmRuleId, rule.getId()));

        if (!CollectionUtils.isEmpty(ruleSwitchMapList)) {
            //如果存在开关，那么就判断所有的开关是否全部都已经打开
            //规则开关映射，key=switchId  null
            Map<Long, AlarmRuleSwitchMap> alarmRuleSwitchMap = ruleSwitchMapList.stream().collect(Collectors.toMap(AlarmRuleSwitchMap::getRefSwitchId, a -> a, (o, n) -> n));
            //查询开关对应规则
            //查询所有规则对应的switch
            for (Map.Entry<Long, AlarmRuleSwitchMap> alarmRuleSwitchMapEntry : alarmRuleSwitchMap.entrySet()) {
                Switch aSwitch = switchMapperImp.getById(alarmRuleSwitchMapEntry.getValue().getRefSwitchId());
                List<SwitchRule> switchRuleList = switchRuleMapperImp.list(Wrappers.<SwitchRule>lambdaQuery().eq(SwitchRule::getRefSwitchId, alarmRuleSwitchMapEntry.getValue().getRefSwitchId()));
                if (!CollectionUtils.isEmpty(switchRuleList)) {
                    //检查规则是否都符合
                    boolean switchLogic = true;
                    switch (aSwitch.getSwitchLogic()) {
                        case SWITCH_LOGIC_OR: {
                            switchLogic = switchRuleList.stream().anyMatch(sr -> switchLogic(sr, aSwitch, lasterDataFromIot, tempError));
                            break;
                        }
                        case SWITCH_LOGIC_AND: {
                            switchLogic = switchRuleList.stream().allMatch(sr -> switchLogic(sr, aSwitch, lasterDataFromIot, tempError));
                            break;
                        }
                        default: {
                            //do no thing
                        }
                    }
                    switchTurnOn = switchTurnOn && switchLogic;
                }
            }
        }

        return switchTurnOn;
    }


    private boolean switchLogic(SwitchRule sr, Switch aSwitch, Map<String, Double> lasterDataFromIot, StringBuilder tempError) {
        //开关判断的数据点位获取
        Point point = pointMapperImp.getById(sr.getPointId());
        if (ObjectUtils.isEmpty(point)) {
            //以下操作只是为了记录下逻辑
            if (ObjectUtils.isNotEmpty(aSwitch)) {
                log.error("开关{}规则位号查找不到id={}", aSwitch.getName(), sr.getPointId());
                tempError.append(String.format("开关%s规则位号查找不到id=%d\n", aSwitch.getName(), sr.getPointId()));
            }
            return true;
        } else {
            //取数iot最新值
            Double lastValue = lasterDataFromIot.get(point.getNodeCode() + DELIMITER + point.getTag());
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
                if (ObjectUtils.isNotEmpty(aSwitch)) {
                    log.error("开关{}的规则位号数据无法获取={}", ObjectUtils.isNotEmpty(aSwitch) ? aSwitch.getName() : "名称未知", point.getNodeCode() + "=" + point.getTag());
                    tempError.append(String.format("开关%s的规则位号%s数据无法获取\n", ObjectUtils.isNotEmpty(aSwitch) ? aSwitch.getName() : "名称未知", point.getNodeCode() + "=" + point.getTag()));
                }
                return true;
            }
        }
    }

    private void removeAbolishedAlarmRelu() {
        //出现目前设置的报警规则
        List<AlarmRule> allSetRules = alarmRuleMapperImp.list();
        //计算哪些报警规则已经移除，不需报警了
        List<Long> abolishedAlarmRelus = new ArrayList<>();
        abolishedAlarmRelus.addAll(ruleCaches.keySet());
        abolishedAlarmRelus.removeAll(allSetRules.stream().map(BaseEntity::getId).distinct().collect(Collectors.toList()));
        //移除掉数据库废除的报警规则
        abolishedAlarmRelus.forEach(d ->
                {
                    BaseRule abolishRule=ruleCaches.remove(d);
                    //清除会话中的报警内容
                    sessionListener.removeAbolishContext(abolishRule);
                    log.debug("alarm-mode={},sub-mode={},temple={}",abolishRule.getAlarmMode(),abolishRule.getAlarmSubMode(),abolishRule.getAlarmTemple());
                }
        );
        //添加新的报警规则设置
        List<Long> needRemoveRuleCache = new ArrayList<>();

        allSetRules.forEach(newestRule -> {
            //新的报警规则
            BaseRule newestBaseRule = RuleCacheFacyory.create(newestRule);

            if (!ruleCaches.containsKey(newestRule.getId())) {
                ruleCaches.put(newestRule.getId(), newestBaseRule);
                log.debug("add id={},temple={}", newestRule.getId(), newestRule.getAlarmTemple());
                update(newestBaseRule, needRemoveRuleCache);
            } else {
                //已经存在的规则，但内容被修改
                BaseRule cacheBaseRule = ruleCaches.get(newestRule.getId());
                //已经存在的，但是设置和原来的不一样
                if (!cacheBaseRule.equals(newestBaseRule)) {
                    //删除变换的报警规则，会话中剩余的报警内容
                    log.debug("update relu");
                    sessionListener.removeAbolishContext(cacheBaseRule);
                    //更新设置
                    BeanUtils.copyProperties(newestBaseRule, cacheBaseRule);
                    update(cacheBaseRule, needRemoveRuleCache);

                }
            }
        });
        //remove its 如果配置的点位被删除了
        needRemoveRuleCache.forEach(id -> {
            BaseRule baseRule = ruleCaches.remove(id);
            sessionListener.removeAbolishContext(baseRule);
        });
    }

    /**
     * 更新报警规则
     */
    private void update(BaseRule newestRule, List<Long> needRemoveRuleCache) {
        BaseRule baseRule = ruleCaches.get(newestRule.getId());
        //更新关联的点位信息
        Point point = pointMapperImp.getById(newestRule.getPointId());
        if (ObjectUtils.isNotEmpty(point)) {
            baseRule.setIotNode(point.getNodeCode());
            baseRule.setTag(point.getTag());
        } else {
            /**如果点位都不存在的，那么直接移除规则*/
            log.debug("remove id={},temple={}", newestRule.getId(), newestRule.getAlarmTemple());
            needRemoveRuleCache.add(newestRule.getId());
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
