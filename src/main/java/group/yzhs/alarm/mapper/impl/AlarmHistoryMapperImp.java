package group.yzhs.alarm.mapper.impl;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import group.yzhs.alarm.mapper.AlarmHistoryMapper;
import group.yzhs.alarm.model.entity.AlarmHistory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;


/**
 * @author zzx
 * @version 1.0
 * @date 2021/10/16 10:56
 */
@Service
public class AlarmHistoryMapperImp extends ServiceImpl<AlarmHistoryMapper, AlarmHistory> {
    @Resource
    private AlarmHistoryMapper alarmHistoryMapper;

    @Transactional(rollbackFor = Exception.class)
    public int deleteExpiredHistory(Date time){
        return alarmHistoryMapper.deleteExpiredHistory(time);
    }



   public AlarmHistory getLastAlatmHistoryByNodeTag(Long alarmRuleId){
         Date date=alarmHistoryMapper.getMaxCreateByNodeTag(alarmRuleId);
         if(ObjectUtils.isNotEmpty(date)){
             AlarmHistory res=alarmHistoryMapper.selectOne(Wrappers.<AlarmHistory>lambdaQuery()
                     .eq(AlarmHistory::getCreateTime,date)
                     .eq(AlarmHistory::getRefAlarmRuleId,alarmRuleId));
             return res;
         }
         return null;
    }

    public Date getMaxCreateByNodeTag(Long alarmReuleId){
        return alarmHistoryMapper.getMaxCreateByNodeTag(alarmReuleId);
    }
}
