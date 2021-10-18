package group.yzhs.alarm.mapper.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import group.yzhs.alarm.mapper.SystemConfigMapper;
import group.yzhs.alarm.model.entity.SystemConfig;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/10/16 12:47
 */
@Service
public class SystemConfigMapperImp extends ServiceImpl<SystemConfigMapper, SystemConfig> {
    @Resource
    private SystemConfigMapper systemConfigMapper;
}
