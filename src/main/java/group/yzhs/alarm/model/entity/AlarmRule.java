package group.yzhs.alarm.model.entity;

import group.yzhs.alarm.constant.ProductTypeEnum;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/10/16 10:15
 */
@Data
public class AlarmRule extends BaseEntity{
    private String  alarmMode    ;
    private String alarmSubMode ;
    private Boolean isAudio ;
    private Long alarmClassId;
    private Long pointId;
    //'报警组：ktj，sl，sc，zc
    private String alarmGroup;
    private BigDecimal limiteValue;
    private String alarmTemple;
    private Boolean isWxPush ;
}
