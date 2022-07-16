package group.yzhs.alarm.service.alarmHandle.subHandler;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import group.yzhs.alarm.config.WXPushConfig;
import group.yzhs.alarm.constant.SessionContextEnum;
import group.yzhs.alarm.constant.SysConfigEnum;
import group.yzhs.alarm.listener.SessionListener;
import group.yzhs.alarm.mapper.impl.AlarmHistoryMapperImp;
import group.yzhs.alarm.mapper.impl.PointMapperImp;
import group.yzhs.alarm.mapper.impl.SystemConfigMapperImp;
import group.yzhs.alarm.model.AlarmMessage;
import group.yzhs.alarm.model.entity.SystemConfig;
import group.yzhs.alarm.model.rule.BaseRule;
import group.yzhs.alarm.service.JudgementService;
import group.yzhs.alarm.service.alarmHandle.SubHandler;
import group.yzhs.alarm.utils.WXPushTools;
import lombok.extern.slf4j.Slf4j;
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
        this.systemConfigMapperImp = systemConfigMapperImp;
        this.executorService = executorService;
    }

    public abstract void alarmHandle(BaseRule triggerRule);

    public abstract void noAlarmHandle(BaseRule triggerRule);


    public void defaultAlarmHandle(BaseRule triggerRule) {


        //现在达到报警状态了

        //是否需要保存/查询

        log.debug(" TRG context:{}", triggerRule.getPushWXContext());
        sessionListener.getHttpSessionMap().values().forEach(session -> {
            sessionListener.alarmListOperate(session, triggerRule);
        });


        //之前不报警，新出现的报警是需要判断是否需要语音或微信推送的
        if (!triggerRule.getIsAlarm().get()) {

            //微信推送
            if (triggerRule.getIsWxPush() || (triggerRule.getIsAudio())) {
                //微信推送
                sessionListener.wxPushOperate(triggerRule);
                //语音报警
                sessionListener.audioListOperate(triggerRule);
            }

            triggerRule.getIsAlarm().set(true);
        }

    }


    public void defaultNoAlarmHandle(BaseRule triggerRule) {
        //点位信息查询
        //Point point =pointMapperImp.getById(triggerRule.getPointId());
        //不报警，那么需要进行web报警画面消除
        sessionListener.defaultNoAlarmHandle(triggerRule);
    }


}
