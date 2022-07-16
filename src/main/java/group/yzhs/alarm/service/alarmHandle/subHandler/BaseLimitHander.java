package group.yzhs.alarm.service.alarmHandle.subHandler;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import group.yzhs.alarm.config.AlarmHistoryArchiveConfig;
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

    public BaseLimitHander(
            SessionListener sessionListener,
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
        sessionListener.getHttpSessionMap().values().forEach(session -> {
            sessionListener.alarmListOperate(session, limitRule);
        });
        log.debug(Thread.currentThread() + " LIMIT  context:{}", limitRule.getPushWXContext());
        log.info("*****session size={}", sessionListener.getHttpSessionMap().size());
        if (!limitRule.getIsAlarm().get()) {
            limitRule.setBegionAlarmTime(LocalDateTime.now());
            //微信推送
            if (limitRule.getIsWxPush() || (limitRule.getIsAudio())) {
                //微信推送
                sessionListener.wxPushOperate(limitRule);
                //语音报警
                sessionListener.audioListOperate(limitRule);
            }
            limitRule.getIsAlarm().set(true);
        }
        sessionListener.continueAlarmAudioOperate(limitRule);
    }

    public void defaultNoAlarmHandle(LimitRule limitRule) {
        //不报警，那么需要进行web报警画面消除
        //现在达到报警状态了
        sessionListener.defaultNoAlarmHandle(limitRule);
    }



}
