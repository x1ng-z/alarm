package group.yzhs.alarm.model.dto.alarm;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/10/29 13:36
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AlarmPushDto {
    @JSONField(name ="companycode" )
    @JsonProperty(value = "companycode")
    private String companyCode;

    @JsonProperty(value = "devicecode")
    @JSONField(name ="devicecode" )
    private String deviceCode;

    @JsonProperty(value = "risktype")
    @JSONField(name ="risktype" )
    private String riskType;

    @JsonProperty(value = "typecode")
    @JSONField(name ="typecode" )
    private String typeCode;

    @JsonProperty(value = "remarks")
    @JSONField(name ="remarks" )
    private String remarks;
}
