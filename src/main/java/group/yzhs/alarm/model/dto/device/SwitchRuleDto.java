package group.yzhs.alarm.model.dto.device;

import group.yzhs.alarm.constant.DeviceSwitchRuleEnum;
import group.yzhs.alarm.model.entity.BaseEntity;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/10/16 12:25
 */
@Data
public class SwitchRuleDto {
    private Long id;
    @NotNull(message = "开关id不能为空")
    private Long refSwitchId;
    @NotNull(message = "规则编码不能为空")
    private DeviceSwitchRuleEnum ruleCode;
    @NotNull(message = "点位id不能为空")
    private Long pointId;
    @NotNull(message = "限制值不能为空")
    private BigDecimal limitValue;
}
