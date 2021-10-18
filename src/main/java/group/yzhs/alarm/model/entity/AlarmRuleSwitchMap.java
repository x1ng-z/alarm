package group.yzhs.alarm.model.entity;

import lombok.Data;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/10/16 12:27
 */
@Data
public class AlarmRuleSwitchMap extends BaseEntity {
    private Long refAlarmRuleId;
    private Long refSwitchId;
}
