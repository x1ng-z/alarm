package group.yzhs.alarm.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/6/15 15:21
 */
@Getter
public enum LimiteModelEnum {

    LIMIT_HIGH("HI_LIM", "高限报警"),

    LIMIT_LOW("LO_LIM", "低限报警");


    LimiteModelEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }
    @EnumValue
//    @JsonValue
    private String code;
    private String name;

    public static final Map<String,LimiteModelEnum> LIMITE_MODEL_ENUM_MAP;

    static {
        LIMITE_MODEL_ENUM_MAP=new HashMap<>();
        Arrays.stream(LimiteModelEnum.values()).forEach(a->{
            LIMITE_MODEL_ENUM_MAP.put(a.getCode(),a);
        });
    }

}
