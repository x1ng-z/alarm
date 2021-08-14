package group.yzhs.alarm.model.dto.view;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/8/13 16:04
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AlarmSetDto {
    private String message;
    private int status;
    private AlarmSetInfo data;
}
