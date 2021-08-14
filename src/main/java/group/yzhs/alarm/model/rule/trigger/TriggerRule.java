package group.yzhs.alarm.model.rule.trigger;

import group.yzhs.alarm.model.rule.BaseRule;
import lombok.Data;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/6/15 11:17
 */
@Data
public class TriggerRule extends BaseRule {

    //上一次的值
    private Double lastvalue;

}
