package group.yzhs.alarm.model.entity;

import lombok.Data;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/10/16 10:04
 */
@Data
public class AlarmClass extends BaseEntity {
    private Long id;
    private String name;
    private String code;
}
