package group.yzhs.alarm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import group.yzhs.alarm.model.entity.AlarmHistory;
import org.apache.ibatis.annotations.Param;

import java.lang.annotation.Documented;
import java.util.Date;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/10/16 10:22
 */
public interface AlarmHistoryMapper extends BaseMapper<AlarmHistory> {
    /**
     * 删除过期的报警
     * */
    int deleteExpiredHistory(@Param("time") Date time);

    Date getMaxCreateByNodeTag(@Param("alarmRuleId")Long alarmRuleId);
}
