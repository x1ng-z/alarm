package group.yzhs.alarm.service.alarm;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import group.yzhs.alarm.config.AlarmHistoryArchiveConfig;
import group.yzhs.alarm.constant.AlarmPushStatusEnum;
import group.yzhs.alarm.constant.SysConfigEnum;
import group.yzhs.alarm.exception.ParameterException;
import group.yzhs.alarm.mapper.impl.*;
import group.yzhs.alarm.model.dto.alarm.AlarmHistoryDto;
import group.yzhs.alarm.model.dto.alarm.AlarmPushDto;
import group.yzhs.alarm.model.entity.*;
import group.yzhs.alarm.model.vo.page.BasePageParamDto;
import group.yzhs.alarm.model.vo.page.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/10/16 18:54
 */
@Service
@Slf4j
public class AlarmHistoryService {
    @Autowired
    private AlarmHistoryMapperImp alarmHistoryMapperImp;

    @Autowired
    private AlarmRuleMapperImp alarmRuleMapperImp;

    @Autowired
    private PointMapperImp pointMapperImp;


    @Autowired
    private AlarmClassMapperImp alarmClassMapperImp;

    @Autowired
    private DeviceMapperImp deviceMapperImp;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private AlarmHistoryArchiveConfig alarmHistoryArchiveConfig;

    @Autowired
    private SystemConfigMapperImp systemConfigMapperImp;

    @Transactional(rollbackFor = Exception.class)
    public void add(AlarmHistoryDto alarmHistoryDto){
        Date time=Optional.ofNullable(alarmHistoryDto).map(AlarmHistoryDto::getAlarmTime).orElseGet(Date::new);
        alarmHistoryDto.setAlarmTime(time);
        AlarmHistory alarmHistory=new AlarmHistory();
        BeanUtils.copyProperties(alarmHistoryDto,alarmHistory);
        alarmHistoryMapperImp.save(alarmHistory);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id){
        Optional.ofNullable(id).orElseThrow(()->new ParameterException("id为空"));
        alarmHistoryMapperImp.remove(Wrappers.<AlarmHistory>lambdaQuery().eq(AlarmHistory::getId,id));
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(AlarmHistoryDto alarmHistoryDto){
        Optional.ofNullable(alarmHistoryDto).map(AlarmHistoryDto::getId).orElseThrow(()->new ParameterException("id为空"));
        Date time=Optional.of(alarmHistoryDto).map(AlarmHistoryDto::getAlarmTime).orElseGet(Date::new);
        alarmHistoryDto.setAlarmTime(time);
        alarmHistoryMapperImp.update(Wrappers.<AlarmHistory>lambdaUpdate()
                .eq(AlarmHistory::getId,alarmHistoryDto.getId())
                .set(AlarmHistory::getAlarmContext,alarmHistoryDto.getAlarmContext())
                .set(AlarmHistory::getCreateTime,alarmHistoryDto.getAlarmTime())
        );
    }


    public PageInfo<AlarmHistoryDto> pageAlarmHistory(BasePageParamDto pageParamDto){
        Page<AlarmHistory>  alarmHistoryPage=alarmHistoryMapperImp.page(new Page<AlarmHistory>(pageParamDto.getCurrent(),pageParamDto.getPageSize()));
        PageInfo<AlarmHistoryDto> pageInfo =new PageInfo<AlarmHistoryDto>();
        pageInfo.setPages(alarmHistoryPage.getPages());
        pageInfo.setTotal(alarmHistoryPage.getTotal());
        pageInfo.setCurrent(pageParamDto.getCurrent());
        List<AlarmHistoryDto> alarmHistoryList=new ArrayList<>();
        List<AlarmHistory> alarmHistories=alarmHistoryPage.getRecords();
        if(CollectionUtils.isNotEmpty(alarmHistories)){
            alarmHistories.forEach(a->{
                AlarmHistoryDto alarmHistoryDto=AlarmHistoryDto.builder().build();
                BeanUtils.copyProperties(a,alarmHistoryDto);
                alarmHistoryList.add(alarmHistoryDto);
            });
        }
        pageInfo.setList(alarmHistoryList);
        pageInfo.setCurrentSize(alarmHistoryList.size());
        return pageInfo;
    }


    public AlarmHistoryDto get(Long id){
        Optional.ofNullable(id).orElseThrow(()->new ParameterException("id为空"));
        AlarmHistory alarmHistory=alarmHistoryMapperImp.getOne(Wrappers.<AlarmHistory>lambdaQuery().eq(AlarmHistory::getId,id));
        AlarmHistoryDto res=new AlarmHistoryDto();
        BeanUtils.copyProperties(alarmHistory,res);
       return res;
    }

    public void push(Long id){
        Optional.ofNullable(id).orElseThrow(()->new ParameterException("id为空"));
        AlarmHistory alarmHistory=alarmHistoryMapperImp.getById(id);
        Optional.ofNullable(alarmHistory).orElseThrow(()->new ParameterException("查询不到指定的报警记录"));

        Long refAlarmRuleId=alarmHistory.getRefAlarmRuleId();

        AlarmRule alarmRule =alarmRuleMapperImp.getById(refAlarmRuleId);
        Optional.ofNullable(alarmRule).orElseThrow(()->new ParameterException("查询不到关联的报警规则"));

        Point point =pointMapperImp.getById(alarmRule.getPointId());
        Optional.ofNullable(point).orElseThrow(()->new ParameterException("查询不到关联的点位"));
        Device device =deviceMapperImp.getById(point.getRefDeviceId());
        Optional.ofNullable(device).orElseThrow(()->new ParameterException("查询不到关联的设备"));

        AlarmClass alarmClass =alarmClassMapperImp.getById(alarmRule.getAlarmClassId());
        Optional.ofNullable(alarmClass).orElseThrow(()->new ParameterException("查询不到关联的报警类别"));

        SystemConfig systemConfig=systemConfigMapperImp.getOne(Wrappers.<SystemConfig>lambdaQuery().eq(SystemConfig::getCode, SysConfigEnum.SYS_CONFIG_companyCode.getCode()));
        Optional.ofNullable(systemConfig).orElseThrow(()->new ParameterException("查询不到公司编码"));

        AlarmPushDto alarmPushDto=AlarmPushDto.builder()
                .companyCode(systemConfig.getValue())
                .deviceCode(device.getDeviceNo())
                .remarks(alarmHistory.getAlarmContext())
                .riskType(alarmClass.getName())
                .typeCode(alarmClass.getCode())
                .build();
        ResponseEntity<String> responseEntity=restTemplate.postForEntity(alarmHistoryArchiveConfig.getAlarmPush(),alarmPushDto,String.class);
        alarmHistory.setPushStatus(AlarmPushStatusEnum.ALARM_PUSH_STATUS_PUSHED);
        alarmHistoryMapperImp.updateById(alarmHistory);

    }

}
