package group.yzhs.alarm.model.entity;

import group.yzhs.alarm.constant.ProcessEnum;
import lombok.Data;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/10/16 10:09
 */
@Data
public class Device extends BaseEntity {
    private String deviceNo;
    private String deviceName;
    private ProcessEnum process;
}
