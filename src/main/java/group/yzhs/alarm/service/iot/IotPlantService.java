package group.yzhs.alarm.service.iot;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import group.yzhs.alarm.config.CollectorConfig;
import group.yzhs.alarm.constant.SysConfigEnum;
import group.yzhs.alarm.mapper.impl.PointMapperImp;
import group.yzhs.alarm.mapper.impl.SystemConfigMapperImp;
import group.yzhs.alarm.model.dto.iot.IotDcsReadDto;
import group.yzhs.alarm.model.dto.iot.IotMeasurePointValueDto;
import group.yzhs.alarm.model.dto.iot.IotReadNodeInfo;
import group.yzhs.alarm.model.dto.iot.IotResponse;
import group.yzhs.alarm.model.entity.Point;
import group.yzhs.alarm.model.entity.SystemConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zzx
 * @version 1.0
 * @date 2022/7/5 11:19
 */
@Service
@Slf4j
public class IotPlantService {


    /**
     * 获取最新的数据
     */
    public List<IotMeasurePointValueDto> getLatest() {
        List<Point> pointList = pointMapperImp.list().stream().filter(point -> !StringUtils.isEmpty(point.getTag())).collect(Collectors.toList());
        IotDcsReadDto iotDcsReadDto = new IotDcsReadDto();
        iotDcsReadDto.setSample(1);
        List<IotReadNodeInfo> points = new ArrayList<>();
        iotDcsReadDto.setPoints(points);
        if (!CollectionUtils.isEmpty(pointList)) {
            pointList.forEach(p -> {
                IotReadNodeInfo point = IotReadNodeInfo.builder()
                        .measurePoint(p.getTag())
                        .node(p.getNodeCode())
                        .build();
                points.add(point);
            });
        } else {
            log.warn("have no any rules");
            return null;
        }
        SystemConfig iotConfig = systemConfigMapperImp.getOne(Wrappers.<SystemConfig>lambdaQuery().eq(SystemConfig::getCode, SysConfigEnum.SYS_CONFIG_ioturl.getCode()));
        ResponseEntity<IotResponse> iotResponse = restTemplate.postForEntity(iotConfig.getValue() + config.getIotread(), iotDcsReadDto, IotResponse.class);
        if (iotResponse.getStatusCode().equals(HttpStatus.OK)) {
            IotResponse iotResponseBody = iotResponse.getBody();
            if (Integer.valueOf(HttpStatus.OK.value()).equals(iotResponseBody.getStatus())) {
                if (!CollectionUtils.isEmpty(iotResponseBody.getData())) {
                    return iotResponseBody.getData();
                }
            } else {
                log.error("iot 数据获取异常={}", iotResponseBody.getMessage());
            }
        }
        return null;
    }

    @Autowired
    private PointMapperImp pointMapperImp;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private SystemConfigMapperImp systemConfigMapperImp;

    @Autowired
    private CollectorConfig config;


}
