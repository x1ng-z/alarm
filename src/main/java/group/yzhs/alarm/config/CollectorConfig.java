package group.yzhs.alarm.config;

import com.alibaba.fastjson.JSONObject;
import group.yzhs.alarm.constant.DataResourceEnum;
import group.yzhs.alarm.model.ProductionLine;
import group.yzhs.alarm.model.dto.iot.IotMeasurePointDto;
import group.yzhs.alarm.model.dto.iot.MeasurePointsPage;
import group.yzhs.alarm.model.dto.iot.node.IotNodeDto;
import group.yzhs.alarm.model.rule.BaseRule;
import group.yzhs.alarm.utils.WXPushTools;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/6/15 14:53
 * 取数接口
 */
@Configuration
@ConfigurationProperties(prefix = "collector")
@Data
@Slf4j
public class CollectorConfig  {
    //数据源
    private String dataresurce;


    //自有接口
    private String innerurl = "http://127.0.0.1:8070";
    private String innerread = "/realdata/read";

    //iot接口
    private String ioturl;
    private String iotread = "/api/measure-point/read/latest";
    private String iotnodelist = "/api/node/list";
    private String iotMeasurePointlist = "/api/measure-point/page";


}
