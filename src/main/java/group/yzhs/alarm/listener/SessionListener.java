package group.yzhs.alarm.listener;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import group.yzhs.alarm.config.WXPushConfig;
import group.yzhs.alarm.constant.AlarmPushStatusEnum;
import group.yzhs.alarm.constant.SessionContextEnum;
import group.yzhs.alarm.constant.SysConfigEnum;
import group.yzhs.alarm.mapper.impl.AlarmHistoryMapperImp;
import group.yzhs.alarm.mapper.impl.SystemConfigMapperImp;
import group.yzhs.alarm.model.AlarmMessage;
import group.yzhs.alarm.model.entity.AlarmHistory;
import group.yzhs.alarm.model.entity.SystemConfig;
import group.yzhs.alarm.model.rule.BaseRule;
import group.yzhs.alarm.model.rule.limit.LimitRule;
import group.yzhs.alarm.service.JudgementService;
import group.yzhs.alarm.service.alarm.AlarmRuleService;
import group.yzhs.alarm.utils.WXPushTools;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/8/13 9:19
 */
@Component
@WebListener
@Slf4j
@Getter
public class SessionListener implements HttpSessionListener {
    //session cache
    private Map<String, HttpSession> httpSessionMap = new ConcurrentHashMap<>();
    @Autowired
    private AlarmHistoryMapperImp alarmHistoryMapperImp;
    //    @Autowired
    @Autowired
    private SystemConfigMapperImp systemConfigMapperImp;

    @Qualifier("http-wx-push-thread")
    @Autowired
    private ExecutorService executorService;


    @Override
    public void sessionCreated(HttpSessionEvent se) {
        //add alarm list and audio list， alarm-list：key=tag
        se.getSession().setAttribute(SessionContextEnum.SESSIONCONTEXT_ALARMLIST.getCode(), new ConcurrentHashMap<String, AlarmMessage>());
        //key=tag
        se.getSession().setAttribute(SessionContextEnum.SESSIONCONTEXT_AUDIOLIST.getCode(), new ConcurrentHashMap<String, AlarmMessage>());
        //key=tag
        se.getSession().setAttribute(SessionContextEnum.SESSIONCONTEXT_AUDIOPUSHLASTTIME.getCode(), new ConcurrentHashMap<String, LocalDateTime>());
        httpSessionMap.put(se.getSession().getId(), se.getSession());
        log.info("a new session has created.id={},size={}", se.getSession().getId(), httpSessionMap.size());

    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        log.info("a expired session has remove.id={}", se.getSession().getId());
        HttpSession httpSession = httpSessionMap.remove(se.getSession().getId());
        //help gc
        httpSession.removeAttribute(SessionContextEnum.SESSIONCONTEXT_ALARMLIST.getCode());
        httpSession.removeAttribute(SessionContextEnum.SESSIONCONTEXT_AUDIOLIST.getCode());

        se.getSession().removeAttribute(SessionContextEnum.SESSIONCONTEXT_AUDIOPUSHLASTTIME.getCode());
    }

    public static Map<String, AlarmMessage> getAlarmList(HttpSession session) {
        Object attribute = session.getAttribute(SessionContextEnum.SESSIONCONTEXT_ALARMLIST.getCode());
        if (null != attribute && attribute instanceof Map) {
            return (Map<String, AlarmMessage>) attribute;
        }
        return null;
    }


    public static Map<String, AlarmMessage> getAudioList(HttpSession session) {
        Object attribute = session.getAttribute(SessionContextEnum.SESSIONCONTEXT_AUDIOLIST.getCode());
        if (null != attribute && attribute instanceof Map) {
            return (Map<String, AlarmMessage>) attribute;
        }
        return null;
    }

    public static Map<String, LocalDateTime> getAudioPushTimeList(HttpSession session) {
        Object attribute = session.getAttribute(SessionContextEnum.SESSIONCONTEXT_AUDIOPUSHLASTTIME.getCode());
        if (null != attribute && attribute instanceof Map) {
            return (Map<String, LocalDateTime>) attribute;
        }
        return null;
    }

