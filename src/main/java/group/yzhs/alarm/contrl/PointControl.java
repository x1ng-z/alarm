package group.yzhs.alarm.contrl;

import group.yzhs.alarm.model.dto.device.DeviceDto;
import group.yzhs.alarm.model.dto.device.PointDto;
import group.yzhs.alarm.model.httpRespBody.RestHttpResponseEntity;
import group.yzhs.alarm.service.alarm.PointService;
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
    public RestHttpResponseEntity<List<PointDto>> get(@RequestParam("id") Long deviceId) {
        return RestHttpResponseEntity.success( pointService.getByDevicId(deviceId));
    }


    @RequestMapping(value = "/export", method = RequestMethod.GET)
    public void export(HttpServletResponse httpServletResponse){
        pointService.export(httpServletResponse);
    }


    @RequestMapping(value = "/import", method = RequestMethod.POST)
    public RestHttpResponseEntity<Void> imp0rt(@RequestPart("excel-file") MultipartFile file){
        pointService.imp0rt(file);
        return RestHttpResponseEntity.success();
    }




}
