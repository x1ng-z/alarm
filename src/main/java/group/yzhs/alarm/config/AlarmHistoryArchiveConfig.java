package group.yzhs.alarm.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 报警日志归档定时任务开关配置类
 * @author chenpiwei
 * @date 2021年01月23日15:59:50
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Configuration
public class AlarmHistoryArchiveConfig {

    /**
     * 组件实例运行日志归档定时任务开关 默认true打开
     */
    @Value("${alarm-history-archive:true}")
    private Boolean archive;

    @Value("${alarm-history-execute-period:8748000000}")//30days
    private Long period;
}
