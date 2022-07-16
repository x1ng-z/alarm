package group.yzhs.alarm.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * @author zzx
 * @version 1.0
 * @date 2022/7/7 8:08
 */
@Getter
public enum SwitchLogic {

    SWITCH_LOGIC_AND("and","与"),
    SWITCH_LOGIC_OR("or","")
    ;
    SwitchLogic(String code, String name) {
        this.code = code;
        this.name = name;
    }

    @EnumValue
    private String code;
    private String name;

}
