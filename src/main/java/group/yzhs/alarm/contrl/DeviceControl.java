package group.yzhs.alarm.contrl;

import com.alibaba.fastjson.JSON;
import group.yzhs.alarm.model.dto.alarm.AlarmRuleDto;
import group.yzhs.alarm.model.dto.device.DeviceDto;
import group.yzhs.alarm.model.httpRespBody.RestHttpResponseEntity;
import group.yzhs.alarm.service.alarm.DeviceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/10/18 12:40
 */
@RestController
@Slf4j
@RequestMapping("/device")
public class DeviceControl {
    @Autowired
    private DeviceService deviceService;


    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public RestHttpResponseEntity<Void> add(@Valid @RequestBody DeviceDto deviceDto) {
        //System.out.println(JSON.toJSONString(deviceDto));
        deviceService.add(deviceDto);
        return RestHttpResponseEntity.success();
    }

    //删除设备及点位
    @RequestMapping(value = "/delete", method = RequestMethod.GET)
    public RestHttpResponseEntity<Void> delete(@RequestParam("id") Long id) {
        deviceService.delete(id);
        return RestHttpResponseEntity.success();
    }

    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public RestHttpResponseEntity<Void> update(@Valid @RequestBody DeviceDto deviceDto) {
        deviceService.update(deviceDto);
        return RestHttpResponseEntity.success();
    }

    @RequestMapping(value = "/get", method = RequestMethod.GET)
    public RestHttpResponseEntity<List<DeviceDto>> get() {
        return RestHttpResponseEntity.success(deviceService.get());
    }
}
