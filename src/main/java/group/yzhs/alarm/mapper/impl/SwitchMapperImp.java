package group.yzhs.alarm.mapper.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import group.yzhs.alarm.mapper.AlarmClassMapper;
import group.yzhs.alarm.mapper.SwitchMapper;
import group.yzhs.alarm.model.entity.AlarmClass;
import group.yzhs.alarm.model.entity.Switch;
import org.springframework.stereotype.Service;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/10/18 10:25
 */
@Service
public class SwitchMapperImp extends ServiceImpl<SwitchMapper, Switch> {
    private SwitchMapper switchMapper;
}
