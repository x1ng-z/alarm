package group.yzhs.alarm.model.rule.limit;

import group.yzhs.alarm.model.rule.BaseRule;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/6/15 11:20
 */
@Data
public class LimitRule extends BaseRule {
//    private double limitValue;
    private LocalDateTime begionAlarmTime;//开始报警时间
}
