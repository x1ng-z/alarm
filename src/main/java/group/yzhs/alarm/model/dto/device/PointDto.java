package group.yzhs.alarm.model.dto.device;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/10/16 10:13
 */
@Data
public class PointDto {
    private Long id;
    @NotNull(message = "位号名称不能为空")
    private String tag;
    @NotNull(message = "位号注释不能为空")
    private String name;
    @NotNull(message = "引用的设备id不能为空")
    private Long refDeviceId;
}
