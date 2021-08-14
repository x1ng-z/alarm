package group.yzhs.alarm.model.dto.iot;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/7/27 9:43
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IotReadNodeInfo implements Serializable {
     private String node;
     private String measurePoint;
     private Object value;
     private Long time;
}
