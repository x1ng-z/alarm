package group.yzhs.alarm.service.alarmHandle.subHandler;

import group.yzhs.alarm.config.WXPushConfig;
import group.yzhs.alarm.constant.TrigerModelEnum;
import group.yzhs.alarm.listener.SessionListener;
import group.yzhs.alarm.mapper.impl.AlarmHistoryMapperImp;
import group.yzhs.alarm.mapper.impl.PointMapperImp;
import group.yzhs.alarm.mapper.impl.SystemConfigMapperImp;
import group.yzhs.alarm.model.rule.BaseRule;
import group.yzhs.alarm.model.rule.trigger.TriggerRule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/8/13 13:10
 */
@Service
@Slf4j
public class RiseTrigerHandler extends BaseTrigerHandler {
    private SessionListener sessionListener;

    private WXPushConfig wxPushConfig;

    private PointMapperImp pointMapperImp;

    private AlarmHistoryMapperImp alarmHistoryMapperImp;

    private SystemConfigMapperImp systemConfigMapperImp;

    public RiseTrigerHandler(SessionListener sessionListener,
                             WXPushConfig wxPushConfig,
                             PointMapperImp pointMapperImp,
                             AlarmHistoryMapperImp alarmHistoryMapperImp,
                             SystemConfigMapperImp systemConfigMapperImp,
                             @Qualifier("http-wx-push-thread")
                                     ExecutorService executorService) {
        super(sessionListener, wxPushConfig, pointMapperImp, alarmHistoryMapperImp, systemConfigMapperImp, executorService);
        this.sessionListener = sessionListener;
        this.wxPushConfig = wxPushConfig;
        this.pointMapperImp = pointMapperImp;
        this.alarmHistoryMapperImp = alarmHistoryMapperImp;
        this.systemConfigMapperImp = systemConfigMapperImp;
    }

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
        if (!(rule instanceof TriggerRule)) {
            return;
        }
        TriggerRule triggerRule = (TriggerRule) rule;
        //?????????
        if (judge(triggerRule)) {
            alarmHandle(triggerRule);
        } else {
            noAlarmHandle(triggerRule);
        }
    }

    @Override
    public void alarmHandle(BaseRule triggerRule) {
        defaultAlarmHandle(triggerRule);
    }

    @Override
    public void noAlarmHandle(BaseRule triggerRule) {
        defaultNoAlarmHandle(triggerRule);
    }

}
