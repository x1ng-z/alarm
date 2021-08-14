package group.yzhs.alarm.model.dto.view;

import group.yzhs.alarm.model.AlarmMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.List;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/8/13 10:20
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AlarmDto {
    private Collection<AlarmMessage> data;
    private String message;
    private Integer status;
    private Integer size;
}
