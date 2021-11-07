package group.yzhs.alarm;

import com.alibaba.fastjson.JSON;
import group.yzhs.alarm.model.AlarmMessage;
import group.yzhs.alarm.model.ProductionLine;
import group.yzhs.alarm.service.JudgementService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

@SpringBootTest
class AlarmApplicationTests {


    @Autowired
    public JudgementService judgementService;
    @Test
    void contextLoads() {

        AlarmMessage alarmMessage = AlarmMessage.builder()
                .context("")
                .date(new Date())
                .level(0L)
                .product("")
                .rate(0.0)
                .value(0.1234)
                .alarmId("")
                .build();

        System.out.println(JSON.toJSONString(alarmMessage));


        LocalDateTime n=LocalDateTime.now();
        Long aa=Instant.now().toEpochMilli();

        LocalDateTime nn=n.minus(300, ChronoUnit.SECONDS);

        System.out.println(Duration.between(LocalDateTime.now(),LocalDateTime.now()).getSeconds());

        System.out.println(nn.isBefore(LocalDateTime.now()));


//            List<ProductionLine>  productionLines=judgementService.getXmlService().Find();
//        System.out.println(productionLines.size());

    }

}
