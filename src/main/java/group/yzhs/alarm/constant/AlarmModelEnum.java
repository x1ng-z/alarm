package group.yzhs.alarm.constant;

import lombok.Getter;

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

    private String name;
    private String code;
}
