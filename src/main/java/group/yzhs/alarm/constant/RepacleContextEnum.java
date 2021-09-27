package group.yzhs.alarm.constant;

import group.yzhs.alarm.model.rule.BaseRule;
import lombok.Getter;


import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/6/16 10:07
 * 占位符替换类
 */
@Getter
public enum RepacleContextEnum {

    REPLACE_TIME("_time_","时间"){
        @Override
        public <T extends BaseRule> void replacePlaceholderContext(T rule) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            rule.setPushWXContext(rule.getPushWXContext().replace(getCode(),formatter.format(LocalDateTime.now())));
        }

    },

    REPLACE_VALUE("_value_","值") {
        @Override
        public <T extends BaseRule> void replacePlaceholderContext(T rule) {
            rule.setPushWXContext(rule.getPushWXContext().replace(getCode(), new DecimalFormat("#.###").format(rule.getValue())));
            rule.setPushAudioContext(rule.getPushAudioContext().replace(getCode(), new DecimalFormat("#.###").format(rule.getValue())));

        }

        @Override
        public <T extends BaseRule> void removePlaceholderContext(T rule) {
            //do nothing
        }
    };

    abstract public  <T extends BaseRule>  void replacePlaceholderContext(T rule);

    public <T extends BaseRule> void removePlaceholderContext(T rule) {
        rule.setPushAudioContext(rule.getPushAudioContext().replace(getCode(),""));
    }


    RepacleContextEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    private String code;
    private String name;
}
