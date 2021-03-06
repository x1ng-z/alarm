package group.yzhs.alarm.service.alarm;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import group.yzhs.alarm.exception.ParameterException;
import group.yzhs.alarm.mapper.impl.SwitchMapperImp;
import group.yzhs.alarm.mapper.impl.SystemConfigMapperImp;
import group.yzhs.alarm.model.dto.alarm.SystemConfigDto;
import group.yzhs.alarm.model.dto.device.SwitchDto;
import group.yzhs.alarm.model.entity.Switch;
import group.yzhs.alarm.model.entity.SystemConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/10/18 11:30
 */
@Service
@Slf4j
public class SystemConfigService {
    //字典:工艺
    private static final String DICTIONARY_PROCESS = "process";
    private static final String DICTIONARY_PROCESS_NAME = "processName";
    @Autowired
    private SystemConfigMapperImp systemConfigMapperImp;

    @Transactional(rollbackFor = Exception.class)
    public void add(SystemConfigDto systemConfigDto) {
        List<SystemConfig> db_res = systemConfigMapperImp.list(Wrappers.<SystemConfig>lambdaQuery().eq(SystemConfig::getCode, systemConfigDto.getCode()));
        if (CollectionUtils.isNotEmpty(db_res)) {
            throw new ParameterException("编码已经存在");
        }
        SystemConfig systemConfig = new SystemConfig();
        BeanUtils.copyProperties(systemConfigDto, systemConfig);
        systemConfigMapperImp.save(systemConfig);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Optional.ofNullable(id).orElseThrow(() -> new ParameterException("系统配置id为空"));
        systemConfigMapperImp.remove(Wrappers.<SystemConfig>lambdaQuery().eq(SystemConfig::getId, id));
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(SystemConfigDto systemConfigDto) {
        Optional.ofNullable(systemConfigDto).map(SystemConfigDto::getId).orElseThrow(() -> new ParameterException("系统配置id为空"));
        /*
        List<SystemConfig> db_res=systemConfigMapperImp.list(Wrappers.<SystemConfig>lambdaQuery().eq(SystemConfig::getCode,systemConfigDto.getCode()));
        if(CollectionUtils.isNotEmpty(db_res)&&db_res.size()>1){
            throw new ParameterException("编码已经存在");
        }*/

        SystemConfig systemConfig = new SystemConfig();
        BeanUtils.copyProperties(systemConfigDto, systemConfig);
        systemConfigMapperImp.update(Wrappers.<SystemConfig>lambdaUpdate().eq(SystemConfig::getId, systemConfigDto.getId()).set(SystemConfig::getValue, systemConfigDto.getValue()));
    }

    public List<SystemConfigDto> get() {
        List<SystemConfig> db_res = systemConfigMapperImp.list();
        List<SystemConfigDto> res = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(db_res)) {
            db_res.forEach(d -> {
                SystemConfigDto systemConfigDto = new SystemConfigDto();
                BeanUtils.copyProperties(d, systemConfigDto);
                res.add(systemConfigDto);
            });
        }
        return res;
    }

    public List<SystemConfigDto> findPropertiesByGroupAndCode(String groupName, String code) {
        List<SystemConfig> db_res = systemConfigMapperImp.list(Wrappers.<SystemConfig>lambdaQuery().eq(SystemConfig::getCode, code).eq(SystemConfig::getConfigGroup, groupName));
        List<SystemConfigDto> res = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(db_res)) {
            db_res.forEach(d -> {
                SystemConfigDto systemConfigDto = new SystemConfigDto();
                BeanUtils.copyProperties(d, systemConfigDto);
                res.add(systemConfigDto);
            });
        }
        return res;
    }

    public List<SystemConfigDto> findPropertiesByGroupAndCode(String groupName) {
        List<SystemConfig> db_res = systemConfigMapperImp.list(Wrappers.<SystemConfig>lambdaQuery().eq(SystemConfig::getConfigGroup, groupName));
        List<SystemConfigDto> res = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(db_res)) {
            db_res.forEach(d -> {
                SystemConfigDto systemConfigDto = new SystemConfigDto();
                BeanUtils.copyProperties(d, systemConfigDto);
                res.add(systemConfigDto);
            });
        }
        return res;
    }


    /**
     * 获取工艺名称字典设置，list返回内容形如：生料-sl,烧成-sc,制成-zc
     * */
    public List<String> getProcess() {
        List<SystemConfigDto> db_res = findPropertiesByGroupAndCode(DICTIONARY_PROCESS, DICTIONARY_PROCESS_NAME);
        List<String> res = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(db_res)) {
            db_res.forEach(d -> {
                if (StringUtils.isNotBlank(d.getValue())) {
                    String[] splitProcesss = d.getValue().split(",");
                    Arrays.stream(splitProcesss).forEach(
                            process -> {
                                res.add(process);
                            }
                    );

                }
            });
        }
        return res;
    }


}
