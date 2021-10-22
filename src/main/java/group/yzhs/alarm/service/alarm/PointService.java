package group.yzhs.alarm.service.alarm;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.common.collect.Lists;
import group.yzhs.alarm.constant.DeviceSwitchRuleEnum;
import group.yzhs.alarm.exception.ParameterException;
import group.yzhs.alarm.mapper.impl.*;
import group.yzhs.alarm.model.dto.alarm.AlarmRuleDto;
import group.yzhs.alarm.model.dto.device.*;
import group.yzhs.alarm.model.entity.*;
import group.yzhs.alarm.service.valid.Success;
import group.yzhs.alarm.service.valid.Valider;
import group.yzhs.alarm.utils.ExcelUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/10/18 11:03
 */
@Service
@Slf4j
public class PointService {
    @Autowired
    private PointMapperImp pointMapperImp;

    @Autowired
    private AlarmRuleService alarmRuleService;

    @Autowired
    private AlarmRuleSwitchMapMapperImp alarmRuleSwitchMapMapperImp;

    @Autowired
    private DeviceMapperImp deviceMapperImp;


    @Autowired
    private DeviceService deviceService;

    @Transactional(rollbackFor = Exception.class)
    public void add(PointDto pointDto){
        //设备是否存在
        List<Device> devices=deviceMapperImp.list(Wrappers.<Device>lambdaQuery().eq(Device::getId,pointDto.getRefDeviceId()));
        if(CollectionUtils.isEmpty(devices)){
            throw new ParameterException("不存在指定id的设备");
        }
        List<Point> existPoints=pointMapperImp.list(Wrappers.<Point>lambdaQuery().eq(Point::getTag,pointDto.getTag()).eq(Point::getNodeCode,pointDto.getNodeCode()));
        if(CollectionUtils.isEmpty(existPoints)){
            Point point=new Point();
            BeanUtils.copyProperties(pointDto,point);
            pointMapperImp.save(point);
        }else {
            throw new ParameterException(String.format("点位已经存在:%s=%s,%s",pointDto.getNodeCode(),pointDto.getTag(),pointDto.getName()));
        }

    }

