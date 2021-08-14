package group.yzhs.alarm.constant;

import lombok.Data;
import lombok.Getter;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/8/13 11:05
 */
@Getter
public enum DataResourceEnum {

    DATA_RESOURCE_IOT("iot","iot取数"),
    DATA_RESOURCE_DIR("inner","内部取数");


    private String code;
    private String decs;

    DataResourceEnum(String code, String decs) {
        this.code = code;
        this.decs = decs;
    }
}
