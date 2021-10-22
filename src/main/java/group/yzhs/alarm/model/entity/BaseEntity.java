package group.yzhs.alarm.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/10/16 10:05
 */
@Data
public class BaseEntity implements Serializable {
    private Boolean deleted;
    @TableId
    private Long id;
}
