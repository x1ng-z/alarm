package group.yzhs.alarm.model;

import group.yzhs.alarm.model.rule.BaseRule;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
@Slf4j
@Builder
public class Device {
    //工艺名称
    private String name;
    //工艺名称代码
    private String dNo;
    //报警基本规则
    private List<BaseRule> rules;
}
