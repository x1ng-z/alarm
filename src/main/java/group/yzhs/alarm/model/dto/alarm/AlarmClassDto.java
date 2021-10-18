package group.yzhs.alarm.model.dto.alarm;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/10/16 12:57
 */
@Data
public class AlarmClassDto {
    private Long id;
    @NotNull(message="报警类别名称不能为空")
    private String name;
    @NotNull(message="报警编码不能为空")
    private String code;
}
