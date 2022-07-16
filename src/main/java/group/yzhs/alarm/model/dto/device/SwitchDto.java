package group.yzhs.alarm.model.dto.device;

import com.alibaba.fastjson.annotation.JSONField;
import group.yzhs.alarm.config.FastJsonEnumDeserializerAndSerializerConfig;
import group.yzhs.alarm.constant.SwitchLogic;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/10/16 10:11
 */
@Data
public class SwitchDto {
    @JSONField(serializeUsing = FastJsonEnumDeserializerAndSerializerConfig.LongToStringSerializer.class)
    private Long id;
    @NotNull(message = "开关名称不为空")
    private String name;
    @NotNull(message = "switchLogic is null")
    @JSONField(deserializeUsing = FastJsonEnumDeserializerAndSerializerConfig.FastJsonEnumDeserializer.class, serializeUsing = FastJsonEnumDeserializerAndSerializerConfig.FastJsonEnumSerializer.class)
    private SwitchLogic switchLogic;
}
