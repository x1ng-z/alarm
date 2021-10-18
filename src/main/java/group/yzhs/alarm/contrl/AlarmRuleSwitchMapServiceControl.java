package group.yzhs.alarm.contrl;

import group.yzhs.alarm.model.dto.alarm.AlarmRuleSwitchMapDto;
import group.yzhs.alarm.model.httpRespBody.RestHttpResponseEntity;
import group.yzhs.alarm.service.alarm.AlarmRuleSwitchMapService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/10/18 12:30
 */
@RestController
@Slf4j
@RequestMapping("/alarmRuleSwitchMap")
public class AlarmRuleSwitchMapServiceControl {
    @Autowired
    private AlarmRuleSwitchMapService alarmRuleSwitchMapService;


    @RequestMapping(value = "/add",method = RequestMethod.POST)
    public RestHttpResponseEntity<Void> add(@Valid @RequestBody AlarmRuleSwitchMapDto alarmRuleSwitchMapDto){
        alarmRuleSwitchMapService.add(alarmRuleSwitchMapDto);
        return RestHttpResponseEntity.success();
    }

    @Deprecated
    @RequestMapping(value = "/delete",method = RequestMethod.GET)
    public RestHttpResponseEntity<Void> delete(@RequestParam("id") Long id){
        alarmRuleSwitchMapService.delete(id);
        return RestHttpResponseEntity.success();
    }

    @RequestMapping(value = "/update",method = RequestMethod.GET)
    public RestHttpResponseEntity<Void> update(@Valid @RequestBody AlarmRuleSwitchMapDto alarmRuleSwitchMapDto){
        alarmRuleSwitchMapService.update(alarmRuleSwitchMapDto);
        return RestHttpResponseEntity.success();
    }




}
