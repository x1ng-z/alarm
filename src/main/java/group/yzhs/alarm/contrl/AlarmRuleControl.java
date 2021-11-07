package group.yzhs.alarm.contrl;

import group.yzhs.alarm.model.dto.alarm.AlarmRuleDto;
import group.yzhs.alarm.model.httpRespBody.RestHttpResponseEntity;
import group.yzhs.alarm.service.alarm.AlarmRuleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/10/18 11:39
 */
@Slf4j
@RestController
@RequestMapping("/alarmRule")
public class AlarmRuleControl {
    @Autowired
    private AlarmRuleService alarmRuleService;

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public RestHttpResponseEntity<Void> add(@Valid @RequestBody AlarmRuleDto alarmRuleDto) {
        alarmRuleService.add(alarmRuleDto);
        return RestHttpResponseEntity.success();
    }

    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public RestHttpResponseEntity<Void> update(@Valid @RequestBody AlarmRuleDto alarmRuleDto) {
        alarmRuleService.update(alarmRuleDto);
        return RestHttpResponseEntity.success();
    }

    @RequestMapping(value = "/delete", method = RequestMethod.GET)
    public RestHttpResponseEntity<Void> delete(@RequestParam("id") Long id) {
        alarmRuleService.delete(id);
        return RestHttpResponseEntity.success();
    }

    @RequestMapping(value = "/getByPointId", method = RequestMethod.GET)
    public RestHttpResponseEntity<List<AlarmRuleDto>> getByPointId(@RequestParam("id") Long id) {
        return RestHttpResponseEntity.success(alarmRuleService.getByPointId(id));
    }

    @RequestMapping(value = "/getById", method = RequestMethod.GET)
    public RestHttpResponseEntity<AlarmRuleDto> getById(@RequestParam("id") Long id) {
        return RestHttpResponseEntity.success(alarmRuleService.getById(id));
    }

    @RequestMapping(value = "/export", method = RequestMethod.GET)
    public void export(HttpServletResponse httpServletResponse){
        alarmRuleService.export(httpServletResponse);
    }

    //数据解析,只解析5000条数据
    @RequestMapping(value = "/import", method = RequestMethod.POST)
    public RestHttpResponseEntity<Void> imp0rt(@RequestPart("excel-file") MultipartFile file){
        alarmRuleService.imp0rt(file,alarmRuleService);
        return RestHttpResponseEntity.success();
    }






}
