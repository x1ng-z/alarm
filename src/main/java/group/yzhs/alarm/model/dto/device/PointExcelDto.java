package group.yzhs.alarm.model.dto.device;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/10/16 10:13
 */
@Data
public class PointExcelDto {
    private String tag;
    private String name;
    private String nodeCode;
    private String refDevice;

}
