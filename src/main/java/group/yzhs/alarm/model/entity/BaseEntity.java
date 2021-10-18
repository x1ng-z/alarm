package group.yzhs.alarm.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/10/16 10:05
 */
@Data
public class BaseEntity {
    private Boolean deleted;
    @TableId
    private Long id;
}