    /**
     * 报警列表操作
     */
    public void alarmListOperate(HttpSession session, BaseRule baseRule) {
        //初始化
        AlarmPushStatusEnum historyPushStatus = AlarmPushStatusEnum.ALARM_PUSH_STATUS_PRODUCT;
        Long historyAlarmMessageId = null;
        //获取历史报警消息id,及推送状态
        if (baseRule.getIsAlarm().get()) {
            //已经在报警了，那么去内存里拿最新的报警id再将历史报警的id获取出来

            Map<String, AlarmMessage> alarmMap = getAlarmList(session);//(Map<String, AlarmMessage>) session.getAttribute(SessionContextEnum.SESSIONCONTEXT_ALARMLIST.getCode());
            AlarmMessage alreadyExistAlarmMessage = Optional.ofNullable(alarmMap).map(m -> m.get(getAlarmId(baseRule))).orElseGet(() -> null);
            if (null != alreadyExistAlarmMessage) {
                AlarmHistory existAlarmHistory = alarmHistoryMapperImp.getById(alreadyExistAlarmMessage.getAlarmHistoryId());
                if (!ObjectUtils.isEmpty(existAlarmHistory)) {
                    historyPushStatus = existAlarmHistory.getPushStatus();
                    historyAlarmMessageId = existAlarmHistory.getId();
                }

            } else {
                //内存中没有报警，那么匹配历史的对应报警规则产生的最新报警消息
                AlarmHistory existAlarmHistory = alarmHistoryMapperImp.getLastAlatmHistoryByNodeTag(baseRule.getId());
                if (!ObjectUtils.isEmpty(existAlarmHistory)) {
                    historyPushStatus = existAlarmHistory.getPushStatus();
                    historyAlarmMessageId = existAlarmHistory.getId();
                }
            }
        } else {
            //在没在报警，这是新产生的报警
            AlarmHistory newAlarmHistory = saveNewestAlarm(baseRule);
            if (!ObjectUtils.isEmpty(newAlarmHistory)) {
                historyAlarmMessageId = newAlarmHistory.getId();
            }
        }
        AlarmMessage alarmMessage =
                AlarmMessage.builder()
                        .context(baseRule.getPushAudioContext())
                        .date(new Date())
                        .level(0L)
                        .product(baseRule.getAlarmGroup())
                        .rate(0.0)
                        .value(baseRule.getValue())
                        .alarmId(getAlarmId(baseRule))
                        .pushStatus(historyPushStatus)
                        .alarmHistoryId(historyAlarmMessageId)
                        .build();

        Map<String, AlarmMessage> alarmMap = SessionListener.getAlarmList(session);//Map<String, AlarmMessage>) session.getAttribute(SessionContextEnum.SESSIONCONTEXT_ALARMLIST.getCode());
        //跟替换更新，等到不报警的时候在将其移除
        if (null != alarmMap && (alarmMap instanceof Map)) {
            alarmMap.put(getAlarmId(baseRule), alarmMessage);
            log.debug("add alarm list");
        }
    }

    /**
     * 语音列表操作
     */
    public void audioListOperate(BaseRule baseRule) {
        if (!baseRule.getIsAudio()) {
            return;
        }
        if (!CollectionUtils.isEmpty(httpSessionMap)) {
            httpSessionMap.values().forEach(session -> {
                Map<String, LocalDateTime> sessionAudioTimeMapp = getAudioPushTimeList(session);//(Map<String, LocalDateTime>) session.getAttribute(SessionContextEnum.SESSIONCONTEXT_AUDIOPUSHLASTTIME.getCode());
                if (null == sessionAudioTimeMapp) {
                    return;
                }
                LocalDateTime audioPushTime = Optional.of(sessionAudioTimeMapp).map(sessionAudioTimeMap -> sessionAudioTimeMap.get(getAlarmId(baseRule))).orElseGet(() -> null);

                if (audioPushTime != null) {
                    List<String> wxconfig = Arrays.asList(SysConfigEnum.SYS_CONFIG_pushIntervalSec.getCode());
                    List<SystemConfig> systemConfigs = systemConfigMapperImp.list(Wrappers.<SystemConfig>lambdaQuery().in(SystemConfig::getCode, wxconfig));
                    Map<String, SystemConfig> systemConfigMap = systemConfigs.stream().collect(Collectors.toMap(SystemConfig::getCode, p -> p, (o, n) -> n));


                    if (audioPushTime.plus(Long.parseLong(systemConfigMap.get(SysConfigEnum.SYS_CONFIG_pushIntervalSec.getCode()).getValue()), ChronoUnit.SECONDS).isBefore(LocalDateTime.now())) {
                        addAudioMessage(baseRule, session, sessionAudioTimeMapp);
                        sessionAudioTimeMapp.put(getAlarmId(baseRule), LocalDateTime.now());
                    } else {
                        log.debug("should audio but not time,laster={} now={},interval={}", audioPushTime, LocalDateTime.now(), systemConfigMap.get(SysConfigEnum.SYS_CONFIG_pushIntervalSec.getCode()).getValue());
                    }
                } else {
                    //上一次报警时间为null，则直接进行报警
                    addAudioMessage(baseRule, session, sessionAudioTimeMapp);
                    sessionAudioTimeMapp.put(getAlarmId(baseRule), LocalDateTime.now());
                }

            });
        }

    }

