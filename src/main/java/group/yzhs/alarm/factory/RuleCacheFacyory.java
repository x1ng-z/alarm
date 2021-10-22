package group.yzhs.alarm.factory;

import group.yzhs.alarm.constant.AlarmModelEnum;
import group.yzhs.alarm.exception.ParameterException;
import group.yzhs.alarm.model.entity.AlarmRule;
import group.yzhs.alarm.model.rule.BaseRule;
import group.yzhs.alarm.model.rule.limit.LimitRule;
import group.yzhs.alarm.model.rule.trigger.TriggerRule;
import org.springframework.beans.BeanUtils;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/10/22 13:01
 */

public class RuleCacheFacyory {

    public static BaseRule create(AlarmRule alarmRule){
        if(alarmRule.getAlarmMode().equals(AlarmModelEnum.ALARMMODEL_LIM.getCode())){
            LimitRule limitRule= new LimitRule();
            BeanUtils.copyProperties(alarmRule,limitRule);
            return limitRule;

        }else if(alarmRule.getAlarmMode().equals(AlarmModelEnum.ALARMMODEL_TRIG.getCode())){
            TriggerRule triggerRule=new TriggerRule();
            BeanUtils.copyProperties(alarmRule,triggerRule);
            return triggerRule;
        }else{
            throw new ParameterException(String.format("无法根据报警模式%s创建其缓存",alarmRule.getAlarmMode()));
        }
    }

}
