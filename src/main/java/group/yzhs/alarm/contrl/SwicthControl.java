package group.yzhs.alarm.contrl;

import group.yzhs.alarm.model.dto.device.PointDto;
import group.yzhs.alarm.model.dto.device.SwitchDto;
import group.yzhs.alarm.model.httpRespBody.RestHttpResponseEntity;
import group.yzhs.alarm.service.alarm.SwitchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/10/18 15:24
 */
@RestController
@Slf4j
@RequestMapping("/switch")
public class SwicthControl {

    @Autowired
    private SwitchService switchService;


    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public RestHttpResponseEntity<Void> add(@Valid @RequestBody SwitchDto switchDto) {
        switchService.add(switchDto);
        return RestHttpResponseEntity.success();
    }

    @RequestMapping(value = "/delete", method = RequestMethod.GET)
    public RestHttpResponseEntity<Void> delete(Long id) {
        switchService.delete(id);
        return RestHttpResponseEntity.success();
    }

    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public RestHttpResponseEntity<Void> update(@Valid @RequestBody SwitchDto switchDto) {
        switchService.update(switchDto);
        return RestHttpResponseEntity.success();
    }


    @RequestMapping(value = "/get", method = RequestMethod.GET)
    public RestHttpResponseEntity<List<SwitchDto>> get() {
        return RestHttpResponseEntity.success(switchService.get());
    }


}
