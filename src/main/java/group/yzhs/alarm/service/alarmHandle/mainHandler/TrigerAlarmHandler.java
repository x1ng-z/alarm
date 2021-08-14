package group.yzhs.alarm.service.alarmHandle.mainHandler;

import group.yzhs.alarm.config.WXPushConfig;
import group.yzhs.alarm.constant.AlarmModelEnum;
import group.yzhs.alarm.constant.RepacleContextEnum;
import group.yzhs.alarm.constant.SessionContextEnum;
import group.yzhs.alarm.constant.TrigerModelEnum;
import group.yzhs.alarm.listener.SessionListener;
import group.yzhs.alarm.model.AlarmMessage;
import group.yzhs.alarm.model.rule.BaseRule;
import group.yzhs.alarm.model.rule.trigger.TriggerRule;
import group.yzhs.alarm.service.alarmHandle.Handler;
import group.yzhs.alarm.service.alarmHandle.SubHandler;
import group.yzhs.alarm.utils.WXPushTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/6/15 14:27
 */
@Service
@Slf4j
public class TrigerAlarmHandler implements Handler {
    @Autowired
    private SessionListener sessionListener;

    @Autowired
    private WXPushConfig wxPushConfig;

    @Autowired
    private List<SubHandler> subHandlerList;

    private Map<String, SubHandler> handlerPool = new HashMap<>();




    @Override
    public String getCode() {
        return AlarmModelEnum.ALARMMODEL_TRIG.getCode();
    }

    @Override
    public void handle(BaseRule rule) {

        synchronized (rule) {
            TriggerRule triggerRule = (TriggerRule) rule;

            //推送内容替换为模板内容
            triggerRule.setPushWXContext(triggerRule.getTemplate());
            triggerRule.setPushAudioContext(triggerRule.getTemplate());
            //内容替换
            Arrays.stream(RepacleContextEnum.values()).forEach(rp -> {
                rp.replacePlaceholderContext(triggerRule);
                rp.removePlaceholderContext(triggerRule);
            });

            if (ObjectUtils.isEmpty(triggerRule.getLastvalue())) {
                triggerRule.setLastvalue(triggerRule.getValue());
            }

            SubHandler subHandler=getHandlerPool().get(triggerRule.getSubModel());
            if(!ObjectUtils.isEmpty(subHandler)){
                subHandler.handle(triggerRule);
            }
            //更新上一次的值
            triggerRule.setLastvalue(triggerRule.getValue());

        }


    }


    public synchronized Map<String, SubHandler> getHandlerPool() {
        if (CollectionUtils.isEmpty(handlerPool)) {
            if (!CollectionUtils.isEmpty(subHandlerList)) {
                handlerPool = subHandlerList.stream().collect(Collectors.toMap(h -> h.getCode(), h -> h, (o, n) -> n));
            }
        }
        return handlerPool;
    }
}
