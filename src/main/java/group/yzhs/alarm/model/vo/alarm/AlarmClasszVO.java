package group.yzhs.alarm.model.vo.alarm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/10/16 17:30
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AlarmClasszVO {

    private String name;
    private String code;
}
