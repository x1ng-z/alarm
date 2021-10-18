package group.yzhs.alarm.mapper.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import group.yzhs.alarm.mapper.AlarmRuleSwitchMapMapper;
import group.yzhs.alarm.mapper.DeviceMapper;
import group.yzhs.alarm.model.entity.Device;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/10/16 12:45
 */
@Service
public class DeviceMapperImp extends ServiceImpl<DeviceMapper, Device> {
    @Resource
    private DeviceMapper deviceMapper;
}
