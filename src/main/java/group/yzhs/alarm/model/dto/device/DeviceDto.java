package group.yzhs.alarm.model.dto.device;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;
import group.yzhs.alarm.config.FastJsonEnumDeserializerAndSerializerConfig;
import group.yzhs.alarm.constant.ProcessEnum;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/10/16 10:09
 */
@Data
public class DeviceDto {
//    @JsonFormat(shape = JsonFormat.Shape.STRING)
@JSONField(serializeUsing = FastJsonEnumDeserializerAndSerializerConfig.LongToStringSerializer.class)
private Long id;
    private String deviceNo;
    @NotNull(message = "设备名称不能为空")
    private String deviceName;

//    @JSONField(deserializeUsing = FastJsonEnumDeserializerAndSerializerConfig.FastJsonEnumDeserializer.class,serializeUsing = FastJsonEnumDeserializerAndSerializerConfig.FastJsonEnumSerializer.class)
    @NotNull(message = "设备分类不能为空")
    private String process;
}
