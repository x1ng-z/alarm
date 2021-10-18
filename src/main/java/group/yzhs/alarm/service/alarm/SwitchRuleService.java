package group.yzhs.alarm.service.alarm;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import group.yzhs.alarm.exception.ParameterException;
import group.yzhs.alarm.mapper.impl.SwitchMapperImp;
import group.yzhs.alarm.mapper.impl.SwitchRuleMapperImp;
import group.yzhs.alarm.model.dto.device.SwitchDto;
import group.yzhs.alarm.model.dto.device.SwitchRuleDto;
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
 * @date 2021/10/18 11:23
 */
@Service
@Slf4j
public class SwitchRuleService {
    @Autowired
    private SwitchRuleMapperImp switchRuleMapperImp;

    @Transactional(rollbackFor = Exception.class)
    public void add(SwitchRuleDto switchRuleDto){
        SwitchRule switchRule=new SwitchRule();
        BeanUtils.copyProperties(switchRuleDto,switchRule);
        switchRuleMapperImp.save(switchRule);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id){
        Optional.ofNullable(id).orElseThrow(()->new ParameterException("开关规则id为空"));
        switchRuleMapperImp.remove(Wrappers.<SwitchRule>lambdaQuery().eq(SwitchRule::getId,id));
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(SwitchRuleDto switchRuleDto){
        Optional.ofNullable(switchRuleDto).map(SwitchRuleDto::getId).orElseThrow(()->new ParameterException("开关规则id为空"));
        SwitchRule switchRule=new SwitchRule();
        BeanUtils.copyProperties(switchRuleDto,switchRule);
        switchRuleMapperImp.updateById(switchRule);
    }

    public List<SwitchRuleDto> get(Long swicthId){
        Optional.ofNullable(swicthId).orElseThrow(()->new ParameterException("开关id为空"));
        List<SwitchRule> db_res=switchRuleMapperImp.list(Wrappers.<SwitchRule>lambdaQuery().eq(SwitchRule::getRefSwitchId,swicthId));
        List<SwitchRuleDto> res=new ArrayList<>();
        if(CollectionUtils.isNotEmpty(db_res)){
            db_res.forEach(d->{
                SwitchRuleDto switchRuleDto=new SwitchRuleDto();
                BeanUtils.copyProperties(d,switchRuleDto);
                res.add(switchRuleDto);
            });
        }
        return res;
    }

}
