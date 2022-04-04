package group.yzhs.alarm.model.rule;


import group.yzhs.alarm.constant.AlarmModelEnum;
import group.yzhs.alarm.constant.ProductTypeEnum;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;


@Data
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


    //iot 节点
    private String iotNode;
    private String tag;
//    private String notion;
    //推送模板
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


}
