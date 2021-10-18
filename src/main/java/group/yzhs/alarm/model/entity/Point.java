package group.yzhs.alarm.model.entity;

import lombok.Data;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/10/16 10:13
 */
@Data
public class Point extends BaseEntity {
    private String tag;
    private String name;
    private Long refDeviceId;
}
