package group.yzhs.alarm.constant;

import lombok.Getter;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/6/15 15:15
 */
@Getter
public enum TrigerModelEnum {

    TRIGER_RISE("RISE", "上升沿"),
    TRIGER_FALL("FALL", "下降沿");


    TrigerModelEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    private String code;
    private String name;
}
