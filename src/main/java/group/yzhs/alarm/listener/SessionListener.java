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
        //add alarm list and audio list??? alarm-list???key=tag
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
     * ??????????????????
     */
    public void alarmListOperate(HttpSession session, BaseRule baseRule) {
        //?????????
        AlarmPushStatusEnum historyPushStatus = AlarmPushStatusEnum.ALARM_PUSH_STATUS_PRODUCT;
        Long historyAlarmMessageId = null;
        //????????????????????????id,???????????????
        if (baseRule.getIsAlarm().get()) {
            //?????????????????????????????????????????????????????????id?????????????????????id????????????

            Map<String, AlarmMessage> alarmMap = getAlarmList(session);//(Map<String, AlarmMessage>) session.getAttribute(SessionContextEnum.SESSIONCONTEXT_ALARMLIST.getCode());
            AlarmMessage alreadyExistAlarmMessage = Optional.ofNullable(alarmMap).map(m -> m.get(getAlarmId(baseRule))).orElseGet(() -> null);
            if (null != alreadyExistAlarmMessage) {
                AlarmHistory existAlarmHistory = alarmHistoryMapperImp.getById(alreadyExistAlarmMessage.getAlarmHistoryId());
                if (!ObjectUtils.isEmpty(existAlarmHistory)) {
                    historyPushStatus = existAlarmHistory.getPushStatus();
                    historyAlarmMessageId = existAlarmHistory.getId();
                }

            } else {
                //??????????????????????????????????????????????????????????????????????????????????????????
                AlarmHistory existAlarmHistory = alarmHistoryMapperImp.getLastAlatmHistoryByNodeTag(baseRule.getId());
                if (!ObjectUtils.isEmpty(existAlarmHistory)) {
                    historyPushStatus = existAlarmHistory.getPushStatus();
                    historyAlarmMessageId = existAlarmHistory.getId();
                }
            }
        } else {
            //??????????????????????????????????????????
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
                        .limit(baseRule.getLimiteValue())
                        .build();

        Map<String, AlarmMessage> alarmMap = SessionListener.getAlarmList(session);//Map<String, AlarmMessage>) session.getAttribute(SessionContextEnum.SESSIONCONTEXT_ALARMLIST.getCode());
        //?????????????????????????????????????????????????????????
        if (null != alarmMap && (alarmMap instanceof Map)) {
            alarmMap.put(getAlarmId(baseRule), alarmMessage);
            log.debug("add alarm list");
        }
    }

    /**
     * ??????????????????
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
                    //????????????????????????null????????????????????????
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
     * ??????????????????
     */
    public void continueAlarmAudioOperate(LimitRule limitRule) {
        List<String> wxconfig = Arrays.asList(SysConfigEnum.SYS_CONFIG_pushIntervalSec.getCode(), SysConfigEnum.SYS_CONFIG_department.getCode(), SysConfigEnum.SYS_CONFIG_url.getCode(), SysConfigEnum.SYS_CONFIG_continueAlarmSec.getCode());
        List<SystemConfig> systemConfigs = systemConfigMapperImp.list(Wrappers.<SystemConfig>lambdaQuery().in(SystemConfig::getCode, wxconfig));
        Map<String, SystemConfig> systemConfigMap = systemConfigs.stream().collect(Collectors.toMap(SystemConfig::getCode, p -> p, (o, n) -> n));
        //????????????????????????????????????
        Long continueAlarmSec = AlarmRuleService.INFTY_ALARM_INTERVAL.equals(limitRule.getAlarmInterval()) ? Long.MAX_VALUE : Long.parseLong(limitRule.getAlarmInterval());
        //?????????????????????????????????????????????????????????????????????????????????,?????????????????????????????????
        if (4 == systemConfigMap.size()  /*wxPushConfig.getContinueAlarmSec()*/) {
            //????????????????????????????????????????????????????????????????????????????????????
            if (
                    Duration.between(limitRule.getBegionAlarmTime(), LocalDateTime.now()).getSeconds()
                            < continueAlarmSec
            ) {
                return;
            }
            //????????????
            if (limitRule.getIsWxPush()) {
                executorService.execute(() -> {
                    WXPushTools.sendwx(systemConfigMap.get(SysConfigEnum.SYS_CONFIG_url.getCode()).getValue()/*wxPushConfig.getUrl()*/, limitRule.getPushWXContext(), systemConfigMap.get(SysConfigEnum.SYS_CONFIG_department.getCode()).getValue() /*wxPushConfig.getDepartment()*/);
                });
            }
            //????????????
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
                                    .limit(limitRule.getLimiteValue())
                                    .build();
                            audioAlarmMap.put(getAlarmId(limitRule), alarmMessage);
                            log.debug("add continue audio list");
                            //?????????????????????????????????????????????
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
        //??????????????????????????????
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
        //??????????????????????????????web??????????????????
        //???????????????????????????
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

    public void removeAbolishContext(BaseRule baseRule) {
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
        //???????????????
        if (!triggerRule.getIsAlarm().get()) {
            //?????????????????????????????????????????????
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