    private void addAudioMessage(BaseRule baseRule, HttpSession session, Map<String, LocalDateTime> sessionAudioTimeMapp) {
        AlarmMessage alarmMessage = AlarmMessage.builder()
                .context(baseRule.getPushAudioContext())
                .date(new Date())
                .level(0L)
                .product(baseRule.getAlarmGroup())
                .rate(0.0)
                .value(baseRule.getValue())
                .build();
        Map<String, AlarmMessage> audioList = getAudioList(session);
        if (audioList != null) {
            synchronized (audioList) {
                audioList.put(getAlarmId(baseRule), alarmMessage);
                log.debug("add audio list");
            }
        }
        sessionAudioTimeMapp.put(getAlarmId(baseRule), LocalDateTime.now());
    }

    /**
     * 持续报警处理
     */
    public void continueAlarmAudioOperate(LimitRule limitRule) {
        List<String> wxconfig = Arrays.asList(SysConfigEnum.SYS_CONFIG_pushIntervalSec.getCode(), SysConfigEnum.SYS_CONFIG_department.getCode(), SysConfigEnum.SYS_CONFIG_url.getCode(), SysConfigEnum.SYS_CONFIG_continueAlarmSec.getCode());
        List<SystemConfig> systemConfigs = systemConfigMapperImp.list(Wrappers.<SystemConfig>lambdaQuery().in(SystemConfig::getCode, wxconfig));
        Map<String, SystemConfig> systemConfigMap = systemConfigs.stream().collect(Collectors.toMap(SystemConfig::getCode, p -> p, (o, n) -> n));
        //持续报警时，间隔报警推送
        Long continueAlarmSec = AlarmRuleService.INFTY_ALARM_INTERVAL.equals(limitRule.getAlarmInterval()) ? Long.MAX_VALUE : Long.parseLong(limitRule.getAlarmInterval());
        //已经在报警状态了，判断持续时间是否超过，超过则进行报警,这里不记录上次报警时间
        if (4 == systemConfigMap.size()  /*wxPushConfig.getContinueAlarmSec()*/) {
            //报警开始时间持续到现在的时间是不是大于设定的持续报警时间
            if (
                    Duration.between(limitRule.getBegionAlarmTime(), LocalDateTime.now()).getSeconds()
                            < continueAlarmSec
            ) {
                return;
            }
            //微信推送
            if (limitRule.getIsWxPush()) {
                executorService.execute(() -> {
                    WXPushTools.sendwx(systemConfigMap.get(SysConfigEnum.SYS_CONFIG_url.getCode()).getValue()/*wxPushConfig.getUrl()*/, limitRule.getPushWXContext(), systemConfigMap.get(SysConfigEnum.SYS_CONFIG_department.getCode()).getValue() /*wxPushConfig.getDepartment()*/);
                });
            }
            //语音报警
            if (limitRule.getIsAudio()) {
                if (!CollectionUtils.isEmpty(httpSessionMap)) {
                    httpSessionMap.values().forEach(session -> {
                        Map<String, AlarmMessage> audioAlarmMap = getAudioList(session);
                        if (null == audioAlarmMap) {
                            return;
                        }
                        synchronized (audioAlarmMap) {
                            AlarmMessage alarmMessage = AlarmMessage.builder()
                                    .context(limitRule.getPushAudioContext())
                                    .date(new Date())
                                    .level(0L)
                                    .product(limitRule.getAlarmGroup())
                                    .rate(0.0)
                                    .value(limitRule.getValue())
                                    .build();
                            audioAlarmMap.put(getAlarmId(limitRule), alarmMessage);
                            log.debug("add continue audio list");
                            //推送完成以后重置下开始报警时间
                            limitRule.setBegionAlarmTime(LocalDateTime.now());
                        }
                    });
                }
            }

        }
    }

