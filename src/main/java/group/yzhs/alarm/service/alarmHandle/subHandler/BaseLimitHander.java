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
import group.yzhs.alarm.model.entity.SystemConfig;
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
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

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

    private SystemConfigMapperImp systemConfigMapperImp;

    private ExecutorService executorService;

    public BaseLimitHander(SessionListener sessionListener,
                           WXPushConfig wxPushConfig,
                           PointMapperImp pointMapperImp,
                           AlarmHistoryMapperImp alarmHistoryMapperImp,
                           SystemConfigMapperImp systemConfigMapperImp,
                           ExecutorService executorService) {
        this.sessionListener = sessionListener;
        this.wxPushConfig = wxPushConfig;
        this.pointMapperImp = pointMapperImp;
        this.alarmHistoryMapperImp = alarmHistoryMapperImp;
        this.systemConfigMapperImp = systemConfigMapperImp;
        this.executorService = executorService;
    }

    public abstract void alarmHandle(LimitRule limitRule);

    public abstract void noAlarmHandle(LimitRule limitRule);

    public void defaultAlarmHandle(LimitRule limitRule) {
        AlarmHistory newAlarmHistory = null;
        if (!limitRule.getIsAlarm().get()) {
            //之前没发送报警，保存到数据库中
            newAlarmHistory = new AlarmHistory();
            newAlarmHistory.setAlarmContext(limitRule.getPushWXContext());
            newAlarmHistory.setCreateTime(new Date());
            newAlarmHistory.setRefAlarmRuleId(limitRule.getId());
            newAlarmHistory.setPushStatus(AlarmPushStatusEnum.ALARM_PUSH_STATUS_PRODUCT);
            alarmHistoryMapperImp.save(newAlarmHistory);
        }


        //现在达到报警状态了
        if (/*limitRule.getAlarmGroup().getDisplay()*/true) {
            AlarmHistory finalAlarmHistory = newAlarmHistory;
            log.debug(Thread.currentThread() + " LIMIT  context:{}", limitRule.getPushWXContext());
            log.info("*****session size={}", sessionListener.getHttpSessionMap().size());
            sessionListener.getHttpSessionMap().values().forEach(s -> {
                //获取历史报警的缓存
                Map<String, AlarmMessage> alarmMap = (Map<String, AlarmMessage>) s.getAttribute(SessionContextEnum.SESSIONCONTEXT_ALARMLIST.getCode());

                AlarmMessage alarmMessage = null;
                if (!ObjectUtils.isEmpty(finalAlarmHistory)) {
                    //新产生的报警，那么直接放进去就行了
                    alarmMessage = AlarmMessage.builder()
                            .context(limitRule.getPushAudioContext())
                            .date(new Date())
                            .level(0L)
                            .product(limitRule.getAlarmGroup())
                            .rate(0.0)
                            .value(limitRule.getValue())
                            .alarmId(getAlaramId(limitRule))
                            .alarmHistoryId(finalAlarmHistory.getId())
                            .pushStatus(AlarmPushStatusEnum.ALARM_PUSH_STATUS_PRODUCT)
                            .build();
                } else {
                    //旧的报警，从session中获取旧的报警，然后对其数据进行更新
                    AlarmMessage oldAlarmMessage = alarmMap.get(limitRule.getIotNode() + "=" + limitRule.getTag());
                    if (!ObjectUtils.isEmpty(oldAlarmMessage)) {
                        AlarmHistory existAlarmHistory = alarmHistoryMapperImp.getById(oldAlarmMessage.getAlarmHistoryId());
                        if (!ObjectUtils.isEmpty(existAlarmHistory)) {
                            alarmMessage = AlarmMessage.builder()
                                    .context(limitRule.getPushAudioContext())
                                    .date(new Date())
                                    .level(0L)
                                    .product(limitRule.getAlarmGroup())
                                    .rate(0.0)
                                    .value(limitRule.getValue())
                                    .alarmId(getAlaramId(limitRule))
                                    .alarmHistoryId(existAlarmHistory.getId())
                                    .pushStatus(existAlarmHistory.getPushStatus())
                                    .build();
                        }

                    } else {
                        //如果缓存里没有，那么说明报警产生的时候，用户还没生成，那么直接就添加新的
                        AlarmHistory alarmHistory = alarmHistoryMapperImp.getLastAlatmHistoryByNodeTag(limitRule.getId());
                        if (!ObjectUtils.isEmpty(alarmHistory)) {
                            alarmMessage = AlarmMessage.builder()
                                    .context(limitRule.getPushAudioContext())
                                    .date(new Date())
                                    .level(0L)
                                    .product(limitRule.getAlarmGroup())
                                    .rate(0.0)
                                    .value(limitRule.getValue())
                                    .alarmId(getAlaramId(limitRule))
                                    .alarmHistoryId(alarmHistory.getId())
                                    .pushStatus(alarmHistory.getPushStatus())
                                    .build();

                        }
                    }
                }

                if (!ObjectUtils.isEmpty(alarmMessage)) {
                    alarmMap.put(limitRule.getIotNode() + "=" + limitRule.getTag(), alarmMessage);
                }

            });
        }


        //之前不报警，新出现的报警是需要判断是否需要语音或微信推送的
        if (!limitRule.getIsAlarm().get()) {
            //之前没在报警状态
            limitRule.setBegionAlarmTime(LocalDateTime.now());
//            log.info(Thread.currentThread() + " LIMIT  context:{}", limitRule.getPushWXContext());
            //微信推送
            if (limitRule.getIsWxPush() || (limitRule.getIsAudio())) {
                //微信推送
                List<String> wxconfig = Arrays.asList(SysConfigEnum.SYS_CONFIG_pushIntervalSec.getCode(), SysConfigEnum.SYS_CONFIG_department.getCode(), SysConfigEnum.SYS_CONFIG_url.getCode());
                List<SystemConfig> systemConfigs = systemConfigMapperImp.list(Wrappers.<SystemConfig>lambdaQuery().in(SystemConfig::getCode, wxconfig));
                Map<String, SystemConfig> systemConfigMap = systemConfigs.stream().collect(Collectors.toMap(SystemConfig::getCode, p -> p, (o, n) -> n));

                if (limitRule.getIsWxPush() && (systemConfigMap.size() == 3)) {
                    if (ObjectUtils.isEmpty(limitRule.getPushWXLastTime()) || limitRule.getPushWXLastTime().plus(Long.parseLong(systemConfigMap.get(SysConfigEnum.SYS_CONFIG_url.getCode()).getValue())/*wxPushConfig.getPushIntervalSec()*/, ChronoUnit.SECONDS).isBefore(LocalDateTime.now())) {
                        //微信推送配置
                        executorService.execute(()->{
                            WXPushTools.sendwx(systemConfigMap.get(SysConfigEnum.SYS_CONFIG_url.getCode()).getValue()/*wxPushConfig.getUrl()*/, limitRule.getPushWXContext(), systemConfigMap.get(SysConfigEnum.SYS_CONFIG_department.getCode()).getValue()/*wxPushConfig.getDepartment()*/);
                        });

                        limitRule.setPushWXLastTime(LocalDateTime.now());
                    }
                }
                //语音报警
                if (limitRule.getIsAudio()) {

                    if (!CollectionUtils.isEmpty(sessionListener.getHttpSessionMap())) {
                        sessionListener.getHttpSessionMap().values().forEach(s -> {
                            Map<String, LocalDateTime> sessionAudioTime = (Map<String, LocalDateTime>) s.getAttribute(SessionContextEnum.SESSIONCONTEXT_AUDIOPUSHLASTTIME.getCode());
                            if (ObjectUtils.isEmpty(sessionAudioTime.get(limitRule.getIotNode() + "=" + limitRule.getTag())) || sessionAudioTime.get(limitRule.getIotNode() + "=" + limitRule.getTag()).plus(wxPushConfig.getPushIntervalSec(), ChronoUnit.SECONDS).isBefore(LocalDateTime.now())) {
                                Map<String, AlarmMessage> audioAlarmMap = (Map<String, AlarmMessage>) s.getAttribute(SessionContextEnum.SESSIONCONTEXT_AUDIOLIST.getCode());
                                synchronized (audioAlarmMap) {
                                    AlarmMessage alarmMessage = AlarmMessage.builder()
                                            .context(limitRule.getPushAudioContext())
                                            .date(new Date())
                                            .level(0L)
                                            .product(limitRule.getAlarmGroup())
                                            .rate(0.0)
                                            .value(limitRule.getValue())
                                            .build();
                                    audioAlarmMap.put(limitRule.getTag(), alarmMessage);
                                }

                                //update
                                sessionAudioTime.put(limitRule.getIotNode() + "=" + limitRule.getTag(), LocalDateTime.now());
                            }
                        });
                    }
                }
            }

            limitRule.getIsAlarm().set(true);
        }

        List<String> wxconfig = Arrays.asList(SysConfigEnum.SYS_CONFIG_pushIntervalSec.getCode(), SysConfigEnum.SYS_CONFIG_department.getCode(), SysConfigEnum.SYS_CONFIG_url.getCode(), SysConfigEnum.SYS_CONFIG_continueAlarmSec.getCode());
        List<SystemConfig> systemConfigs = systemConfigMapperImp.list(Wrappers.<SystemConfig>lambdaQuery().in(SystemConfig::getCode, wxconfig));
        Map<String, SystemConfig> systemConfigMap = systemConfigs.stream().collect(Collectors.toMap(SystemConfig::getCode, p -> p, (o, n) -> n));


        //已经在报警状态了，判断持续时间是否超过，超过则进行报警,这里不记录上次报警时间
        if (systemConfigMap.size() == 4 && Duration.between(limitRule.getBegionAlarmTime(), LocalDateTime.now()).getSeconds() > Float.parseFloat(systemConfigMap.get(SysConfigEnum.SYS_CONFIG_continueAlarmSec.getCode()).getValue()) /*wxPushConfig.getContinueAlarmSec()*/) {
            //微信推送
            if (limitRule.getIsWxPush()) {
                executorService.execute(() -> {
                    WXPushTools.sendwx(systemConfigMap.get(SysConfigEnum.SYS_CONFIG_url.getCode()).getValue()/*wxPushConfig.getUrl()*/, limitRule.getPushWXContext(), systemConfigMap.get(SysConfigEnum.SYS_CONFIG_department.getCode()).getValue() /*wxPushConfig.getDepartment()*/);
                });
            }
            //语音报警
            if (limitRule.getIsAudio()) {
                if (!CollectionUtils.isEmpty(sessionListener.getHttpSessionMap())) {
                    sessionListener.getHttpSessionMap().values().forEach(s -> {
                        Map<String, AlarmMessage> audioAlarmMap = (Map<String, AlarmMessage>) s.getAttribute(SessionContextEnum.SESSIONCONTEXT_AUDIOLIST.getCode());
                        synchronized (audioAlarmMap) {
                            AlarmMessage alarmMessage = AlarmMessage.builder()
                                    .context(limitRule.getPushAudioContext())
                                    .date(new Date())
                                    .level(0L)
                                    .product(limitRule.getAlarmGroup())
                                    .rate(0.0)
                                    .value(limitRule.getValue())
                                    .build();
                            audioAlarmMap.put(limitRule.getIotNode() + "=" + limitRule.getTag(), alarmMessage);
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
        if (/*limitRule.getAlarmGroup().getDisplay()*/true) {
            sessionListener.getHttpSessionMap().values().forEach(s -> {
                Map<String, AlarmMessage> AlarmMap = (Map<String, AlarmMessage>) s.getAttribute(SessionContextEnum.SESSIONCONTEXT_ALARMLIST.getCode());
                AlarmMessage alarmMessage = AlarmMap.get(limitRule.getIotNode() + "=" + limitRule.getTag());
                if (alarmMessage != null) {
                    if (getAlaramId(limitRule).equals(alarmMessage.getAlarmId())) {
                        AlarmMap.remove(limitRule.getIotNode() + "=" + limitRule.getTag());
                    }
                }
            });
        }
        limitRule.getIsAlarm().set(false);
    }

    private String getAlaramId(BaseRule baseRule) {
        return baseRule.getIotNode() + "=" + baseRule.getTag() + baseRule.getAlarmMode() + baseRule.getAlarmSubMode();
    }


}
