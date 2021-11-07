package group.yzhs.alarm.model.dto.alarm;

import com.alibaba.fastjson.annotation.JSONField;
import group.yzhs.alarm.config.FastJsonEnumDeserializerAndSerializerConfig;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/10/16 22:15
 */
@Data
public class AlarmRuleSwitchMapDto {
    @JSONField(serializeUsing = FastJsonEnumDeserializerAndSerializerConfig.LongToStringSerializer.class)
    private Long id;

    @JSONField(serializeUsing = FastJsonEnumDeserializerAndSerializerConfig.LongToStringSerializer.class)
    @NotNull(message = "refAlarmRuleId为空")
    private Long refAlarmRuleId;

    @JSONField(serializeUsing = FastJsonEnumDeserializerAndSerializerConfig.LongToStringSerializer.class)
    @NotNull(message = "refSwitchId为空")
    private Long refSwitchId;
}
