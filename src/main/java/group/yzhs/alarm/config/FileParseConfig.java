package group.yzhs.alarm.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/6/15 10:55
 * 报警规则配置文件
 */
@Configuration
@ConfigurationProperties(prefix = "rules")
@Data
public class FileParseConfig {
    private  String filelocation;
}
