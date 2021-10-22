package group.yzhs.alarm.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/10/21 14:42
 * 工艺分类
 */
@Getter
public enum ProcessEnum {
    PROCESS_SL("sl","生料"),
    PROCESS_SC("sc","烧成"),
    PROCESS_ZC("zc","制成")
    ;

    @EnumValue
    private String code;
    private String desc;

    ProcessEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static final Map<String,ProcessEnum> PROCESS_ENUM_MAP;

    static {
        PROCESS_ENUM_MAP=new HashMap<>();
        Arrays.stream(ProcessEnum.values()).forEach(a->{
            PROCESS_ENUM_MAP.put(a.getCode(),a);
        });
    }
}
