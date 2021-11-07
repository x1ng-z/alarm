package group.yzhs.alarm.job;

import group.yzhs.alarm.service.JudgementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/11/5 13:52
 */
@Slf4j
@Service
public class JudgementJob {
    @Autowired
    private JudgementService judgementService;


    @Scheduled(fixedRate = 1000 * 5, initialDelay = 1000 * 10)
    public void run() {
        try {
            judgementService.judge();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
