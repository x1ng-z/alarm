package group.yzhs.alarm.model.dto.device;

import group.yzhs.alarm.model.entity.BaseEntity;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/10/16 10:11
 */
@Data
public class SwitchDto  {
    private Long id;
    @NotNull(message = "开关名称不为空")
    private String name;
}
