package group.yzhs.alarm.service.alarm;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import group.yzhs.alarm.exception.ParameterException;
import group.yzhs.alarm.mapper.impl.AlarmHistoryMapperImp;
import group.yzhs.alarm.model.dto.alarm.AlarmHistoryDto;
import group.yzhs.alarm.model.entity.AlarmHistory;
import group.yzhs.alarm.model.vo.page.BasePageParamDto;
import group.yzhs.alarm.model.vo.page.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/10/16 18:54
 */
@Service
@Slf4j
public class AlarmHistoryService {
    @Autowired
    private AlarmHistoryMapperImp alarmHistoryMapperImp;

    @Transactional(rollbackFor = Exception.class)
    public void add(AlarmHistoryDto alarmHistoryDto){
        Date time=Optional.ofNullable(alarmHistoryDto).map(AlarmHistoryDto::getAlarmTime).orElseGet(Date::new);
        alarmHistoryDto.setAlarmTime(time);
        AlarmHistory alarmHistory=new AlarmHistory();
        BeanUtils.copyProperties(alarmHistoryDto,alarmHistory);
        alarmHistoryMapperImp.save(alarmHistory);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id){
        Optional.ofNullable(id).orElseThrow(()->new ParameterException("id为空"));
        alarmHistoryMapperImp.remove(Wrappers.<AlarmHistory>lambdaQuery().eq(AlarmHistory::getId,id));
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(AlarmHistoryDto alarmHistoryDto){
        Optional.ofNullable(alarmHistoryDto).map(AlarmHistoryDto::getId).orElseThrow(()->new ParameterException("id为空"));
        Date time=Optional.of(alarmHistoryDto).map(AlarmHistoryDto::getAlarmTime).orElseGet(Date::new);
        alarmHistoryDto.setAlarmTime(time);
        alarmHistoryMapperImp.update(Wrappers.<AlarmHistory>lambdaUpdate()
                .eq(AlarmHistory::getId,alarmHistoryDto.getId())
                .set(AlarmHistory::getAlarmContext,alarmHistoryDto.getAlarmContext())
                .set(AlarmHistory::getAlarmTime,alarmHistoryDto.getAlarmTime())
        );
    }


    public PageInfo<AlarmHistoryDto> pageAlarmHistory(BasePageParamDto pageParamDto){
        Page<AlarmHistory>  alarmHistoryPage=alarmHistoryMapperImp.page(new Page<AlarmHistory>(pageParamDto.getCurrent(),pageParamDto.getPageSize()));
        PageInfo<AlarmHistoryDto> pageInfo =new PageInfo<AlarmHistoryDto>();
        pageInfo.setPages(alarmHistoryPage.getPages());
        pageInfo.setTotal(alarmHistoryPage.getTotal());
        pageInfo.setCurrent(pageParamDto.getCurrent());
        List<AlarmHistoryDto> alarmHistoryList=new ArrayList<>();
        List<AlarmHistory> alarmHistories=alarmHistoryPage.getRecords();
        if(CollectionUtils.isNotEmpty(alarmHistories)){
            alarmHistories.forEach(a->{
                AlarmHistoryDto alarmHistoryDto=AlarmHistoryDto.builder().build();
                BeanUtils.copyProperties(a,alarmHistoryDto);
                alarmHistoryList.add(alarmHistoryDto);
            });
        }
        pageInfo.setList(alarmHistoryList);
        pageInfo.setCurrentSize(alarmHistoryList.size());
        return pageInfo;
    }


    public AlarmHistoryDto get(Long id){
        Optional.ofNullable(id).orElseThrow(()->new ParameterException("id为空"));
        AlarmHistory alarmHistory=alarmHistoryMapperImp.getOne(Wrappers.<AlarmHistory>lambdaQuery().eq(AlarmHistory::getId,id));
        AlarmHistoryDto res=new AlarmHistoryDto();
        BeanUtils.copyProperties(alarmHistory,res);
       return res;
    }
}