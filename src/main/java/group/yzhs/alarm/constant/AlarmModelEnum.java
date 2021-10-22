package group.yzhs.alarm.constant;

import com.alibaba.fastjson.annotation.JSONType;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import group.yzhs.alarm.config.FastJsonEnumDeserializerAndSerializerConfig;
import lombok.Getter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/6/15 11:10
 */
@Getter
public enum AlarmModelEnum {
    ALARMMODEL_TRIG("延边触发报警", "TRIG"),
    ALARMMODEL_LIM("限制报警","LIM")
    ;


    AlarmModelEnum(String name, String code) {
        this.name = name;
        this.code = code;
    }



    public static final Map<String,AlarmModelEnum> ALARM_MODEL_ENUM_MAP;

    static {
        ALARM_MODEL_ENUM_MAP=new HashMap<>();
        Arrays.stream(AlarmModelEnum.values()).forEach(a->{
            ALARM_MODEL_ENUM_MAP.put(a.getCode(),a);
        });
    }

    private String name;

//    @JsonValue
    public String getCode() {
        return code;
    }

    @EnumValue
    private String code;

//
//    @JsonCreator
//    public static AlarmModelEnum getByCode(String code) {
//        for (AlarmModelEnum value : AlarmModelEnum.values()) {
//            if (value.getCode().equals(code)) {
//                return value;
//            }
//        }
//        return null;
//    }
}
