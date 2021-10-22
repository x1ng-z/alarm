package group.yzhs.alarm.model.dto.alarm;

import com.alibaba.fastjson.annotation.JSONField;
import group.yzhs.alarm.config.FastJsonEnumDeserializerAndSerializerConfig;
import group.yzhs.alarm.constant.ProductTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/10/16 10:15
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AlarmRuleExcelDto {
    private String id;
    private String  alarmMode    ;
    private String alarmSubMode ;
    private String alarmClass;
    private String point;
    //'报警组：ktj，sl，sc，zc
    private  String alarmGroup;
    private BigDecimal limiteValue;
    private String alarmTemple;
    private Boolean isAudio ;
    private Boolean isWxPush ;
    private String alarmSwitch;
}
