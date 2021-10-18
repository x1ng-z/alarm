package group.yzhs.alarm.mapper.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import group.yzhs.alarm.mapper.AlarmClassMapper;
import group.yzhs.alarm.mapper.AlarmRuleMapper;
import group.yzhs.alarm.model.entity.AlarmRule;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/10/16 12:38
 */
@Service
public class AlarmRuleMapperImp extends ServiceImpl<AlarmRuleMapper, AlarmRule> {
    @Resource
    private AlarmRuleMapper alarmRuleMapper;
}
