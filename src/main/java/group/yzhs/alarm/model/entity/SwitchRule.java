package group.yzhs.alarm.model.entity;

import group.yzhs.alarm.constant.DeviceSwitchRuleEnum;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/10/16 12:25
 */
@Data
public class SwitchRule extends BaseEntity {
    private Long refSwitchId;
    private DeviceSwitchRuleEnum ruleCode;
    private Long pointId;
    private BigDecimal limitValue;
}
