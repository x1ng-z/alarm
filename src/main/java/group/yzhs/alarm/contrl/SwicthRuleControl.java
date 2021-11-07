package group.yzhs.alarm.contrl;

import group.yzhs.alarm.model.dto.device.SwitchDto;
import group.yzhs.alarm.model.dto.device.SwitchRuleDto;
import group.yzhs.alarm.model.httpRespBody.RestHttpResponseEntity;
import group.yzhs.alarm.service.alarm.SwitchRuleService;
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
@RequestMapping("/switchRule")
public class SwicthRuleControl {

    @Autowired
    private SwitchRuleService switchRuleService;


    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public RestHttpResponseEntity<Void> add(@Valid @RequestBody SwitchRuleDto switchRuleDto) {
        switchRuleService.add(switchRuleDto);
        return RestHttpResponseEntity.success();
    }

    @RequestMapping(value = "/delete", method = RequestMethod.GET)
    public RestHttpResponseEntity<Void> delete(Long id) {
        switchRuleService.delete(id);
        return RestHttpResponseEntity.success();
    }

    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public RestHttpResponseEntity<Void> update(@Valid @RequestBody SwitchRuleDto switchRuleDto) {
        switchRuleService.update(switchRuleDto);
        return RestHttpResponseEntity.success();
    }


    @RequestMapping(value = "/getBySwitchId/{id}", method = RequestMethod.GET)
    public RestHttpResponseEntity<List<SwitchRuleDto>> getBySwitchId(@PathVariable("id") Long switchId) {
        return RestHttpResponseEntity.success(switchRuleService.get(switchId));
    }


    @RequestMapping(value = "/export", method = RequestMethod.GET)
    public void export(HttpServletResponse httpServletResponse){
        switchRuleService.export2(httpServletResponse);
    }


    @RequestMapping(value = "/import", method = RequestMethod.POST)
    public RestHttpResponseEntity<Void> imp0rt(@RequestPart("excel-file") MultipartFile file){
        switchRuleService.imp0rt(file);
        return RestHttpResponseEntity.success();
    }


}
