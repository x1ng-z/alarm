package group.yzhs.alarm.contrl;

import group.yzhs.alarm.model.dto.device.DeviceDto;
import group.yzhs.alarm.model.dto.device.PointDto;
import group.yzhs.alarm.model.httpRespBody.RestHttpResponseEntity;
import group.yzhs.alarm.service.alarm.PointService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/10/18 15:24
 */
@RestController
@Slf4j
@RequestMapping("/point")
public class PointControl {

    @Autowired
    private PointService pointService;


    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public RestHttpResponseEntity<Void> add(@Valid @RequestBody PointDto pointDto) {
        pointService.add(pointDto);
        return RestHttpResponseEntity.success();
    }

    @RequestMapping(value = "/delete", method = RequestMethod.GET)
    public RestHttpResponseEntity<Void> delete(Long id) {
        pointService.delete(id);
        return RestHttpResponseEntity.success();
    }

    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public RestHttpResponseEntity<Void> update(@Valid @RequestBody PointDto pointDto) {
        pointService.update(pointDto);
        return RestHttpResponseEntity.success();
    }


    @RequestMapping(value = "/get", method = RequestMethod.GET)
    public RestHttpResponseEntity<Void> get(@RequestParam("id") Long deviceId) {
        pointService.getByDevicId(deviceId);
        return RestHttpResponseEntity.success();
    }


}
