package group.yzhs.alarm.service.alarmHandle;

import group.yzhs.alarm.model.rule.BaseRule;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/8/13 13:14
 */
public interface SubHandler {
    String getCode();
    void handle(BaseRule rule);
    boolean judge(BaseRule rule);
}
