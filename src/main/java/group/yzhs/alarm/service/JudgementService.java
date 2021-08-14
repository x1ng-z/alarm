package group.yzhs.alarm.service;


import group.yzhs.alarm.config.CollectorConfig;
import group.yzhs.alarm.config.WXPushConfig;
import group.yzhs.alarm.constant.DataResourceEnum;
import group.yzhs.alarm.model.ProductionLine;
import group.yzhs.alarm.model.dto.inner.OceanRequestDto;
import group.yzhs.alarm.model.dto.inner.OceanResponseDto;
import group.yzhs.alarm.model.dto.iot.IotDcsReadDto;
import group.yzhs.alarm.model.dto.iot.IotReadNodeInfo;
import group.yzhs.alarm.model.dto.iot.IotResponse;
import group.yzhs.alarm.model.rule.BaseRule;
import group.yzhs.alarm.service.alarmHandle.Handler;
import group.yzhs.alarm.utils.WXPushTools;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/6/15 15:00
 */
@Service
@Data
@Slf4j
public class JudgementService {

    @Autowired
    private XmlService xmlService;

    @Autowired
    private List<Handler> handleList;


    @Autowired
    private ExecutorService executorService;

    @Autowired
    private WXPushConfig wxPushConfig;

    private Map<String, Handler> handlerPool = new HashMap<>();

    private Map<String, List<BaseRule>> rules = null;

    private CollectorConfig config=null;

    public JudgementService(CollectorConfig config) {
        this.config = config;
        init();
    }


    public void init() {
        List<ProductionLine>  productionLines=config.getProductionLines();
        rules = config.getRules();

    }


    @Scheduled(fixedRate = 1000 * 5, initialDelay = 1000 * 10)
    public void run() {
        try {
            //获取数据
            StringJoiner tags = new StringJoiner(",", "", "");
            if (!CollectionUtils.isEmpty(rules.keySet())) {
                rules.keySet().stream().forEach(r -> {
                    tags.add(r);
                });
            }

//            OceanRequestDto oceanRequestDto = OceanRequestDto.builder().tags(tags.toString()).build();
//
//            //组合数据
//            MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
//            map.add("tags", oceanRequestDto.getTags());
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
//            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(map, headers);
            //请求数据
            log.debug("try to request lastest data");
            IotDcsReadDto iotDcsReadDto=new IotDcsReadDto();
            iotDcsReadDto.setSample(1);

            List<IotReadNodeInfo> points=new ArrayList<>();
            if(!CollectionUtils.isEmpty(rules)){
                rules.values().forEach(r->{
                    IotReadNodeInfo point=IotReadNodeInfo.builder()
                            .measurePoint(r.get(0).getTag())
                            .node(r.get(0).getIotNode())
                            .build();
                    points.add(point);
                });
            }else{
                log.warn("have no any rules");
                return;
            }


            iotDcsReadDto.setPoints(points);
            IotResponse responseEntity=null;
            if(DataResourceEnum.DATA_RESOURCE_IOT.getCode().equals(config.getDataresurce())){
                responseEntity=WXPushTools.postForEntity(config.getIoturl() + config.getIotread(), iotDcsReadDto, IotResponse.class);
            }else if(DataResourceEnum.DATA_RESOURCE_DIR.getCode().equals(config.getDataresurce())){

            }

            //更新数据并判断
            if (responseEntity.getStatus().equals(200)) {
                if (!CollectionUtils.isEmpty(responseEntity.getData())) {
                    responseEntity.getData().forEach(p-> {

                        if (!CollectionUtils.isEmpty(rules)) {
                            List<BaseRule> ruleslist = rules.get(p.getMeasurePoint());

                            if (!CollectionUtils.isEmpty(ruleslist)) {

                                ruleslist.stream().forEach(r -> {
                                    //更新数据
                                        if(!CollectionUtils.isEmpty(p.getData())){
                                            r.setValue(p.getData().get(0).getValue());
                                            executorService.execute(new Runnable() {
                                                @Override
                                                public void run() {
                                                    getHandlerPool().get(r.getAlarmModelEnum().getCode()).handle(r);
                                                }
                                            });
                                        }
                                });
                            }
                        }
                    });
                }
            }
        }catch (Exception e){
            log.error(e.getMessage(),e);
        }



    }


    public synchronized Map<String, Handler> getHandlerPool() {
        if (CollectionUtils.isEmpty(handlerPool)) {
            if (!CollectionUtils.isEmpty(handleList)) {
                handlerPool = handleList.stream().collect(Collectors.toMap(h -> h.getCode(), h -> h, (o, n) -> n));
            }
        }
        return handlerPool;
    }


}
