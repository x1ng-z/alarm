package group.yzhs.alarm.job;


import group.yzhs.alarm.config.AlarmHistoryArchiveConfig;
import group.yzhs.alarm.mapper.impl.AlarmHistoryMapperImp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 报警历史消息归档
 *
 * @author zzx
 * @date 2021年01月23日10:38:47
 */
@Component
@Slf4j
public class NodeInstanceLogArchiveJob /*implements InitializingBean*/ {
    /*m默认1天删除一次*/
    private static final Long DEFAULT_EXECUTE_PERIOD=1*24 * 3600 * 1000L;

    private AlarmHistoryMapperImp alarmHistoryMapperImp;
    private ThreadPoolTaskScheduler taskScheduler;
    private ScheduledFuture<?> localFuture=null;
    private AlarmHistoryArchiveConfig archive;

    @Autowired
    public NodeInstanceLogArchiveJob(ThreadPoolTaskScheduler taskScheduler,
                                     AlarmHistoryMapperImp alarmHistoryMapperImp,
                                     AlarmHistoryArchiveConfig archive) {
        this.taskScheduler = taskScheduler;
        this.alarmHistoryMapperImp=alarmHistoryMapperImp;
        this.archive=archive;
    }

    @PostConstruct
    public  void init(){
        if(localFuture==null){
            PeriodicTrigger periodicTrigger=new PeriodicTrigger(archive.getAlarmHistoryExecutePeriod() <= 0 ? DEFAULT_EXECUTE_PERIOD : archive.getAlarmHistoryExecutePeriod(), TimeUnit.MILLISECONDS);
            periodicTrigger.setInitialDelay(30*60*1000L);
            localFuture = taskScheduler.schedule(new LogExe(archive,alarmHistoryMapperImp), periodicTrigger);
            log.info("init execute period={}", archive.getAlarmHistoryExecutePeriod());
        }

    }

    /**
     * 每天晚上00:00执行，目前暂一天执行一次，归档前一天凌晨之前的数据，消息量多分批发mq消息归档。后续根据实际的日志量看是否需要多次执行任务，每次任务固定100条数据，单次任务中在分批发mq消息归档
     */
    private static class LogExe implements Runnable{
        private AlarmHistoryArchiveConfig archive;
        private AlarmHistoryMapperImp alarmHistoryMapperImp;

        public LogExe(AlarmHistoryArchiveConfig archive, AlarmHistoryMapperImp alarmHistoryMapperImp) {
            this.archive = archive;
            this.alarmHistoryMapperImp = alarmHistoryMapperImp;
        }

        @Override
        public void run() {
            timer();
        }

        public void timer() {
            try {
                if (!archive.getAlarmHistoryArchive()) {
                    return;
                }
                log.info("报警历史归档定时任务开始...");
                execute();
                log.info("报警历史归档定时任务结束...");
            } catch (Exception e) {
                log.error("报警历史归档定时任务异常", e);
            }
        }

        private void execute() {
            // 获取当前时间的前一天的时间点
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime oneMonthAgo = now.minusMonths(1);
            ZoneId zone = ZoneId.systemDefault();
            Date time = Date.from(oneMonthAgo.atZone(zone).toInstant());
            // 清除前一个月的报警消息
            alarmHistoryMapperImp.deleteExpiredHistory(time);
        }
    }



}
