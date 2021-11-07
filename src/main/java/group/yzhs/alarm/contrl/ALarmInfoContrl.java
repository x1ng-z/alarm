package group.yzhs.alarm.contrl;


import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import group.yzhs.alarm.config.AudioConfig;
import group.yzhs.alarm.config.CollectorConfig;
import group.yzhs.alarm.config.VersionInfo;
import group.yzhs.alarm.constant.SessionContextEnum;
import group.yzhs.alarm.constant.SysConfigEnum;
import group.yzhs.alarm.mapper.impl.SystemConfigMapperImp;
import group.yzhs.alarm.model.AlarmMessage;
import group.yzhs.alarm.model.entity.SystemConfig;
import group.yzhs.alarm.model.httpRespBody.RestHttpResponseEntity;
import group.yzhs.alarm.model.vo.alarm.AlarmDto;

import group.yzhs.alarm.model.vo.alarm.AlarmSetDto;
import group.yzhs.alarm.model.dto.view.AlarmSetInfo;
import group.yzhs.alarm.service.alarm.SystemConfigService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static group.yzhs.alarm.constant.SysConfigEnum.SYS_CONFIG_rate;

/**
 * @author zzx
 * @version 1.0
 * @date 2020/10/31 17:12
 */
@RestController
@RequestMapping("/alarm")
@Slf4j
public class ALarmInfoContrl {

    @Autowired
    private CollectorConfig config;

    @Autowired
    private AudioConfig audioConfig;

    @Autowired
    private VersionInfo versionInfo;

    @Autowired
    private SystemConfigService systemConfigService;

    @Autowired
    private SystemConfigMapperImp systemConfigMapperImp;
    /**
     *报警基础设置
     * */
    @RequestMapping("/baseSet")
    public AlarmSetDto getBaseSet(HttpSession session){

        AlarmSetDto alarmSetDto=new AlarmSetDto();
        alarmSetDto.setStatus(200);
        alarmSetDto.setMessage("报警基础配置信息");
        AlarmSetInfo alarmSetInfo=new AlarmSetInfo();

        SystemConfig  audioConfigValue=systemConfigMapperImp.getOne(Wrappers.<SystemConfig>lambdaQuery().eq(SystemConfig::getCode,SYS_CONFIG_rate.getCode()));
        if(!ObjectUtils.isEmpty(audioConfigValue)){
            alarmSetInfo.setAudioRate(Float.parseFloat(audioConfigValue.getValue()));
        }else{
            alarmSetInfo.setAudioRate(audioConfig.getRate());
        }
        SystemConfig  companyNameConfigValue=systemConfigMapperImp.getOne(Wrappers.<SystemConfig>lambdaQuery().eq(SystemConfig::getCode, SysConfigEnum.SYS_CONFIG_companyName.getCode()));
        alarmSetInfo.setCompany(companyNameConfigValue.getValue());
        alarmSetInfo.setVersion(versionInfo.getVersion());
        alarmSetDto.setData(alarmSetInfo);
        return alarmSetDto;
    }




    /**
     * 获取报警列表
     * **/
    @RequestMapping(value = "alarmList",method = RequestMethod.GET)
    public AlarmDto getAlarmInfo(HttpSession session) {

        Object alarmList = session.getAttribute(SessionContextEnum.SESSIONCONTEXT_ALARMLIST.getCode());

        if ((!ObjectUtils.isEmpty(alarmList)) && (!CollectionUtils.isEmpty((Map) alarmList))) {
            Map<String, AlarmMessage> alarmMessageMap=(Map<String, AlarmMessage>) alarmList;
            AlarmDto alarmDto=AlarmDto.builder()
                    .data(alarmMessageMap.values())
                    .message("报警列表回去成功")
                    .status(200)
                    .size(alarmMessageMap.values().size())
                    .build();
            return alarmDto;
        }else{
            AlarmDto alarmDto=AlarmDto.builder()
                    .data(new ArrayList<>())
                    .message("无报警消息")
                    .status(200)
                    .size(0)
                    .build();
            return alarmDto;
        }
    }

    /**
     * 获取语音报警消息
     * */
    @RequestMapping(value = "audioAlarmList",method = RequestMethod.GET)
    public AlarmDto getAudioAlarmList(HttpSession session) {

        Object audioList = session.getAttribute(SessionContextEnum.SESSIONCONTEXT_AUDIOLIST.getCode());
        if ((!ObjectUtils.isEmpty(audioList)) && (!CollectionUtils.isEmpty((Map) audioList))) {

            synchronized (audioList){
                Map<String, AlarmMessage> alarmMessageMap=(Map<String, AlarmMessage>) audioList;
                log.info("语音报警消息获取成功 size={}",alarmMessageMap.size());
                List<AlarmMessage> audioContextList=new ArrayList<>();
                alarmMessageMap.forEach((k,v)->{
                    v.setContext(v.getContext().replace("-","负"));
                    audioContextList.add(v);
                });
                alarmMessageMap.clear();
                AlarmDto alarmDto=AlarmDto.builder()
                        .data(audioContextList)
                        .message("语音报警消息获取成功")
                        .status(200)
                        .size(audioContextList.size())
                        .build();
                return alarmDto;
            }

        }else{
            AlarmDto alarmDto=AlarmDto.builder()
                    .data(new ArrayList<>())
                    .message("无报警消息")
                    .status(200)
                    .size(0)
                    .build();
            return alarmDto;
        }

    }


}
