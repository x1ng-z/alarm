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


        //???????????????????????????

        //??????????????????/??????

        log.debug(" TRG context:{}", triggerRule.getPushWXContext());
        sessionListener.getHttpSessionMap().values().forEach(session -> {
            sessionListener.alarmListOperate(session, triggerRule);
        });


        //???????????????????????????????????????????????????????????????????????????????????????
        if (!triggerRule.getIsAlarm().get()) {

            //????????????
            if (triggerRule.getIsWxPush() || (triggerRule.getIsAudio())) {
                //????????????
                sessionListener.wxPushOperate(triggerRule);
                //????????????
                sessionListener.audioListOperate(triggerRule);
            }

            triggerRule.getIsAlarm().set(true);
        }

    }


    public void defaultNoAlarmHandle(BaseRule triggerRule) {
        //??????????????????
        //Point point =pointMapperImp.getById(triggerRule.getPointId());
        //??????????????????????????????web??????????????????
        sessionListener.defaultNoAlarmHandle(triggerRule);
    }


}
