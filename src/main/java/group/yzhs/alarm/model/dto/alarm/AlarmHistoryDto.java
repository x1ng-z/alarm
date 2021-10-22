package group.yzhs.alarm.model.dto.alarm;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;
import group.yzhs.alarm.config.FastJsonEnumDeserializerAndSerializerConfig;
import group.yzhs.alarm.constant.AlarmPushStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/10/16 18:57
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AlarmHistoryDto {
    private Long id;
    @NotNull(message="报警内容不能为空")
    private String alarmContext;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date alarmTime;
    @JSONField(deserializeUsing = FastJsonEnumDeserializerAndSerializerConfig.FastJsonEnumDeserializer.class,serializeUsing = FastJsonEnumDeserializerAndSerializerConfig.FastJsonEnumSerializer.class)
    private AlarmPushStatusEnum pushStatus;
    private String deviceNo;
    @NotNull(message = "报警规则id不能为空")
    private Long refAlarmRuleId;
}
