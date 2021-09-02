package group.yzhs.alarm.model.dto.view;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/8/13 16:05
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AlarmSetInfo {
    private float audioRate=1.0f;
    private String company;
    private String version;
}
