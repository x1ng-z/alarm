package group.yzhs.alarm.service.alarm;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import group.yzhs.alarm.exception.ParameterException;
import group.yzhs.alarm.mapper.impl.AlarmClassMapperImp;
import group.yzhs.alarm.model.dto.alarm.AlarmClassDto;
import group.yzhs.alarm.model.entity.AlarmClass;
import group.yzhs.alarm.model.httpRespBody.RestHttpResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/10/16 15:47
 */
@Service
public class AlarmClazzService {
    @Autowired
    private AlarmClassMapperImp alarmClassMapperImp;

    @Transactional(rollbackFor = Exception.class)
    public void addAlarmClazz(AlarmClassDto alarmClassDto) throws ParameterException{
        List<AlarmClass> existAlarmClasses=alarmClassMapperImp.list(Wrappers.<AlarmClass>lambdaQuery().eq(AlarmClass::getCode,alarmClassDto.getCode()));
        if(CollectionUtils.isNotEmpty(existAlarmClasses)){
            throw  new ParameterException("存在相同的编码，请求修改编码");
        }else{
            AlarmClass alarmClass=new AlarmClass();
            alarmClass.setCode(alarmClassDto.getCode());
            alarmClass.setName(alarmClassDto.getName());
            alarmClassMapperImp.save(alarmClass);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateAlarmClazz(AlarmClassDto alarmClassDto) throws ParameterException{
        Optional.ofNullable(alarmClassDto).map(AlarmClassDto::getId).orElseThrow(()-> new ParameterException("id为空"));
        List<AlarmClass> existAlarmClasses=alarmClassMapperImp.list(Wrappers.<AlarmClass>lambdaQuery().eq(AlarmClass::getCode,alarmClassDto.getCode()));
        if(CollectionUtils.isNotEmpty(existAlarmClasses)){
            throw  new ParameterException("存在相同的编码，请求修改编码");
        }else{
            AlarmClass alarmClass=new AlarmClass();
            alarmClass.setCode(alarmClassDto.getCode());
            alarmClass.setName(alarmClassDto.getName());
            alarmClass.setId(alarmClassDto.getId());
//            alarmClassMapperImp.updateById(alarmClass);
            alarmClassMapperImp.update(Wrappers.<AlarmClass>lambdaUpdate()
                    .eq(AlarmClass::getId,alarmClassDto.getId())
                    .set(AlarmClass::getCode,alarmClassDto.getCode())
                    .set(AlarmClass::getName,alarmClassDto.getName()));
        }
    }


    @Transactional(rollbackFor = Exception.class)
    public void deleteAlarmClazz(Long id) throws ParameterException{
        Optional.ofNullable(id).orElseThrow(()->new ParameterException("id为空"));
        alarmClassMapperImp.remove(Wrappers.<AlarmClass>lambdaQuery().eq(AlarmClass::getId,id));
    }


}
