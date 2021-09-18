package group.yzhs.alarm.service.alarmHandle.subHandler;

import group.yzhs.alarm.config.WXPushConfig;
import group.yzhs.alarm.constant.LimiteModelEnum;
import group.yzhs.alarm.constant.SessionContextEnum;
import group.yzhs.alarm.listener.SessionListener;
import group.yzhs.alarm.model.AlarmMessage;
import group.yzhs.alarm.model.rule.BaseRule;
import group.yzhs.alarm.model.rule.limit.LimitRule;
import group.yzhs.alarm.service.alarmHandle.SubHandler;
import group.yzhs.alarm.utils.WXPushTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/8/13 13:15
 */
@Service
@Slf4j
public class LimitHighHandler implements SubHandler {
    @Autowired
    private SessionListener sessionListener;

    @Autowired
    private WXPushConfig wxPushConfig;

    @Override
    public String getCode() {
        return LimiteModelEnum.LIMIT_HIGH.getCode();
    }

    @Override
    public boolean judge(BaseRule rule) {
        LimitRule limitRule = (LimitRule) rule;
        return (limitRule.getValue() > limitRule.getLimitValue());
    }

    @Override
    public void handle(BaseRule rule) {
        LimitRule limitRule = (LimitRule) rule;
        if (judge(rule)) {
            alarmHandle(limitRule);
        } else {
            noAlarmHandle(limitRule);
        }
    }

    private void alarmHandle(LimitRule limitRule) {
        //现在达到报警状态了
        if (limitRule.getProduct().getDisplay()) {
            sessionListener.getHttpSessionMap().values().forEach(s -> {
                Map<String, AlarmMessage> alarmMap = (Map<String, AlarmMessage>) s.getAttribute(SessionContextEnum.SESSIONCONTEXT_ALARMLIST.getCode());
                AlarmMessage alarmMessage = AlarmMessage.builder()
                        .context(limitRule.getPushAudioContext())
                        .date(new Date())
                        .level(0L)
                        .product(limitRule.getProduct().getCode())
                        .rate(0.0)
                        .value(limitRule.getValue())
                        .alarmId(getAlaramId(limitRule))
                        .build();
                alarmMap.put(limitRule.getTag(), alarmMessage);
            });
        }


        //之前不报警，新出现的报警是需要判断是否需要语音或微信推送的
        if (!limitRule.getIsAlarm().get()) {
            //之前没在报警状态
            limitRule.setBegionAlarmTime(LocalDateTime.now());

            log.info(Thread.currentThread() + " LIMIT_HIGH  context:{}", limitRule.getPushWXContext());

            //微信推送
            if (limitRule.isPushWX() || (limitRule.isAudio())) {
                //微信推送
                if (limitRule.isPushWX()) {
                    if (ObjectUtils.isEmpty(limitRule.getPushWXLastTime()) || limitRule.getPushWXLastTime().plus(wxPushConfig.getPushIntervalSec(), ChronoUnit.SECONDS).isBefore(LocalDateTime.now())) {
                        WXPushTools.sendwx(wxPushConfig.getUrl(), limitRule.getPushWXContext(), wxPushConfig.getDepartment());
                        limitRule.setPushWXLastTime(LocalDateTime.now());
                    }
                }
                //语音报警
                if (limitRule.isAudio()) {

                    if (!CollectionUtils.isEmpty(sessionListener.getHttpSessionMap())) {
                            sessionListener.getHttpSessionMap().values().forEach(s -> {
                                Map<String,LocalDateTime> sessionAudioTime=(Map<String,LocalDateTime>)s.getAttribute(SessionContextEnum.SESSIONCONTEXT_AUDIOPUSHLASTTIME.getCode());
                                if (ObjectUtils.isEmpty(sessionAudioTime.get(limitRule.getTag())) || sessionAudioTime.get(limitRule.getTag()).plus(wxPushConfig.getPushIntervalSec(), ChronoUnit.SECONDS).isBefore(LocalDateTime.now())) {
                                    Map<String, AlarmMessage> audioAlarmMap = (Map<String, AlarmMessage>) s.getAttribute(SessionContextEnum.SESSIONCONTEXT_AUDIOLIST.getCode());
                                    synchronized (audioAlarmMap){
                                        AlarmMessage alarmMessage = AlarmMessage.builder()
                                                .context(limitRule.getPushAudioContext())
                                                .date(new Date())
                                                .level(0L)
                                                .product(limitRule.getProduct().getCode())
                                                .rate(0.0)
                                                .value(limitRule.getValue())
                                                .build();
                                        audioAlarmMap.put(limitRule.getTag(), alarmMessage);
                                    }

                                    //update
                                    sessionAudioTime.put(limitRule.getTag(),LocalDateTime.now());
                                }
                            });
                    }
                }
            }

            limitRule.getIsAlarm().set(true);
        }

        //已经在报警状态了，判断持续时间是否超过，超过则进行报警,这里不记录上次报警时间
        if (Duration.between(limitRule.getBegionAlarmTime(), LocalDateTime.now()).getSeconds() > wxPushConfig.getContinueAlarmSec()) {
            //微信推送
            if (limitRule.isPushWX()) {
                    WXPushTools.sendwx(wxPushConfig.getUrl(), limitRule.getPushWXContext(), wxPushConfig.getDepartment());
            }
            //语音报警
            if (limitRule.isAudio()) {
                    if (!CollectionUtils.isEmpty(sessionListener.getHttpSessionMap())) {
                        sessionListener.getHttpSessionMap().values().forEach(s -> {
                            Map<String, AlarmMessage> audioAlarmMap = (Map<String, AlarmMessage>) s.getAttribute(SessionContextEnum.SESSIONCONTEXT_AUDIOLIST.getCode());
                            synchronized (audioAlarmMap){
                                AlarmMessage alarmMessage = AlarmMessage.builder()
                                        .context(limitRule.getPushAudioContext())
                                        .date(new Date())
                                        .level(0L)
                                        .product(limitRule.getProduct().getCode())
                                        .rate(0.0)
                                        .value(limitRule.getValue())
                                        .build();
                                audioAlarmMap.put(limitRule.getTag(), alarmMessage);
                            }

                        });
                }
            }
            //推送完成以后重置下开始报警时间
            limitRule.setBegionAlarmTime(LocalDateTime.now());
        }

    }

    private void noAlarmHandle(LimitRule limitRule) {
        //不报警，那么需要进行web报警画面消除
        //现在达到报警状态了
        if (limitRule.getProduct().getDisplay()) {
            sessionListener.getHttpSessionMap().values().forEach(s -> {
                Map<String, AlarmMessage> AlarmMap = (Map<String, AlarmMessage>) s.getAttribute(SessionContextEnum.SESSIONCONTEXT_ALARMLIST.getCode());
                AlarmMessage alarmMessage=AlarmMap.get(limitRule.getTag());
                if(alarmMessage!=null){
                    if(getAlaramId(limitRule).equals(alarmMessage.getAlarmId())){
                        AlarmMap.remove(limitRule.getTag());
                    }
                }
            });
        }
        limitRule.getIsAlarm().set(false);
    }

    private String  getAlaramId(BaseRule baseRule){
        return baseRule.getTag()+baseRule.getAlarmModelEnum().getCode()+baseRule.getSubModel();
    }


}
