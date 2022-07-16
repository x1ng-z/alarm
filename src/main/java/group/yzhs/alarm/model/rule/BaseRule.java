package group.yzhs.alarm.model.rule;


import group.yzhs.alarm.constant.AlarmModelEnum;
import group.yzhs.alarm.constant.ProductTypeEnum;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Getter
@Setter
@Slf4j
public class BaseRule {

    private Long id;
    private String alarmMode;
    private String alarmSubMode;
    private Boolean isAudio;
    private Long alarmClassId;
    private Long pointId;
    //'报警组：ktj，sl，sc，zc
    private String alarmGroup;
    private BigDecimal limiteValue;
    private String alarmTemple;
    private Boolean isWxPush;
    private String alarmInterval;


    //iot 节点
    private String iotNode;
    private String tag;
    //    private String notion;
    //    推送模板
    //    private String template;
    //    private boolean isPushWX;
    //    private boolean isAudio;
    //    private String subModel;
    //    private AlarmModelEnum alarmModelEnum;
    /******************以下是报警的时候的运行中间变量***********/
    //最新值
    private double value;
    //当前是否报警
    private AtomicBoolean isAlarm = new AtomicBoolean(false);
    //微信推送内容
    private String pushWXContext;
    //语音报警内容
    private String pushAudioContext;


    private LocalDateTime pushWXLastTime;//上一次推送时间

    private Map<String, LocalDateTime> audioPushLastTime = new ConcurrentHashMap();//key=seesionId


    /**************************************************/
    //报警工艺
//    private ProductTypeEnum product;
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BaseRule baseRule = (BaseRule) o;
        return id.equals(baseRule.id) && alarmMode.equals(baseRule.alarmMode) && alarmSubMode.equals(baseRule.alarmSubMode) && isAudio.equals(baseRule.isAudio) && pointId.equals(baseRule.pointId) && alarmGroup.equals(baseRule.alarmGroup) && limiteValue.equals(baseRule.limiteValue) && alarmTemple.equals(baseRule.alarmTemple) && isWxPush.equals(baseRule.isWxPush);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, alarmMode, alarmSubMode, isAudio, pointId, alarmGroup, limiteValue, alarmTemple, isWxPush);
    }
}
