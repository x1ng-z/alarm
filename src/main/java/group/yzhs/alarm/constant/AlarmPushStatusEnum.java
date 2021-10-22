package group.yzhs.alarm.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/10/19 8:31
 */
@Getter
public enum AlarmPushStatusEnum {
    ALARM_PUSH_STATUS_PRODUCT(1,"报警生成"),
    ALARM_PUSH_STATUS_AUDIO(2,"设备语音报警"),
    ALARM_PUSH_STATUS_PUSHED(3,"推送设备健康");

    @EnumValue
    private Integer code;
    private String desc;

    AlarmPushStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }


    public static final Map<Integer,AlarmPushStatusEnum> STRING_ALARM_MODEL_ENUM_MAP;

    static {
        STRING_ALARM_MODEL_ENUM_MAP=new HashMap<>();
        Arrays.stream(AlarmPushStatusEnum.values()).forEach(a->{
            STRING_ALARM_MODEL_ENUM_MAP.put(a.getCode(),a);
        });
    }
}
