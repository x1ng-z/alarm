package group.yzhs.alarm.model.rule;


import group.yzhs.alarm.constant.AlarmModelEnum;
import group.yzhs.alarm.constant.ProductTypeEnum;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;


@Data
public class BaseRule {
    //iot 节点
    private String iotNode;
    private String tag;
    private String notion;
    //推送模板
    private String template;
    private boolean isPushWX;
    private boolean isAudio;
    private String subModel;

    private AlarmModelEnum alarmModelEnum;
    //最新值
    private double value;
    //当前是否报警
    private AtomicBoolean isAlarm=new AtomicBoolean(false);
    //微信推送内容
    private String pushWXContext;
    //语音报警内容
    private String pushAudioContext;


    private LocalDateTime pushWXLastTime;//上一次推送时间

    private Map<String,LocalDateTime> audioPushLastTime=new ConcurrentHashMap();//key=seesionId

    //报警工艺
    private ProductTypeEnum product;




}
