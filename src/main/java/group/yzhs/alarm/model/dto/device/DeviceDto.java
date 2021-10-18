package group.yzhs.alarm.model.dto.device;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/10/16 10:09
 */
@Data
public class DeviceDto {
    private Long id;
    private String deviceNo;
    @NotNull(message = "设备名称不能为空")
    private String deviceName;
}
