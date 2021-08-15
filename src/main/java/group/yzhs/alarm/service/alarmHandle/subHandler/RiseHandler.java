package group.yzhs.alarm.service.alarmHandle.subHandler;

import group.yzhs.alarm.config.WXPushConfig;
import group.yzhs.alarm.constant.SessionContextEnum;
import group.yzhs.alarm.constant.TrigerModelEnum;
import group.yzhs.alarm.listener.SessionListener;
import group.yzhs.alarm.model.AlarmMessage;
import group.yzhs.alarm.model.rule.BaseRule;
import group.yzhs.alarm.model.rule.trigger.TriggerRule;
import group.yzhs.alarm.service.alarmHandle.SubHandler;
import group.yzhs.alarm.utils.WXPushTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/8/13 13:10
 */
@Service
@Slf4j
public class RiseHandler implements SubHandler {
    @Autowired
    private SessionListener sessionListener;

    @Autowired
    private WXPushConfig wxPushConfig;


    @Override
    public String getCode() {
        return TrigerModelEnum.TRIGER_RISE.getCode();
    }

    @Override
    public boolean judge(BaseRule rule) {
        TriggerRule triggerRule = (TriggerRule) rule;
        return (triggerRule.getValue() != 0) && (triggerRule.getLastvalue() == 0);
    }

    @Override
    public void handle(BaseRule rule) {
        TriggerRule triggerRule = (TriggerRule) rule;
        //上升沿
        if (judge(triggerRule)) {
            alarmHandle(triggerRule);
        } else {
            noAlarmHandle(triggerRule);
        }
    }

    private void alarmHandle(TriggerRule triggerRule) {
        //现在达到报警状态了
        if (triggerRule.getProduct().getDisplay()) {
            sessionListener.getHttpSessionMap().values().forEach(s -> {
                Map<String, AlarmMessage> alarmMap = (Map<String, AlarmMessage>) s.getAttribute(SessionContextEnum.SESSIONCONTEXT_ALARMLIST.getCode());
                AlarmMessage alarmMessage = AlarmMessage.builder()
                        .context(triggerRule.getPushAudioContext())
                        .date(new Date())
                        .level(0L)
                        .product(triggerRule.getProduct().getCode())
                        .rate(0.0)
                        .value(triggerRule.getValue())
                        .build();
                alarmMap.put(triggerRule.getTag(), alarmMessage);
            });
        }


        //之前不报警，新出现的报警是需要判断是否需要语音或微信推送的
        if (!triggerRule.getIsAlarm().get()) {
            log.debug("context:{}", triggerRule.getPushWXContext());
            //微信推送
            if (triggerRule.isPushWX() || (triggerRule.isAudio())) {
                //微信推送
                if (triggerRule.isPushWX()) {
                    if (ObjectUtils.isEmpty(triggerRule.getPushWXLastTime()) || triggerRule.getPushWXLastTime().plus(wxPushConfig.getPushIntervalSec(), ChronoUnit.SECONDS).isBefore(LocalDateTime.now())) {
                        WXPushTools.sendwx(wxPushConfig.getUrl(), triggerRule.getPushWXContext(), wxPushConfig.getDepartment());
                        triggerRule.setPushWXLastTime(LocalDateTime.now());
                    }
                }
                //语音报警
                if (triggerRule.isAudio()) {
                    if (!CollectionUtils.isEmpty(sessionListener.getHttpSessionMap())) {
                        sessionListener.getHttpSessionMap().values().forEach(s -> {
                            Map<String, LocalDateTime> sessionAudioTime = (Map<String, LocalDateTime>) s.getAttribute(SessionContextEnum.SESSIONCONTEXT_AUDIOPUSHLASTTIME.getCode());
                            if (ObjectUtils.isEmpty(sessionAudioTime.get(triggerRule.getTag())) || sessionAudioTime.get(triggerRule.getTag()).plus(wxPushConfig.getPushIntervalSec(), ChronoUnit.SECONDS).isBefore(LocalDateTime.now())) {
                                Map<String, AlarmMessage> audioAlarmMap = (Map<String, AlarmMessage>) s.getAttribute(SessionContextEnum.SESSIONCONTEXT_AUDIOLIST.getCode());
                                synchronized (audioAlarmMap) {
                                    AlarmMessage alarmMessage = AlarmMessage.builder()
                                            .context(triggerRule.getPushAudioContext())
                                            .date(new Date())
                                            .level(0L)
                                            .product(triggerRule.getProduct().getCode())
                                            .rate(0.0)
                                            .value(triggerRule.getValue())
                                            .build();
                                    audioAlarmMap.put(triggerRule.getTag(), alarmMessage);
                                }
                                //update
                                sessionAudioTime.put(triggerRule.getTag(), LocalDateTime.now());
                            }
                        });
                    }
                }
            }

            triggerRule.getIsAlarm().set(true);
        }

    }

    private void noAlarmHandle(TriggerRule triggerRule) {
        //不报警，那么需要进行web报警画面消除
        //现在达到报警状态了
        if (triggerRule.getProduct().getDisplay()) {
            sessionListener.getHttpSessionMap().values().forEach(s -> {
                Map<String, AlarmMessage> AlarmMap = (Map<String, AlarmMessage>) s.getAttribute(SessionContextEnum.SESSIONCONTEXT_ALARMLIST.getCode());
                AlarmMap.remove(triggerRule.getTag());
            });
        }
        triggerRule.getIsAlarm().set(false);
    }
}
