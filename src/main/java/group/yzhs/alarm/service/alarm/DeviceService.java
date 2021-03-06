package group.yzhs.alarm.service.alarm;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import group.yzhs.alarm.exception.ParameterException;
import group.yzhs.alarm.mapper.impl.*;
import group.yzhs.alarm.model.dto.device.DeviceDto;
import group.yzhs.alarm.model.entity.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/10/18 10:52
 */
@Service
public class DeviceService {
    @Autowired
    private DeviceMapperImp deviceMapperImp;

    @Autowired
    private AlarmRuleMapperImp alarmRuleMapperImp;

    @Autowired
    private AlarmRuleSwitchMapMapperImp alarmRuleSwitchMapMapperImp;

    @Autowired
    private PointMapperImp pointMapperImp;


    @Autowired
    private AlarmClassMapperImp alarmClassMapperImp;


    @Autowired
    private PointService pointService;

    @Transactional(rollbackFor = Exception.class)
    public void add(DeviceDto deviceDto){
        //查询是否存在相同的设备名
        List<Device> deviceList=deviceMapperImp.list(Wrappers.<Device>lambdaQuery().eq(Device::getDeviceName,deviceDto.getDeviceName()));
        if(deviceList.size()>0){
            throw new ParameterException("设备名称重复");
        }

        Device device=new Device();
        BeanUtils.copyProperties(deviceDto,device);
        deviceMapperImp.save(device);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id){
        Optional.ofNullable(id).orElseThrow(()->new ParameterException("设备id为空"));

        Device device=deviceMapperImp.getById(id);
        //查询属于设备的所有点位
        List<Point> pointList=pointMapperImp.list(Wrappers.<Point>lambdaQuery().eq(Point::getRefDeviceId,device.getId()));
        if(ObjectUtils.isNotEmpty(device)){
            if(CollectionUtils.isNotEmpty(pointList)){
                //删除所有相关点位数据
                pointList.forEach(p->{pointService.delete(p.getId());});
            }
            /*删除设备*/
            deviceMapperImp.remove(Wrappers.<Device>lambdaQuery().eq(Device::getId,id));

        }else {
            throw new ParameterException("设备不存在");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(DeviceDto deviceDto){
        Optional.ofNullable(deviceDto).map(DeviceDto::getId).orElseThrow(()->new ParameterException("设备id为空"));

        List<Device> deviceList=deviceMapperImp.list(Wrappers.<Device>lambdaQuery().eq(Device::getDeviceName,deviceDto.getDeviceName()));

        boolean otherDevice=deviceList.stream().anyMatch(d->!(d.getId().equals(deviceDto.getId())));
        if(otherDevice){
            throw new ParameterException("设备名称重复");
        }
        Device device=new Device();
        BeanUtils.copyProperties(deviceDto,device);
        deviceMapperImp.updateById(device);
    }

    public List<DeviceDto> get(){
        List<Device> db_res=deviceMapperImp.list();
        List<DeviceDto> res=new ArrayList<>();

        if(CollectionUtils.isNotEmpty(db_res)){
            db_res.forEach(d->{
                DeviceDto deviceDto=new DeviceDto();
                BeanUtils.copyProperties(d,deviceDto);
                res.add(deviceDto);
            });
        }

        return res;
    }


    @Transactional(rollbackFor = Exception.class,propagation = Propagation.REQUIRES_NEW)
    public void deleteTranlatioTest(){
        alarmClassMapperImp.remove(Wrappers.<AlarmClass>lambdaQuery().eq(AlarmClass::getName,"123"));
    }




}
