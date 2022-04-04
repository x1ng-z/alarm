package group.yzhs.alarm.model.dto.device;

import com.alibaba.fastjson.annotation.JSONField;
import group.yzhs.alarm.config.FastJsonEnumDeserializerAndSerializerConfig;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/10/16 10:13
 */
@Data
public class PointDto {
    @JSONField(serializeUsing = FastJsonEnumDeserializerAndSerializerConfig.LongToStringSerializer.class)
    private Long id;
    @NotNull(message = "位号名称不能为空")
    private String tag;
    @NotNull(message = "位号注释不能为空")
    private String name;
    @NotNull(message = "引用的设备id不能为空")
    private Long refDeviceId;
    @NotNull(message = "位号节点编码不能空")
    private String nodeCode;
}
