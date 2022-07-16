package group.yzhs.alarm.service.alarmHandle;

import group.yzhs.alarm.model.rule.BaseRule;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/6/15 14:27
 */
public interface Handler {
    String getCode();
    void handle(BaseRule rule);
}