    public void wxPushOperate(BaseRule triggerRule) {
        if (!triggerRule.getIsWxPush()) {
            return;
        }
        //获取系统微信推送配置
        List<String> wxconfig = Arrays.asList(SysConfigEnum.SYS_CONFIG_pushIntervalSec.getCode(), SysConfigEnum.SYS_CONFIG_department.getCode(), SysConfigEnum.SYS_CONFIG_url.getCode());
        List<SystemConfig> systemConfigs = systemConfigMapperImp.list(Wrappers.<SystemConfig>lambdaQuery().in(SystemConfig::getCode, wxconfig));
        Map<String, SystemConfig> systemConfigMap = systemConfigs.stream().collect(Collectors.toMap(SystemConfig::getCode, p -> p, (o, n) -> n));

        if (triggerRule.getIsWxPush() && systemConfigMap.size() == 3) {
            if (ObjectUtils.isEmpty(triggerRule.getPushWXLastTime()) || triggerRule.getPushWXLastTime().plus(Long.parseLong(systemConfigMap.get(SysConfigEnum.SYS_CONFIG_pushIntervalSec.getCode()).getValue())/*wxPushConfig.getPushIntervalSec()*/, ChronoUnit.SECONDS).isBefore(LocalDateTime.now())) {
                executorService.execute(() -> {
                    WXPushTools.sendwx(systemConfigMap.get(SysConfigEnum.SYS_CONFIG_url.getCode()).getValue()/*wxPushConfig.getUrl()*/, triggerRule.getPushWXContext(), systemConfigMap.get(SysConfigEnum.SYS_CONFIG_department.getCode()).getValue() /*wxPushConfig.getDepartment()*/);
                });
                triggerRule.setPushWXLastTime(LocalDateTime.now());
            }
        }
    }


    public void defaultNoAlarmHandle(BaseRule baseRule) {
        //不报警，那么需要进行web报警画面消除
        //现在达到报警状态了
        httpSessionMap.values().forEach(session -> {
            Map<String, AlarmMessage> AlarmMap = getAlarmList(session);
            if (null == AlarmMap) {
                return;
            }
            AlarmMessage alarmMessage = AlarmMap.get(getAlarmId(baseRule));
            if (alarmMessage != null) {
                if (getAlarmId(baseRule).equals(alarmMessage.getAlarmId())) {
                    AlarmMap.remove(getAlarmId(baseRule));
                }
            }
        });
        if (baseRule.getIsAlarm().get()) {
            log.debug("alarm recover");
        }
        baseRule.getIsAlarm().set(false);
    }

    public void removeBolishContext(BaseRule baseRule) {
        httpSessionMap.values().forEach(session -> {
            Map<String, AlarmMessage> alarmList = getAlarmList(session);
            if (null != alarmList) {
                alarmList.remove(getAlarmId(baseRule));
            }

            Map<String, AlarmMessage> audioList = getAudioList(session);
            if (null != audioList) {
                audioList.remove(getAlarmId(baseRule));
            }

            Map<String, LocalDateTime> audioPushTimeList = getAudioPushTimeList(session);
            if (null != audioPushTimeList) {
                audioPushTimeList.remove(getAlarmId(baseRule));
            }
        });
    }


    private AlarmHistory saveNewestAlarm(BaseRule triggerRule) {
        AlarmHistory newAlarmHistory = null;
        //原先不报警
        if (!triggerRule.getIsAlarm().get()) {
            //之前没发送报警，保存到数据库中
            newAlarmHistory = new AlarmHistory();
            newAlarmHistory.setAlarmContext(triggerRule.getPushWXContext());
            newAlarmHistory.setCreateTime(new Date());
            newAlarmHistory.setRefAlarmRuleId(triggerRule.getId());
            newAlarmHistory.setPushStatus(AlarmPushStatusEnum.ALARM_PUSH_STATUS_PRODUCT);
            try {
                alarmHistoryMapperImp.save(newAlarmHistory);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        return newAlarmHistory;
    }

    public static String getAlarmId(BaseRule baseRule) {
        return baseRule.getIotNode() + JudgementService.DELIMITER + baseRule.getTag() + baseRule.getAlarmMode() + baseRule.getAlarmSubMode();
    }

    public static String getShortAlarmId(BaseRule baseRule) {
        return baseRule.getIotNode() + JudgementService.DELIMITER + baseRule.getTag();
    }


}
