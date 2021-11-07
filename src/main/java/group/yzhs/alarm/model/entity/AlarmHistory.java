package group.yzhs.alarm.model.entity;

import group.yzhs.alarm.constant.AlarmPushStatusEnum;
import lombok.Data;

import java.util.Date;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/10/16 10:07
 */
@Data
public class AlarmHistory extends BaseEntity{
    private String alarmContext;
    private Date createTime;
    private AlarmPushStatusEnum pushStatus;
    private String deviceNo;
    private Long refAlarmRuleId;
}
