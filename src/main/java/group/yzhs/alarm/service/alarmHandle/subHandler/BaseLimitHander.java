package group.yzhs.alarm.service.alarmHandle.subHandler;

import group.yzhs.alarm.config.WXPushConfig;
import group.yzhs.alarm.constant.AlarmPushStatusEnum;
import group.yzhs.alarm.constant.SessionContextEnum;
import group.yzhs.alarm.listener.SessionListener;
import group.yzhs.alarm.mapper.impl.AlarmHistoryMapperImp;
import group.yzhs.alarm.mapper.impl.PointMapperImp;
import group.yzhs.alarm.model.AlarmMessage;
import group.yzhs.alarm.model.entity.AlarmHistory;
import group.yzhs.alarm.model.rule.BaseRule;
import group.yzhs.alarm.model.rule.limit.LimitRule;
import group.yzhs.alarm.service.alarmHandle.SubHandler;
import group.yzhs.alarm.utils.WXPushTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
 * @date 2021/10/22 17:01
 */
@Slf4j
public abstract class BaseLimitHander implements SubHandler {


    private SessionListener sessionListener;


    private WXPushConfig wxPushConfig;


    private PointMapperImp pointMapperImp;

    private AlarmHistoryMapperImp alarmHistoryMapperImp;


    public BaseLimitHander(SessionListener sessionListener, WXPushConfig wxPushConfig, PointMapperImp pointMapperImp, AlarmHistoryMapperImp alarmHistoryMapperImp) {
        this.sessionListener = sessionListener;
        this.wxPushConfig = wxPushConfig;
        this.pointMapperImp = pointMapperImp;
        this.alarmHistoryMapperImp = alarmHistoryMapperImp;
    }

    public abstract void alarmHandle(LimitRule limitRule);
    public abstract void noAlarmHandle(LimitRule limitRule);

