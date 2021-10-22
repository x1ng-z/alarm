package group.yzhs.alarm.contrl;

import group.yzhs.alarm.model.dto.alarm.AlarmRuleDto;
import group.yzhs.alarm.model.httpRespBody.RestHttpResponseEntity;
import group.yzhs.alarm.service.alarm.PointService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/10/17 22:25
 */
@Slf4j
@RestController
@RequestMapping("/test")
public class EnumTestControl {

    @Autowired
    private PointService pointService;

    @RequestMapping("/enum")
    public  RestHttpResponseEntity<AlarmRuleDto> testenum(@RequestBody AlarmRuleDto alarmRuleDto){
        return RestHttpResponseEntity.success(alarmRuleDto);
    }


    @RequestMapping("/trans")
    public  RestHttpResponseEntity<Void> testtrans(){
        pointService.deleteTranlatioTest();
        return RestHttpResponseEntity.success();
    }


}
