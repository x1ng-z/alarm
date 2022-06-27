package group.yzhs.alarm.service.alarmHandle.subHandler;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import group.yzhs.alarm.config.WXPushConfig;
import group.yzhs.alarm.constant.AlarmPushStatusEnum;
import group.yzhs.alarm.constant.SessionContextEnum;
import group.yzhs.alarm.constant.SysConfigEnum;
import group.yzhs.alarm.listener.SessionListener;
import group.yzhs.alarm.mapper.impl.AlarmHistoryMapperImp;
import group.yzhs.alarm.mapper.impl.PointMapperImp;
import group.yzhs.alarm.mapper.impl.SystemConfigMapperImp;
import group.yzhs.alarm.model.AlarmMessage;
import group.yzhs.alarm.model.entity.AlarmHistory;
import group.yzhs.alarm.model.entity.Point;
import group.yzhs.alarm.model.entity.SystemConfig;
import group.yzhs.alarm.model.rule.BaseRule;
import group.yzhs.alarm.model.rule.trigger.TriggerRule;
import group.yzhs.alarm.service.alarmHandle.SubHandler;
import group.yzhs.alarm.utils.WXPushTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/10/22 16:49
 */
@Slf4j
public abstract class BaseTrigerHandler implements SubHandler {

    private SessionListener sessionListener;


    private WXPushConfig wxPushConfig;


    private PointMapperImp pointMapperImp;


    private AlarmHistoryMapperImp alarmHistoryMapperImp;
    private SystemConfigMapperImp systemConfigMapperImp;
    private ExecutorService executorService;

    public BaseTrigerHandler(SessionListener sessionListener,
                             WXPushConfig wxPushConfig,
                             PointMapperImp pointMapperImp,
                             AlarmHistoryMapperImp alarmHistoryMapperImp,
                             SystemConfigMapperImp systemConfigMapperImp,
                             ExecutorService executorService) {
        this.sessionListener = sessionListener;
        this.wxPushConfig = wxPushConfig;
        this.pointMapperImp = pointMapperImp;
        this.alarmHistoryMapperImp = alarmHistoryMapperImp;
        this.systemConfigMapperImp= systemConfigMapperImp;
        this.executorService=executorService;
    }

    public abstract  void alarmHandle(BaseRule triggerRule);
    public abstract void noAlarmHandle(BaseRule triggerRule);



