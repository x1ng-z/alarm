package group.yzhs.alarm.mapper.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import group.yzhs.alarm.mapper.SwitchRuleMapper;
import group.yzhs.alarm.model.entity.SwitchRule;
import org.springframework.stereotype.Service;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/10/18 10:27
 */
@Service
public class SwitchRuleMapperImp extends ServiceImpl<SwitchRuleMapper, SwitchRule> {
    private SwitchRuleMapper switchRuleMapper;
}
