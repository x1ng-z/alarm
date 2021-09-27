package group.yzhs.alarm.model;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/8/13 9:51
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AlarmMessage {
    //报警等级
    private Long level = 0L;
    //报警内容
    private String context;
    //当前值
    @JSONField(format = "#.###")
    private Double value;
    //变化率
    private Double rate;
    //工序
    private String product;
    //时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date date;

    @JSONField(serialize = false)
    private String alarmId;

}
