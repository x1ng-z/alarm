package group.yzhs.alarm.model.dto.alarm;

import com.alibaba.fastjson.annotation.JSONField;
import group.yzhs.alarm.config.FastJsonEnumDeserializerAndSerializerConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotBlank;

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
public class AlarmRuleDto {
    @JSONField(serializeUsing = FastJsonEnumDeserializerAndSerializerConfig.LongToStringSerializer.class)
    private Long id;
    @NotNull(message = "alarmMode 不能为空")
    private String alarmMode;
    @NotNull(message = "alarmSubMode 不能为空")
    private String alarmSubMode;

    private Boolean isAudio;
    @JSONField(serializeUsing = FastJsonEnumDeserializerAndSerializerConfig.LongToStringSerializer.class)
    private Long alarmClassId;
    @NotNull(message = "pointId 不能为空")
    @JSONField(serializeUsing = FastJsonEnumDeserializerAndSerializerConfig.LongToStringSerializer.class)
    private Long pointId;
    //'报警组：ktj，sl，sc，zc
//    @JSONField(deserializeUsing = FastJsonEnumDeserializerAndSerializerConfig.FastJsonEnumDeserializer.class,serializeUsing = FastJsonEnumDeserializerAndSerializerConfig.FastJsonEnumSerializer.class)
    @NotNull(message = "group 不能为空")
    private String alarmGroup;
    //    @NotNull(message = "limiteValue 不能为空")
    private BigDecimal limiteValue;
    @NotNull(message = "alarmTemple 不能为空")
    private String alarmTemple;
    @NotNull(message = "是否微信推送不能为空")
    private Boolean isWxPush;
    @NotBlank(message = "alarmInterval is null")
    private String alarmInterval;
}
