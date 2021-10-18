package group.yzhs.alarm.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/10/16 9:05
 */
public enum DeviceSwitchRuleEnum {
    SWITCH_RULE_GE("ge","大于等于"),
    SWITCH_RULE_LE("le","小于等于"),
    SWITCH_RULE_GT("gt","大于"),
    SWITCH_RULE_LT("lt","小于"),
    SWITCH_RULE_EQ("eq","等于");
    ;
    @EnumValue
//    @JsonValue
    private String code;
    private String desc;

    DeviceSwitchRuleEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
