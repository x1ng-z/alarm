package group.yzhs.alarm.mapper.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import group.yzhs.alarm.mapper.AlarmRuleSwitchMapMapper;
import group.yzhs.alarm.mapper.SystemConfigMapper;
import group.yzhs.alarm.model.entity.AlarmRuleSwitchMap;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/10/16 12:44
 */
@Service
public class AlarmRuleSwitchMapMapperImp extends ServiceImpl<AlarmRuleSwitchMapMapper, AlarmRuleSwitchMap> {
    @Resource
    private AlarmRuleSwitchMapMapper alarmRuleSwitchMapMapper;
}
