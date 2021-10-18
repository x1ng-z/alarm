package group.yzhs.alarm.model.entity;

import lombok.Data;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/10/16 10:19
 */
@Data
public class SystemConfig extends BaseEntity {
    private String name ;
    private String code ;
    private String value ;
     private String group;//'属性组别'
}
