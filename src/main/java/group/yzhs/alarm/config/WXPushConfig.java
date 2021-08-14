package group.yzhs.alarm.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/6/16 10:40
 * 微信推送配置
 */
@Configuration
@Data
@ConfigurationProperties(prefix = "wx")
public class WXPushConfig {
    private String url;
    private String department;
    private Long pushIntervalSec;
    private Long continueAlarmSec;

}
