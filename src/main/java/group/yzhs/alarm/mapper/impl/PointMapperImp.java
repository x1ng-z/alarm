package group.yzhs.alarm.mapper.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import group.yzhs.alarm.mapper.DeviceMapper;
import group.yzhs.alarm.mapper.PointMapper;
import group.yzhs.alarm.model.entity.Point;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/10/16 12:46
 */
@Service
public class PointMapperImp extends ServiceImpl<PointMapper, Point> {
    @Resource
    private PointMapper pointMapper;
}
