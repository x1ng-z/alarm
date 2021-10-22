package group.yzhs.alarm.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/10/16 9:05
 */
@Getter
public enum DeviceSwitchRuleEnum {
    SWITCH_RULE_GE("ge","大于等于"),
    SWITCH_RULE_LE("le","小于等于"),
    SWITCH_RULE_GT("gt","大于"),
    SWITCH_RULE_LT("lt","小于"),
    SWITCH_RULE_EQ("eq","等于");

    //    @JsonValue
    @EnumValue
    private final String code;

    private final String desc;

    public final static Map<String,DeviceSwitchRuleEnum> deviceSwitchRuleEnumMapping;
    
    static {
        deviceSwitchRuleEnumMapping =new HashMap<>();
        Arrays.stream(DeviceSwitchRuleEnum.values()).forEach(d->{
            deviceSwitchRuleEnumMapping.put(d.getCode(),d);
        });
    }

    DeviceSwitchRuleEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
