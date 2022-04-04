package group.yzhs.alarm.contrl;

import group.yzhs.alarm.model.dto.alarm.SystemConfigDto;
import group.yzhs.alarm.model.dto.device.SwitchRuleDto;
import group.yzhs.alarm.model.httpRespBody.RestHttpResponseEntity;
import group.yzhs.alarm.service.JudgementService;
import group.yzhs.alarm.service.alarm.SystemConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/10/18 15:24
 */
@RestController
@Slf4j
@RequestMapping("/systemConfig")
public class SystemConfigControl {

    @Autowired
    private SystemConfigService systemConfigService;

    @Autowired
    private JudgementService judgementService;

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public RestHttpResponseEntity<Void> add(@Valid @RequestBody SystemConfigDto systemConfigDto) {
        systemConfigService.add(systemConfigDto);
        return RestHttpResponseEntity.success();
    }

    @RequestMapping(value = "/delete", method = RequestMethod.GET)
    public RestHttpResponseEntity<Void> delete(Long id) {
        systemConfigService.delete(id);
        return RestHttpResponseEntity.success();
    }

    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public RestHttpResponseEntity<Void> update(@Valid @RequestBody SystemConfigDto systemConfigDto) {
        systemConfigService.update(systemConfigDto);
        return RestHttpResponseEntity.success();
    }


    @RequestMapping(value = "/get", method = RequestMethod.GET)
    public RestHttpResponseEntity<List<SystemConfigDto>> get() {
        return RestHttpResponseEntity.success(systemConfigService.get());
    }


    @RequestMapping(value = "/errorMessage", method = RequestMethod.GET)
    public RestHttpResponseEntity<String> errorMessage() {
        return RestHttpResponseEntity.success(judgementService.getErrormessage());
    }

    @RequestMapping(value = "/getGroupSets")
    public RestHttpResponseEntity<List> getGroupSets(@RequestParam("groupName") String groupName, @RequestParam("code") String code) {
        return RestHttpResponseEntity.success(systemConfigService.findPropertiesByGroupAndCode(groupName, code));
    }


}
