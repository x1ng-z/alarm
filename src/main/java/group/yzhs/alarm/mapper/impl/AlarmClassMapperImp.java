package group.yzhs.alarm.mapper.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import group.yzhs.alarm.mapper.AlarmClassMapper;
import group.yzhs.alarm.model.entity.AlarmClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/10/16 10:43
 */
@Service
public class AlarmClassMapperImp extends ServiceImpl<AlarmClassMapper, AlarmClass> {

    @Resource
    private  AlarmClassMapper alarmClassMapper;

}