    public void defaultAlarmHandle(LimitRule limitRule) {
        AlarmHistory newAlarmHistory=null;
        if(!limitRule.getIsAlarm().get()){
            //之前没发送报警，保存到数据库中
            newAlarmHistory=new AlarmHistory();
            newAlarmHistory.setAlarmContext(limitRule.getPushWXContext());
            newAlarmHistory.setAlarmTime(new Date());
            newAlarmHistory.setRefAlarmRuleId(limitRule.getId());
            newAlarmHistory.setPushStatus(AlarmPushStatusEnum.ALARM_PUSH_STATUS_PRODUCT);
            alarmHistoryMapperImp.save(newAlarmHistory);
        }


        //现在达到报警状态了
        if (limitRule.getAlarmGroup().getDisplay()) {
            AlarmHistory finalAlarmHistory = newAlarmHistory;
            sessionListener.getHttpSessionMap().values().forEach(s -> {
                Map<String, AlarmMessage> alarmMap = (Map<String, AlarmMessage>) s.getAttribute(SessionContextEnum.SESSIONCONTEXT_ALARMLIST.getCode());
                //获取旧的报警
                AlarmMessage oldAlarmMessage=alarmMap.get(limitRule.getIotNode()+"="+limitRule.getTag());
                AlarmMessage alarmMessage=null;
                if(!ObjectUtils.isEmpty(finalAlarmHistory)){
                    alarmMessage = AlarmMessage.builder()
                            .context(limitRule.getPushAudioContext())
                            .date(new Date())
                            .level(0L)
                            .product(limitRule.getAlarmGroup().getCode())
                            .rate(0.0)
                            .value(limitRule.getValue())
                            .alarmId(getAlaramId(limitRule))
                            .alarmHistoryId(finalAlarmHistory.getId())
                            .pushStatus(AlarmPushStatusEnum.ALARM_PUSH_STATUS_PRODUCT)
                            .build();
                }else{
                    if(!ObjectUtils.isEmpty(oldAlarmMessage)){
                        AlarmHistory existAlarmHistory=alarmHistoryMapperImp.getById(oldAlarmMessage.getAlarmHistoryId());
                        if(!ObjectUtils.isEmpty(existAlarmHistory)){
                            alarmMessage = AlarmMessage.builder()
                                    .context(limitRule.getPushAudioContext())
                                    .date(new Date())
                                    .level(0L)
                                    .product(limitRule.getAlarmGroup().getCode())
                                    .rate(0.0)
                                    .value(limitRule.getValue())
                                    .alarmId(getAlaramId(limitRule))
                                    .alarmHistoryId(existAlarmHistory.getId())
                                    .pushStatus(existAlarmHistory.getPushStatus())
                                    .build();
                        }

                    }
                }

                if(ObjectUtils.isEmpty(alarmMessage)){
                    alarmMap.put(limitRule.getIotNode()+"="+limitRule.getTag(), alarmMessage);
                }

            });
        }


        //之前不报警，新出现的报警是需要判断是否需要语音或微信推送的
        if (!limitRule.getIsAlarm().get()) {
            //之前没在报警状态
            limitRule.setBegionAlarmTime(LocalDateTime.now());

            log.info(Thread.currentThread() + " LIMIT  context:{}", limitRule.getPushWXContext());

            //微信推送
            if (limitRule.getIsWxPush() || (limitRule.getIsAudio())) {
                //微信推送
                if (limitRule.getIsWxPush()) {
                    if (ObjectUtils.isEmpty(limitRule.getPushWXLastTime()) || limitRule.getPushWXLastTime().plus(wxPushConfig.getPushIntervalSec(), ChronoUnit.SECONDS).isBefore(LocalDateTime.now())) {
                        WXPushTools.sendwx(wxPushConfig.getUrl(), limitRule.getPushWXContext(), wxPushConfig.getDepartment());
                        limitRule.setPushWXLastTime(LocalDateTime.now());
                    }
                }
                //语音报警
                if (limitRule.getIsAudio()) {

                    if (!CollectionUtils.isEmpty(sessionListener.getHttpSessionMap())) {
                        sessionListener.getHttpSessionMap().values().forEach(s -> {
                            Map<String,LocalDateTime> sessionAudioTime=(Map<String,LocalDateTime>)s.getAttribute(SessionContextEnum.SESSIONCONTEXT_AUDIOPUSHLASTTIME.getCode());
                            if (ObjectUtils.isEmpty(sessionAudioTime.get(limitRule.getIotNode()+"="+limitRule.getTag())) || sessionAudioTime.get(limitRule.getIotNode()+"="+limitRule.getTag()).plus(wxPushConfig.getPushIntervalSec(), ChronoUnit.SECONDS).isBefore(LocalDateTime.now())) {
                                Map<String, AlarmMessage> audioAlarmMap = (Map<String, AlarmMessage>) s.getAttribute(SessionContextEnum.SESSIONCONTEXT_AUDIOLIST.getCode());
                                synchronized (audioAlarmMap){
                                    AlarmMessage alarmMessage = AlarmMessage.builder()
                                            .context(limitRule.getPushAudioContext())
                                            .date(new Date())
                                            .level(0L)
                                            .product(limitRule.getAlarmGroup().getCode())
                                            .rate(0.0)
                                            .value(limitRule.getValue())
                                            .build();
                                    audioAlarmMap.put(limitRule.getTag(), alarmMessage);
                                }

                                //update
                                sessionAudioTime.put(limitRule.getIotNode()+"="+limitRule.getTag(),LocalDateTime.now());
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
            if (limitRule.getIsWxPush()) {
                WXPushTools.sendwx(wxPushConfig.getUrl(), limitRule.getPushWXContext(), wxPushConfig.getDepartment());
            }
            //语音报警
            if (limitRule.getIsAudio()) {
                if (!CollectionUtils.isEmpty(sessionListener.getHttpSessionMap())) {
                    sessionListener.getHttpSessionMap().values().forEach(s -> {
                        Map<String, AlarmMessage> audioAlarmMap = (Map<String, AlarmMessage>) s.getAttribute(SessionContextEnum.SESSIONCONTEXT_AUDIOLIST.getCode());
                        synchronized (audioAlarmMap){
                            AlarmMessage alarmMessage = AlarmMessage.builder()
                                    .context(limitRule.getPushAudioContext())
                                    .date(new Date())
                                    .level(0L)
                                    .product(limitRule.getAlarmGroup().getCode())
                                    .rate(0.0)
                                    .value(limitRule.getValue())
                                    .build();
                            audioAlarmMap.put(limitRule.getIotNode()+"="+limitRule.getTag(), alarmMessage);
                        }

                    });
                }
            }
            //推送完成以后重置下开始报警时间
            limitRule.setBegionAlarmTime(LocalDateTime.now());
        }

    }

    public void defaultNoAlarmHandle(LimitRule limitRule) {
        //不报警，那么需要进行web报警画面消除
        //现在达到报警状态了
        if (limitRule.getAlarmGroup().getDisplay()) {
            sessionListener.getHttpSessionMap().values().forEach(s -> {
                Map<String, AlarmMessage> AlarmMap = (Map<String, AlarmMessage>) s.getAttribute(SessionContextEnum.SESSIONCONTEXT_ALARMLIST.getCode());
                AlarmMessage alarmMessage=AlarmMap.get(limitRule.getTag());
                if(alarmMessage!=null){
                    if(getAlaramId(limitRule).equals(alarmMessage.getAlarmId())){
                        AlarmMap.remove(limitRule.getIotNode()+"="+limitRule.getTag());
                    }
                }
            });
        }
        limitRule.getIsAlarm().set(false);
    }

    private String  getAlaramId(BaseRule baseRule){
        return baseRule.getIotNode()+"="+baseRule.getTag()+baseRule.getAlarmMode()+baseRule.getAlarmSubMode();
    }


}
