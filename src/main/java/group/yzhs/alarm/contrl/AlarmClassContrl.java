package group.yzhs.alarm.contrl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import group.yzhs.alarm.mapper.impl.AlarmClassMapperImp;
import group.yzhs.alarm.model.dto.alarm.AlarmClassDto;
import group.yzhs.alarm.model.entity.AlarmClass;
import group.yzhs.alarm.model.httpRespBody.RestHttpResponseEntity;
import group.yzhs.alarm.model.vo.alarm.AlarmClasszVO;
import group.yzhs.alarm.service.alarm.AlarmClazzService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/10/16 12:51
 */
@RestController
@RequestMapping("/alarmClass")
public class AlarmClassContrl {

    @Autowired
    private AlarmClassMapperImp alarmClassMapperImp;

    @Autowired
    private AlarmClazzService alarmClazzService;

    @RequestMapping(path ="/add",method= RequestMethod.POST)
    public RestHttpResponseEntity<Void> addAlarmClass(@Valid @RequestBody AlarmClassDto alarmClassDto){
        alarmClazzService.addAlarmClazz(alarmClassDto);
        return RestHttpResponseEntity.success();
    }

    @RequestMapping(path ="/get",method= RequestMethod.GET)
    public RestHttpResponseEntity<List<AlarmClasszVO>> getAlarmClass()  {
        List<AlarmClass> dbres=alarmClassMapperImp.list();
        List<AlarmClasszVO> res=new ArrayList<>();
        if(CollectionUtils.isNotEmpty(dbres)){
            dbres.stream().forEach(a->{
                AlarmClasszVO alarmClasszVO=AlarmClasszVO.builder().build();
                BeanUtils.copyProperties(a,alarmClasszVO);
                res.add(alarmClasszVO);
            });
        }
        return RestHttpResponseEntity.success(res);
    }

    @RequestMapping(path ="/update",method= RequestMethod.POST)
    public RestHttpResponseEntity<Void> updateAlarmClazz(@Valid @RequestBody AlarmClassDto alarmClassDto){
        alarmClazzService.updateAlarmClazz(alarmClassDto);
        return RestHttpResponseEntity.success();
    }

    @RequestMapping(path ="/delete",method= RequestMethod.GET)
    public RestHttpResponseEntity<Void> deleteAlarmClazz(@RequestParam("id")Long id){
        alarmClazzService.deleteAlarmClazz(id);
        return RestHttpResponseEntity.success();
    }

}
