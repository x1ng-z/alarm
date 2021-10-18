package group.yzhs.alarm.model.dto.alarm;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/10/16 22:15
 */
@Data
public class AlarmRuleSwitchMapDto {
    private Long id;
    @NotNull(message = "refAlarmRuleId为空")
    private Long refAlarmRuleId;
    @NotNull(message = "refSwitchId为空")
    private Long refSwitchId;
}
