package group.yzhs.alarm.model.dto.iot.node;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/8/13 14:10
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NodeInfoDto {
    private String code;
    private String name;
    @JSONField(name="switch")
    private Boolean _switch;
}
