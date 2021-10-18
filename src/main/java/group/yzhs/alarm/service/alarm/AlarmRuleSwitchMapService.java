package group.yzhs.alarm.service.alarm;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import group.yzhs.alarm.exception.ParameterException;
import group.yzhs.alarm.mapper.AlarmRuleSwitchMapMapper;
import group.yzhs.alarm.mapper.impl.AlarmRuleMapperImp;
import group.yzhs.alarm.mapper.impl.AlarmRuleSwitchMapMapperImp;
import group.yzhs.alarm.mapper.impl.SwitchMapperImp;
import group.yzhs.alarm.model.dto.alarm.AlarmRuleSwitchMapDto;
import group.yzhs.alarm.model.entity.AlarmRule;
import group.yzhs.alarm.model.entity.AlarmRuleSwitchMap;
import group.yzhs.alarm.model.entity.Switch;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/10/16 22:07
 */
@Service
@Slf4j
public class AlarmRuleSwitchMapService {
    @Autowired
    private AlarmRuleSwitchMapMapperImp alarmRuleSwitchMapMapperImp;
    @Autowired
    private AlarmRuleMapperImp alarmRuleMapperImp;
    @Autowired
    private SwitchMapperImp switchMapperImp;


    @Transactional(rollbackFor = Exception.class)
    public void add(AlarmRuleSwitchMapDto alarmRuleSwitchMapDto) {
        AlarmRule alarmRule = alarmRuleMapperImp.getOne(Wrappers.<AlarmRule>lambdaQuery().eq(AlarmRule::getId, alarmRuleSwitchMapDto.getRefAlarmRuleId()));
        if (ObjectUtils.isEmpty(alarmRule)) {
            throw new ParameterException("找不到对应的报警规则id");
        }
        Switch aSwitch = switchMapperImp.getOne(Wrappers.<Switch>lambdaQuery().eq(Switch::getId, alarmRuleSwitchMapDto.getRefSwitchId()));
        if (ObjectUtils.isEmpty(aSwitch)) {
            throw new ParameterException("找不到对应的开关id");
        }
        AlarmRuleSwitchMap switchMap = new AlarmRuleSwitchMap();
        BeanUtils.copyProperties(alarmRuleSwitchMapDto, switchMap);
        alarmRuleSwitchMapMapperImp.save(switchMap);
    }


    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Optional.ofNullable(id).orElseThrow(() -> new ParameterException("id为空"));
        alarmRuleSwitchMapMapperImp.removeById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(AlarmRuleSwitchMapDto alarmRuleSwitchMapDto) {
        Optional.ofNullable(alarmRuleSwitchMapDto).map(AlarmRuleSwitchMapDto::getId).orElseThrow(() -> new ParameterException("id为空"));
        AlarmRuleSwitchMap switchMap = new AlarmRuleSwitchMap();

        AlarmRuleSwitchMap alarmRuleSwitchMap=alarmRuleSwitchMapMapperImp.getOne(Wrappers.<AlarmRuleSwitchMap>lambdaQuery()
                .eq(AlarmRuleSwitchMap::getRefAlarmRuleId,alarmRuleSwitchMapDto.getRefAlarmRuleId()).eq(AlarmRuleSwitchMap::getRefSwitchId,alarmRuleSwitchMapDto.getRefSwitchId()));

        BeanUtils.copyProperties(alarmRuleSwitchMapDto,alarmRuleSwitchMap);

        alarmRuleSwitchMapMapperImp.updateById(alarmRuleSwitchMap);
    }

}
