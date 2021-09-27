package group.yzhs.alarm.config;

import lombok.Data;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/9/2 11:22
 */
@Configuration
@Data
@ConfigurationProperties(prefix = "app")
public class VersionInfo {
    private String version="1.21.09.27.1";
}
