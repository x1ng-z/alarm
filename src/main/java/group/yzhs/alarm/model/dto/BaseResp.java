package group.yzhs.alarm.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/10/16 12:54
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class BaseResp {
    private Long status;
    private String message;
    private Object data;
}
