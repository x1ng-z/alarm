package group.yzhs.alarm.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/8/13 9:43
 */
@Getter
public enum SessionContextEnum {
    SESSIONCONTEXT_ALARMLIST("alarm-list","报警列表"),
    SESSIONCONTEXT_AUDIOLIST("audio-message","语音报警消息"),
    SESSIONCONTEXT_AUDIOPUSHLASTTIME("audio-pushLastTime","语音消息上次播报时间")
    ;
    @EnumValue
//    @JsonValue
    private String code;
    private String decs;

    SessionContextEnum(String code, String decs) {
        this.code = code;
        this.decs = decs;
    }
}
