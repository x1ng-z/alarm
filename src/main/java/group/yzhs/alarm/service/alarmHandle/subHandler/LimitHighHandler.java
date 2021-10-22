package group.yzhs.alarm.service.alarmHandle.subHandler;

import group.yzhs.alarm.config.WXPushConfig;
import group.yzhs.alarm.constant.AlarmPushStatusEnum;
import group.yzhs.alarm.constant.LimiteModelEnum;
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
public class LimitHighHandler extends BaseLimitHander {
    private SessionListener sessionListener;

    private WXPushConfig wxPushConfig;

    private PointMapperImp pointMapperImp;

    private AlarmHistoryMapperImp alarmHistoryMapperImp;

    public LimitHighHandler(SessionListener sessionListener, WXPushConfig wxPushConfig, PointMapperImp pointMapperImp, AlarmHistoryMapperImp alarmHistoryMapperImp) {
        super(sessionListener, wxPushConfig, pointMapperImp, alarmHistoryMapperImp);
        this.sessionListener = sessionListener;
        this.wxPushConfig = wxPushConfig;
        this.pointMapperImp = pointMapperImp;
        this.alarmHistoryMapperImp = alarmHistoryMapperImp;
    }

    @Override
    public String getCode() {
        return LimiteModelEnum.LIMIT_HIGH.getCode();
    }

    @Override
    public boolean judge(BaseRule rule) {
        LimitRule limitRule = (LimitRule) rule;
        return (limitRule.getValue() > limitRule.getLimiteValue().doubleValue());
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

    @Override
    public void alarmHandle(LimitRule limitRule) {
        defaultAlarmHandle(limitRule);
    }

    @Override
    public void noAlarmHandle(LimitRule limitRule) {
defaultNoAlarmHandle(limitRule);
    }
}
