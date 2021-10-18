package group.yzhs.alarm.service.alarm;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import group.yzhs.alarm.exception.ParameterException;
import group.yzhs.alarm.mapper.impl.AlarmRuleSwitchMapMapperImp;
import group.yzhs.alarm.mapper.impl.DeviceMapperImp;
import group.yzhs.alarm.mapper.impl.PointMapperImp;
import group.yzhs.alarm.model.dto.alarm.AlarmRuleDto;
import group.yzhs.alarm.model.dto.device.DeviceDto;
import group.yzhs.alarm.model.dto.device.PointDto;
import group.yzhs.alarm.model.entity.AlarmRuleSwitchMap;
import group.yzhs.alarm.model.entity.Device;
import group.yzhs.alarm.model.entity.Point;
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
 * @date 2021/10/18 11:03
 */
@Service
@Slf4j
public class PointService {
    @Autowired
    private PointMapperImp pointMapperImp;

    @Autowired
    private AlarmRuleService alarmRuleService;

    @Autowired
    private AlarmRuleSwitchMapMapperImp alarmRuleSwitchMapMapperImp;

    @Autowired
    private DeviceMapperImp deviceMapperImp;

    @Transactional(rollbackFor = Exception.class)
    public void add(PointDto pointDto){
        //设备是否存在
        List<Device> devices=deviceMapperImp.list(Wrappers.<Device>lambdaQuery().eq(Device::getId,pointDto.getRefDeviceId()));
        if(CollectionUtils.isEmpty(devices)){
            throw new ParameterException("不存在指定id的设备");
        }
        Point point=new Point();
        BeanUtils.copyProperties(pointDto,point);
        pointMapperImp.save(point);
    }

    /*删除点位及其设置的规则*/
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id){
        Optional.ofNullable(id).orElseThrow(()->new ParameterException("点位id为空"));
        //查找位号相关的规则映射信息
        List<AlarmRuleDto> alarmRuleDtos=alarmRuleService.getByPointId(id);
        if(CollectionUtils.isNotEmpty(alarmRuleDtos)){
            //删除点位设置的规则
            alarmRuleDtos.forEach(a->{
                alarmRuleService.delete(a.getId());
            });
        }
        //删除点位
        pointMapperImp.remove(Wrappers.<Point>lambdaQuery().eq(Point::getId,id));
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(PointDto pointDto){
        Optional.ofNullable(pointDto).map(PointDto::getId).orElseThrow(()->new ParameterException("点位id为空"));
        Point point=new Point();
        BeanUtils.copyProperties(pointDto,point);
        pointMapperImp.updateById(point);
    }

    public List<PointDto> getByDevicId(Long deviceId){
        Optional.ofNullable(deviceId).orElseThrow(()->new ParameterException("设备id为空"));
        List<Point> db_res=pointMapperImp.list(Wrappers.<Point>lambdaQuery().eq(Point::getRefDeviceId,deviceId));
        List<PointDto> res=new ArrayList<>();
        if(CollectionUtils.isNotEmpty(db_res)){
            db_res.forEach(d->{
                PointDto pointDto=new PointDto();
                BeanUtils.copyProperties(d,pointDto);
                res.add(pointDto);
            });
        }

        return res;
    }

}
