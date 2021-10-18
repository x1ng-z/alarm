package group.yzhs.alarm.contrl;

import group.yzhs.alarm.mapper.impl.AlarmHistoryMapperImp;
import group.yzhs.alarm.model.dto.alarm.AlarmHistoryDto;
import group.yzhs.alarm.model.httpRespBody.RestHttpResponseEntity;
import group.yzhs.alarm.model.vo.page.BasePageParamDto;
import group.yzhs.alarm.model.vo.page.PageInfo;
import group.yzhs.alarm.service.alarm.AlarmHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/10/16 17:41
 */
@RequestMapping("/alarmHistory")
@RestController
public class AlarmHistoryContrl {
    @Autowired
    private AlarmHistoryService alarmHistoryService;

    @Autowired
    private AlarmHistoryMapperImp alarmHistoryMapperImp;

    /**
     *
     * */
    @RequestMapping(value = "/add",method = RequestMethod.POST)
    public RestHttpResponseEntity<Void> addAlarmHistory(@Valid @RequestBody AlarmHistoryDto alarmHistoryDto){
        alarmHistoryService.add(alarmHistoryDto);
        return RestHttpResponseEntity.success();
    }

    @RequestMapping(value = "/delete",method=RequestMethod.GET)
    public RestHttpResponseEntity<Void> deleteAlarmHistory(@RequestParam("id")Long id){
        alarmHistoryService.delete(id);
        return RestHttpResponseEntity.success();
    }

    @RequestMapping(value = "/update",method=RequestMethod.POST)
    public RestHttpResponseEntity<Void> updateAlarmHistory(@Valid @RequestBody AlarmHistoryDto alarmHistoryDto){
        alarmHistoryService.update(alarmHistoryDto);
        return RestHttpResponseEntity.success();
    }

    @RequestMapping(value = "/page",method = RequestMethod.POST)
    public RestHttpResponseEntity<PageInfo<AlarmHistoryDto>> pageAlarmHistory(@Valid @RequestBody BasePageParamDto pageParamDto){
        return RestHttpResponseEntity.success(alarmHistoryService.pageAlarmHistory(pageParamDto));
    }


    @RequestMapping(value = "/get/{id}",method = RequestMethod.GET)
    public RestHttpResponseEntity<AlarmHistoryDto> pageAlarmHistory(@PathVariable("id") Long id){
        return RestHttpResponseEntity.success(alarmHistoryService.get(id));
    }

}
