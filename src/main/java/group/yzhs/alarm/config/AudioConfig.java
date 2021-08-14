package group.yzhs.alarm.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/8/13 16:06
 */
@Configuration
@ConfigurationProperties(prefix = "audio")
@Data
public class AudioConfig {
    private float rate;
}