    /*删除点位及其设置的规则*/
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id){
        Optional.ofNullable(id).orElseThrow(()->new ParameterException("点位id为空"));
        //查找位号相关的规则映射信息
        List<AlarmRuleDto> alarmRuleDtos=alarmRuleService.getByPointId(id);
        if(CollectionUtils.isNotEmpty(alarmRuleDtos)){
            //删除点位设置的规则
            alarmRuleDtos.forEach(a->{
                alarmRuleService.delete(a.getId());
            });
        }
        //删除点位
        pointMapperImp.remove(Wrappers.<Point>lambdaQuery().eq(Point::getId,id));
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(PointDto pointDto){
        Optional.ofNullable(pointDto).map(PointDto::getId).orElseThrow(()->new ParameterException("点位id为空"));
        Point point=new Point();
        BeanUtils.copyProperties(pointDto,point);
        pointMapperImp.updateById(point);
    }

    public List<PointDto> getByDevicId(Long deviceId){
        Optional.ofNullable(deviceId).orElseThrow(()->new ParameterException("设备id为空"));
        List<Point> db_res=pointMapperImp.list(Wrappers.<Point>lambdaQuery().eq(Point::getRefDeviceId,deviceId));
        List<PointDto> res=new ArrayList<>();
        if(CollectionUtils.isNotEmpty(db_res)){
            db_res.forEach(d->{
                PointDto pointDto=new PointDto();
                BeanUtils.copyProperties(d,pointDto);
                res.add(pointDto);
            });
        }

        return res;
    }



    @Transactional(rollbackFor = Exception.class)
    public void deleteTranlatioTest() throws ParameterException{
        pointMapperImp.remove(Wrappers.<Point>lambdaQuery().eq(Point::getName,"123"));
        deviceService.deleteTranlatioTest();
        throw new ParameterException("异常抛出");
    }



    public void export(HttpServletResponse response){
        List<List<String>> head = excelHead("报警点位上传模板");
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        //下拉选项设备
        List<Device> deviceList=deviceMapperImp.list();
        List<String> deviceNameList=deviceList.stream().map(s->s.getDeviceName()).collect(Collectors.toList());

        try {
            String fileName = URLEncoder.encode("报警点位上传模板", "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
            EasyExcel.write(response.getOutputStream())
                    .head(head)
                    .sheet("模板")
                    //开关名称选项
                    .registerWriteHandler(new ExcelUtils.DownSelectWriteHandler(2,10000,3,3,deviceNameList))
                    //规则下拉选项
                    .doWrite(excelData());
        } catch (IOException e) {
            log.error(e.getMessage(),e);
            throw new ParameterException("导出报警点位上传模板.xlsx异常");
        }
    }


    public void imp0rt(MultipartFile file){
        if(!file.getOriginalFilename().endsWith(".xlsx")){
            throw new ParameterException("请上传.xlsx格式文件");
        }
        //数据解析,只解析5000条数据
        try {
            EasyExcel.read(file.getInputStream(), PointExcelDto.class, new ReadExcleLisenter(5000,pointMapperImp,deviceMapperImp)).headRowNumber(2).sheet().doRead();
        } catch (IOException e) {
            log.error(e.getMessage(),e);
            throw new ParameterException("文件处理异常");
        }
    }

    /**
     * 动态生成表头
     * */
    private List<List<String>> excelHead(String title) {
        /*表头数据*/
        List<List<String>> list = new ArrayList<List<String>>();
        List<String> head0 = new ArrayList<String>();
        head0.addAll(Lists.newArrayList(title,"iot位号"));
        List<String> head1 = new ArrayList<String>();
        head1.addAll(Lists.newArrayList(title,"位号名称"));
        List<String> head2 = new ArrayList<String>();
        head2.addAll(Lists.newArrayList(title,"iot节点编码"));
        List<String> head3 = new ArrayList<String>();
        head3.addAll(Lists.newArrayList(title,"设备名称"));
        list.add(head0);
        list.add(head1);
        list.add(head2);
        list.add(head3);

        return list;
    }


    /**
     * 表单数据
     * */
    private List<PointExcelDto> excelData(){
        List<PointExcelDto> res=new ArrayList<>();
        /*查询转化规则*/
        List<Point> pointList=pointMapperImp.list();
        //查询开关
        List<Device> deviceList=deviceMapperImp.list();
        Map<Long,Device> deviceMap=deviceList.stream().collect(Collectors.toMap(Device::getId, s->s,(o, n)->n));
        if(CollectionUtils.isNotEmpty(pointList)){
            pointList.forEach(s->{
                PointExcelDto pointExcelDto=new PointExcelDto();
                pointExcelDto.setName(s.getName());
                pointExcelDto.setNodeCode(s.getNodeCode());
                pointExcelDto.setRefDevice(deviceMap.get(s.getRefDeviceId()).getDeviceName());
                pointExcelDto.setTag(s.getTag());
                res.add(pointExcelDto);
            });

        }
        return res;
    }

    //读excle监听类
    public static class ReadExcleLisenter extends AnalysisEventListener<PointExcelDto> {
        private final int batchCount;
        private PointMapperImp pointMapperImp;
        private DeviceMapperImp deviceMapperImp;
        public ReadExcleLisenter(int batchCount
                , PointMapperImp pointMapperImp
                , DeviceMapperImp deviceMapperImp
        ) {
            this.batchCount= batchCount;
            this.pointMapperImp=pointMapperImp;
            this.deviceMapperImp=deviceMapperImp;
        }

        @Override
        public void invoke(PointExcelDto data, AnalysisContext context) {
            int rowIndex=context.readRowHolder().getRowIndex()+1;//第几行
//            log.info("rowIndex={}",rowIndex);
            Point point=new Point();
            //编码位号
            Valider.valid(false,data.getTag(), new Success<Point>(){
                @Override
                public boolean exceute(String[] value, Point object,String errormessage) {
                    if(StringUtils.isNotBlank(value[0])){
                        object.setTag(value[0].trim()); return true;
                    }
                    throw new ParameterException(String.format("%s 位号为空",errormessage));
                }
            },point,String.format("第%d行位号",rowIndex));
            //点位校验和初始化
            Valider.valid(false,data.getName(), new Success<Point>(){
                @Override
                public boolean exceute(String[] value, Point object,String errormessage) {
                    if(StringUtils.isNotBlank(value[0])){
                        object.setName(value[0].trim()); return true;
                    }
                    throw new ParameterException(String.format("%s 位号名称为空",errormessage));
                }
            },point,String.format("第%d行位号名称",rowIndex));
            //开关校验
            Valider.valid(false,data.getNodeCode(), new Success<Point>(){
                @Override
                public boolean exceute(String[] value, Point object,String errormessage) {
                    if(StringUtils.isNotBlank(value[0])){
                        object.setNodeCode(value[0].trim()); return true;
                    }
                    throw new ParameterException(String.format("%s 节点编码为空",errormessage));
                }
            },point,String.format("第%d节点编码",rowIndex));
            //数据校验
            Valider.valid(false,data.getRefDevice().toString(), new Success<Point>(){
                @Override
                public boolean exceute(String[] value, Point object,String errormessage) {
                    if(StringUtils.isNotBlank(value[0])){
                        //查询设备是否存在
                        Device device=deviceMapperImp.getOne(Wrappers.<Device>lambdaQuery().eq(Device::getDeviceName,value[0]));
                        if(ObjectUtils.isNotEmpty(device)){
                            object.setRefDeviceId(device.getId()); return true;
                        }
                    }
                    throw new ParameterException(String.format("%s 设备名称为空或没有配置",errormessage));
                }
            },point,String.format("第%d行设备名称",rowIndex));
            //检查有无数据，有数据就进行更新，无数据就直接插入
            Point existPoint=pointMapperImp.getOne(Wrappers.<Point>lambdaQuery().eq(Point::getNodeCode,point.getNodeCode()).eq(Point::getTag,point.getTag()));
            if(ObjectUtils.isNotEmpty(existPoint)){
                point.setId(existPoint.getId());
            }
            pointMapperImp.saveOrUpdate(point);
        }


        @Override
        public void doAfterAllAnalysed(AnalysisContext context) {

        }

        }

    }



