package group.yzhs.alarm.model.dto.iot;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/8/13 14:36
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class IotMeasurePointDto {

    private IotMeasurepointInfo data;
    private int status;
    private String message;
}
