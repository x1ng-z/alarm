package group.yzhs.alarm.model.dto.alarm;

import com.alibaba.fastjson.annotation.JSONField;
import group.yzhs.alarm.config.FastJsonEnumDeserializerAndSerializerConfig;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/10/16 12:57
 */
@Data
public class AlarmClassDto {
    @JSONField(serializeUsing = FastJsonEnumDeserializerAndSerializerConfig.LongToStringSerializer.class)
    private Long id;
    @NotNull(message="报警类别名称不能为空")
    private String name;
    @NotNull(message="报警编码不能为空")
    private String code;
}