    public void defaultAlarmHandle(BaseRule triggerRule) {
        //现在达到报警状态了
        AlarmHistory newAlarmHistory=null;
        //是否需要保存/查询
        if(!triggerRule.getIsAlarm().get()){
            //之前没发送报警，保存到数据库中
            newAlarmHistory=new AlarmHistory();
            newAlarmHistory.setAlarmContext(triggerRule.getPushWXContext());
            newAlarmHistory.setCreateTime(new Date());
            newAlarmHistory.setRefAlarmRuleId(triggerRule.getId());
            newAlarmHistory.setPushStatus(AlarmPushStatusEnum.ALARM_PUSH_STATUS_PRODUCT);
            alarmHistoryMapperImp.save(newAlarmHistory);
        }

        if (/*triggerRule.getAlarmGroup().getDisplay()*/true) {
            log.debug(" TRG context:{}", triggerRule.getPushWXContext());
            AlarmHistory finalAlarmHistory = newAlarmHistory;
            sessionListener.getHttpSessionMap().values().forEach(s -> {
                Map<String, AlarmMessage> alarmMap = (Map<String, AlarmMessage>) s.getAttribute(SessionContextEnum.SESSIONCONTEXT_ALARMLIST.getCode());
                AlarmMessage alarmMessage=null;

                if(!ObjectUtils.isEmpty(finalAlarmHistory)){
                    //新报警
                    alarmMessage= AlarmMessage.builder()
                            .context(triggerRule.getPushAudioContext())
                            .date(new Date())
                            .level(0L)
                            .product(triggerRule.getAlarmGroup())
                            .rate(0.0)
                            .value(triggerRule.getValue())
                            .alarmId(getAlaramId(triggerRule))
                            .pushStatus(AlarmPushStatusEnum.ALARM_PUSH_STATUS_PRODUCT)
                            .alarmHistoryId(finalAlarmHistory.getId())
                            .build();
                }else{
                    //旧的报警，查询推送状态
                    //获取旧的报警
                    AlarmMessage oldAlarmMessage=alarmMap.get(triggerRule.getIotNode()+"="+triggerRule.getTag());
                    if(!ObjectUtils.isEmpty(oldAlarmMessage)){
                        AlarmHistory existAlarmHistory=alarmHistoryMapperImp.getById(oldAlarmMessage.getAlarmHistoryId());
                        if(!ObjectUtils.isEmpty(existAlarmHistory)){
                            alarmMessage= AlarmMessage.builder()
                                    .context(triggerRule.getPushAudioContext())
                                    .date(new Date())
                                    .level(0L)
                                    .product(triggerRule.getAlarmGroup())
                                    .rate(0.0)
                                    .value(triggerRule.getValue())
                                    .alarmId(getAlaramId(triggerRule))
                                    .pushStatus(existAlarmHistory.getPushStatus())
                                    .alarmHistoryId(existAlarmHistory.getId())
                                    .build();
                        }

                    }else{
                        //旧的报警，但是用户是新的，所以需要从数据库中查询以下
                        AlarmHistory alarmHistory=alarmHistoryMapperImp.getLastAlatmHistoryByNodeTag(triggerRule.getId());
                        if(!ObjectUtils.isEmpty(alarmHistory)){
                            alarmMessage= AlarmMessage.builder()
                                    .context(triggerRule.getPushAudioContext())
                                    .date(new Date())
                                    .level(0L)
                                    .product(triggerRule.getAlarmGroup())
                                    .rate(0.0)
                                    .value(triggerRule.getValue())
                                    .alarmId(getAlaramId(triggerRule))
                                    .pushStatus(alarmHistory.getPushStatus())
                                    .alarmHistoryId(alarmHistory.getId())
                                    .build();
                        }
                    }
                }

                //跟替换更新，等到不报警的时候在将其移除
                if(!ObjectUtils.isEmpty(alarmMessage)) {
                    alarmMap.put(triggerRule.getIotNode() + "=" + triggerRule.getTag(), alarmMessage);
                }
            });
        }


        //之前不报警，新出现的报警是需要判断是否需要语音或微信推送的
        if (!triggerRule.getIsAlarm().get()) {


            //微信推送
            if (triggerRule.getIsWxPush() || (triggerRule.getIsAudio())) {
                //微信推送
                List<String> wxconfig= Arrays.asList(SysConfigEnum.SYS_CONFIG_pushIntervalSec.getCode(),SysConfigEnum.SYS_CONFIG_department.getCode(),SysConfigEnum.SYS_CONFIG_url.getCode());
                List<SystemConfig> systemConfigs =systemConfigMapperImp.list(Wrappers.<SystemConfig>lambdaQuery().in(SystemConfig::getCode,wxconfig));
                Map<String,SystemConfig>systemConfigMap=systemConfigs.stream().collect(Collectors.toMap(SystemConfig::getCode, p->p,(o, n)->n));

                if (triggerRule.getIsWxPush()&&systemConfigMap.size()==3) {
                    if (ObjectUtils.isEmpty(triggerRule.getPushWXLastTime()) || triggerRule.getPushWXLastTime().plus(Long.parseLong(systemConfigMap.get(SysConfigEnum.SYS_CONFIG_pushIntervalSec.getCode()).getValue())/*wxPushConfig.getPushIntervalSec()*/, ChronoUnit.SECONDS).isBefore(LocalDateTime.now())) {
                        executorService.execute(()->{
                            WXPushTools.sendwx(systemConfigMap.get(SysConfigEnum.SYS_CONFIG_url.getCode()).getValue()/*wxPushConfig.getUrl()*/, triggerRule.getPushWXContext(),systemConfigMap.get(SysConfigEnum.SYS_CONFIG_department.getCode()).getValue() /*wxPushConfig.getDepartment()*/);
                        });
                        triggerRule.setPushWXLastTime(LocalDateTime.now());
                    }
                }
                //语音报警
                if (triggerRule.getIsAudio()) {
                    if (!CollectionUtils.isEmpty(sessionListener.getHttpSessionMap())) {
                        sessionListener.getHttpSessionMap().values().forEach(s -> {
                            Map<String,LocalDateTime> sessionAudioTime=(Map<String,LocalDateTime>)s.getAttribute(SessionContextEnum.SESSIONCONTEXT_AUDIOPUSHLASTTIME.getCode());
                            if (ObjectUtils.isEmpty(sessionAudioTime.get(triggerRule.getIotNode()+"="+triggerRule.getTag())) || sessionAudioTime.get(triggerRule.getIotNode()+"="+triggerRule.getTag()).plus(wxPushConfig.getPushIntervalSec(), ChronoUnit.SECONDS).isBefore(LocalDateTime.now())) {
                                Map<String, AlarmMessage> audioAlarmMap = (Map<String, AlarmMessage>) s.getAttribute(SessionContextEnum.SESSIONCONTEXT_AUDIOLIST.getCode());
                                synchronized (audioAlarmMap){
                                    AlarmMessage alarmMessage = AlarmMessage.builder()
                                            .context(triggerRule.getPushAudioContext())
                                            .date(new Date())
                                            .level(0L)
                                            .product(triggerRule.getAlarmGroup())
                                            .rate(0.0)
                                            .value(triggerRule.getValue())
//                                            .alarmHistoryId(newAlarmHistory.getId())
//                                            .pushStatus(AlarmPushStatusEnum.ALARM_PUSH_STATUS_PRODUCT)
                                            .build();
                                    audioAlarmMap.put(triggerRule.getIotNode()+"="+triggerRule.getTag(), alarmMessage);
                                }
                                //update
                                sessionAudioTime.put(triggerRule.getIotNode()+"="+triggerRule.getTag(),LocalDateTime.now());
                            }
                        });
                    }
                }

            }

            triggerRule.getIsAlarm().set(true);
        }

    }

    public void defaultNoAlarmHandle(BaseRule triggerRule) {
        //点位信息查询
        //Point point =pointMapperImp.getById(triggerRule.getPointId());
        //不报警，那么需要进行web报警画面消除
        //现在达到报警状态了
        if (/*triggerRule.getAlarmGroup().getDisplay()*/true) {
            sessionListener.getHttpSessionMap().values().forEach(s -> {
                Map<String, AlarmMessage> AlarmMap = (Map<String, AlarmMessage>) s.getAttribute(SessionContextEnum.SESSIONCONTEXT_ALARMLIST.getCode());
                AlarmMessage alarmMessage=AlarmMap.get(triggerRule.getIotNode()+"="+triggerRule.getTag());
                if(alarmMessage!=null){
                    if(getAlaramId(triggerRule).equals(alarmMessage.getAlarmId())){
                        AlarmMap.remove(triggerRule.getIotNode()+"="+triggerRule.getTag());
                    }
                }
            });
        }
        triggerRule.getIsAlarm().set(false);
    }

    private String  getAlaramId(BaseRule baseRule){
        return baseRule.getIotNode()+"="+baseRule.getTag()+baseRule.getAlarmMode()+baseRule.getAlarmSubMode();
    }

}
