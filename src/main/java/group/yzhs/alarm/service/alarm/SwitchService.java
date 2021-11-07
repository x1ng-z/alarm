package group.yzhs.alarm.service.alarm;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import group.yzhs.alarm.exception.ParameterException;
import group.yzhs.alarm.mapper.AlarmRuleSwitchMapMapper;
import group.yzhs.alarm.mapper.impl.AlarmRuleSwitchMapMapperImp;
import group.yzhs.alarm.mapper.impl.PointMapperImp;
import group.yzhs.alarm.mapper.impl.SwitchMapperImp;
import group.yzhs.alarm.mapper.impl.SwitchRuleMapperImp;
import group.yzhs.alarm.model.dto.device.PointDto;
import group.yzhs.alarm.model.dto.device.SwitchDto;
import group.yzhs.alarm.model.entity.AlarmRuleSwitchMap;
import group.yzhs.alarm.model.entity.Point;
import group.yzhs.alarm.model.entity.Switch;
import group.yzhs.alarm.model.entity.SwitchRule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/10/18 11:09
 */
@Service
@Slf4j
public class SwitchService {
    @Autowired
    private SwitchMapperImp switchMapperImp;

    @Autowired
    private SwitchRuleMapperImp switchRuleMapperImp;

    @Autowired
    private AlarmRuleSwitchMapMapperImp alarmRuleSwitchMapMapperImp;

    @Transactional(rollbackFor = Exception.class)
    public void add(SwitchDto switchDto){
        List<Switch> dbres=switchMapperImp.list(Wrappers.<Switch>lambdaQuery().eq(Switch::getName,switchDto.getName()));
        if(CollectionUtils.isNotEmpty(dbres)){
            throw new ParameterException("重复的开关名称");
        }
        Switch aSwitch=new Switch();
        BeanUtils.copyProperties(switchDto,aSwitch);
        switchMapperImp.save(aSwitch);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id){
        Optional.ofNullable(id).orElseThrow(()->new ParameterException("开关id为空"));
        Switch aSwitch=switchMapperImp.getById(id);
        Optional.ofNullable(aSwitch).orElseThrow(()->new ParameterException("开关不存在"));
        //删除开关的规则
        switchRuleMapperImp.remove(Wrappers.<SwitchRule>lambdaQuery().eq(SwitchRule::getRefSwitchId,id));
        //删除开关
        switchMapperImp.remove(Wrappers.<Switch>lambdaQuery().eq(Switch::getId,id));
        //删除点位与开关的映射
        alarmRuleSwitchMapMapperImp.remove(Wrappers.<AlarmRuleSwitchMap>lambdaQuery().eq(AlarmRuleSwitchMap::getRefSwitchId,id));
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(SwitchDto switchDto){
        Optional.ofNullable(switchDto).map(SwitchDto::getId).orElseThrow(()->new ParameterException("开关id为空"));

        List<Switch> dbres=switchMapperImp.list(Wrappers.<Switch>lambdaQuery().eq(Switch::getName,switchDto.getName()));
        if(CollectionUtils.isNotEmpty(dbres)&&dbres.size()>1){
            throw new ParameterException("重复的开关名称");
        }


        Switch aSwitch=new Switch();
        BeanUtils.copyProperties(switchDto,aSwitch);
        switchMapperImp.updateById(aSwitch);
    }

    public List<SwitchDto> get(){
        List<Switch> db_res=switchMapperImp.list();
        List<SwitchDto> res=new ArrayList<>();
        if(CollectionUtils.isNotEmpty(db_res)){
            db_res.forEach(d->{
                SwitchDto switchDto=new SwitchDto();
                BeanUtils.copyProperties(d,switchDto);
                res.add(switchDto);
            });
        }
        return res;
    }
}
