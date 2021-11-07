package group.yzhs.alarm.model.dto.alarm;

import com.alibaba.fastjson.annotation.JSONField;
import group.yzhs.alarm.config.FastJsonEnumDeserializerAndSerializerConfig;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/10/16 10:19
 */
@Data
public class SystemConfigDto {
    @JSONField(serializeUsing = FastJsonEnumDeserializerAndSerializerConfig.LongToStringSerializer.class)
    private Long id;
    private String name ;
    private String code ;
    @NotNull(message = "属性值不能为空")
    private String value ;
    private String configGroup;//'属性组别'
}
