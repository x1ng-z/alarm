package group.yzhs.alarm.model.dto.device;

import com.alibaba.fastjson.annotation.JSONField;
import group.yzhs.alarm.config.FastJsonEnumDeserializerAndSerializerConfig;
import group.yzhs.alarm.constant.DeviceSwitchRuleEnum;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/10/16 12:25
 */
@Data
public class SwitchRuleExcelDto {
    @JSONField(serializeUsing = FastJsonEnumDeserializerAndSerializerConfig.LongToStringSerializer.class)
    private String id;
    private String refSwitch;//switchname;
    private String ruleCode;//desc=code
    private String point;//node_code=tag
    private BigDecimal limitValue;
}
