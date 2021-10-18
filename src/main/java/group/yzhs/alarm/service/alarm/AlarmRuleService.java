package group.yzhs.alarm.service.alarm;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import group.yzhs.alarm.constant.AlarmModelEnum;
import group.yzhs.alarm.constant.LimiteModelEnum;
import group.yzhs.alarm.constant.ProductTypeEnum;
import group.yzhs.alarm.constant.TrigerModelEnum;
import group.yzhs.alarm.exception.ParameterException;
import group.yzhs.alarm.mapper.impl.AlarmRuleMapperImp;
import group.yzhs.alarm.mapper.impl.AlarmRuleSwitchMapMapperImp;
import group.yzhs.alarm.model.dto.alarm.AlarmRuleDto;
import group.yzhs.alarm.model.entity.AlarmRule;
import group.yzhs.alarm.model.entity.AlarmRuleSwitchMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/10/16 20:19
 */
@Slf4j
@Service
public class AlarmRuleService {
    @Autowired
    private AlarmRuleMapperImp alarmRuleMapperImp;

    @Autowired
    private AlarmRuleSwitchMapMapperImp alarmRuleSwitchMapMapperImp;

    private final static Map<String,LimiteModelEnum> modelEnumMap;
    private final static Map<String,AlarmModelEnum> alarmModelEnumMap;
    private final static Map<String,TrigerModelEnum> trigerModelEnumMap;
    private final static Map<String,ProductTypeEnum> producttypeEnum;
    static {
        modelEnumMap=Arrays.stream(LimiteModelEnum.values()).collect(Collectors.toMap(LimiteModelEnum::getCode, p->p,(o, n)->n));
        alarmModelEnumMap=Arrays.stream(AlarmModelEnum.values()).collect(Collectors.toMap(AlarmModelEnum::getCode, p->p,(o, n)->n));
        trigerModelEnumMap=Arrays.stream(TrigerModelEnum.values()).collect(Collectors.toMap(TrigerModelEnum::getCode, p->p,(o, n)->n));
        producttypeEnum=Arrays.stream(ProductTypeEnum.values()).collect(Collectors.toMap(ProductTypeEnum::getCode, p->p,(o, n)->n));
    }

    @Transactional(rollbackFor = Exception.class)
    public void add(AlarmRuleDto alarmRuleDto){
        alarmRuleMapperImp.save( checkParam(alarmRuleDto));
    }

    /*删除报警规则，及其设置的开关映射*/
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id){
        Optional.ofNullable(id).orElseThrow(()->new ParameterException("报警规则id不能为空"));
        //删除规则开关映射
        alarmRuleSwitchMapMapperImp.remove(Wrappers.<AlarmRuleSwitchMap>lambdaQuery().eq(AlarmRuleSwitchMap::getRefAlarmRuleId,id));
        //删除规则
        alarmRuleMapperImp.remove(Wrappers.<AlarmRule>lambdaQuery().eq(AlarmRule::getId,id));
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(AlarmRuleDto alarmRuleDto){
        alarmRuleMapperImp.updateById(checkParam(alarmRuleDto));
    }


    public AlarmRuleDto getById(Long id){
        Optional.ofNullable(id).orElseThrow(()->new ParameterException("报警规则id不能为空"));
        AlarmRule alarmRule =alarmRuleMapperImp.getOne(Wrappers.<AlarmRule>lambdaQuery().eq(AlarmRule::getId,id));
        AlarmRuleDto alarmRuleDto=new AlarmRuleDto();
        if(ObjectUtils.isNotEmpty(alarmRule)){
            BeanUtils.copyProperties(alarmRule,alarmRuleDto);
        }
        return alarmRuleDto;
    }

    public List<AlarmRuleDto> getByPointId(Long pointId){
        Optional.ofNullable(pointId).orElseThrow(()->new ParameterException("点位id不能为空"));
        List<AlarmRule> alarmRuleList=alarmRuleMapperImp.list(Wrappers.<AlarmRule>lambdaQuery().eq(AlarmRule::getPointId,pointId));
        List<AlarmRuleDto> res=new ArrayList<>();
        if(CollectionUtils.isNotEmpty(alarmRuleList)){
            alarmRuleList.stream().forEach(a->{
                AlarmRuleDto alarmRuleDto=new AlarmRuleDto();
                BeanUtils.copyProperties(a,alarmRuleDto);
                res.add(alarmRuleDto);
            });
        }
        return res;
    }


    private  AlarmRule checkParam(AlarmRuleDto alarmRuleDto){
        AlarmRule alarmRule=new AlarmRule();

        /*判断报警模式是否在规定的编码范围内，并且设置待插入的报警规则的模式*/
        Boolean isValidAlarmMod=Arrays.stream(AlarmModelEnum.values()).anyMatch(m-> {
            if(m.getCode().equals(alarmRuleDto.getAlarmMode())){
                alarmRule.setAlarmMode(m.getCode());
                return true;
            }
            return false;
        });

        Boolean isValidLiAlarmMod=Arrays.stream(LimiteModelEnum.values()).anyMatch(lm->{
            if(lm.getCode().equals(alarmRuleDto.getAlarmSubMode())){
                alarmRule.setAlarmSubMode(lm.getCode());
                return true;
            }
            return false;
        });


        Boolean isValidTmAlarmMod=Arrays.stream(TrigerModelEnum.values()).anyMatch(tm->{
            if(tm.getCode().equals(alarmRuleDto.getAlarmSubMode())){
                alarmRule.setGroup(producttypeEnum.get(tm.getCode()));
                return true;
            }
            return false;
        });

        if(isValidAlarmMod&&(isValidLiAlarmMod||isValidTmAlarmMod)){

        }else {
            throw new ParameterException("报警模式编码不匹配");
        }


        /*报警组的校验*/
        Boolean groupCheck=Arrays.stream(ProductTypeEnum.values()).anyMatch(pt->{
            if(pt.getCode().equals(alarmRuleDto.getGroup())){
                alarmRule.setGroup(producttypeEnum.get(pt.getCode()));
                return true;
            }
            return false;
        });

        if(!groupCheck){
            throw new ParameterException("报警组编码不匹配");
        }
        alarmRule.setIsAudio(alarmRuleDto.getIsAudio());
        alarmRule.setLimiteValue(alarmRuleDto.getLimiteValue());
        alarmRule.setPointId(alarmRuleDto.getPointId());
        alarmRule.setAlarmClassId(alarmRuleDto.getAlarmClassId());
        alarmRule.setAlarmTemple(alarmRuleDto.getAlarmTemple());
        return alarmRule;
    }

    @Deprecated
    private AlarmRuleDto copyAlarmRule(AlarmRule alarmRule){
        return AlarmRuleDto.builder()
                .alarmMode(alarmRule.getAlarmMode())
                .alarmClassId(alarmRule.getAlarmClassId())
                .alarmSubMode(alarmRule.getAlarmSubMode())
                .alarmTemple(alarmRule.getAlarmTemple())
                .isAudio(alarmRule.getIsAudio())
                .id(alarmRule.getId())
                .pointId(alarmRule.getPointId())
                .limiteValue(alarmRule.getLimiteValue())
                .group(alarmRule.getGroup()/*.getCode()*/)
                .build();
    }
}
