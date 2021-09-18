package group.yzhs.alarm.config;

import com.alibaba.fastjson.JSONObject;
import group.yzhs.alarm.constant.DataResourceEnum;
import group.yzhs.alarm.model.ProductionLine;
import group.yzhs.alarm.model.dto.iot.IotMeasurePointDto;
import group.yzhs.alarm.model.dto.iot.MeasurePointsPage;
import group.yzhs.alarm.model.dto.iot.node.IotNodeDto;
import group.yzhs.alarm.model.rule.BaseRule;
import group.yzhs.alarm.service.XmlService;
import group.yzhs.alarm.utils.WXPushTools;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
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
public class CollectorConfig implements InitializingBean {
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

    private Map<String, String> measurepoints = new HashMap();


    private XmlService xmlService;
    //key=tag value=rules
    private Map<String, List<BaseRule>> rules = new HashMap<>();

    public CollectorConfig(XmlService xmlService) {
        this.xmlService = xmlService;
    }

    List<ProductionLine> productionLines = null;


    @Override
    public void afterPropertiesSet() throws Exception {

        productionLines = xmlService.Find();
        log.debug("rule paser comple");
        if(!CollectionUtils.isEmpty(productionLines)){

            productionLines.stream().forEach(p -> {
                p.getDevices().stream().forEach(d -> {
                    Map<String, List<BaseRule>> devReules = d.getRules().stream().collect(Collectors.groupingBy(BaseRule::getTag));
                    if (!CollectionUtils.isEmpty(devReules)) {
                        devReules.forEach((k, v) -> {
                            if (!rules.containsKey(k)) {
                                rules.put(k, new ArrayList<>());
                            }
                            rules.get(k).addAll(v);
                        });
                    }
                });
            });
        }



        if (!StringUtils.isEmpty(dataresurce)) {
            if (DataResourceEnum.DATA_RESOURCE_IOT.getCode().equals(dataresurce)) {
                //获取所有节点
                List<String> nodeList = new ArrayList<>();
                String nodecontext = WXPushTools.doget(ioturl + iotnodelist, new HashMap<>());
                if (!StringUtils.isEmpty(nodecontext)) {
                    IotNodeDto iotNodeDto = JSONObject.parseObject(nodecontext, IotNodeDto.class);
                    if (iotNodeDto.getStatus() == 200) {
                        if (!CollectionUtils.isEmpty(iotNodeDto.getData())) {
                            iotNodeDto.getData().stream().filter(node -> node.get_switch()).forEach(node -> {
                                nodeList.add(node.getCode());
                            });
                        }

                    }
                }
                log.debug("iot node request comple");
                //获取所有节点中位号名称
                for (String node : nodeList) {
                    int pageNum = 1;
                    int total = 0;
                    do {
                        MeasurePointsPage measurePointsPage = MeasurePointsPage.builder()
                                .pointType(1)
                                .pageNum(pageNum)
                                .nodeCode(node)
                                .build();
                        IotMeasurePointDto iotMeasurePointDto = WXPushTools.postForEntity(ioturl + iotMeasurePointlist, measurePointsPage, IotMeasurePointDto.class);
                        pageNum++;
                        if (!ObjectUtils.isEmpty(iotMeasurePointDto.getData())) {


                            if (!CollectionUtils.isEmpty(iotMeasurePointDto.getData().getList())) {
                                iotMeasurePointDto.getData().getList().forEach(p -> {
//                                    measurepoints.put(p.getCode(),node);
                                    if (!CollectionUtils.isEmpty(rules.get(p.getCode()))) {
                                        rules.get(p.getCode()).forEach(rule -> {
                                            rule.setIotNode(node);
                                        });
                                    }
                                });
                            }


                            total = iotMeasurePointDto.getData().getPages();
                        }

                    } while (pageNum <= total);
                }
                log.debug("iot measurePoints request comple");
                //check is exit a no node tag
                Set<String> noNodeTags = new HashSet<>();
                if (!CollectionUtils.isEmpty(rules)) {
                    rules.forEach((k, v) -> {
                        if(!CollectionUtils.isEmpty(v)){
                            v.forEach(r->{
                                if (StringUtils.isEmpty(r.getIotNode())) {
                                    noNodeTags.add(r.getTag());
                                }
                            });
                        }

                    });
                }

                if (!CollectionUtils.isEmpty(noNodeTags)) {
                    log.error("请检查以下位号在iot中是否存在={}", noNodeTags.toString());
                    throw new RuntimeException("配置参数不完整,请检查以下点号:"+noNodeTags.toString());
                }


            }
        }
    }

}
