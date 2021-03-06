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
    private SwitchRuleMapperImp switchRuleMapperImp;

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private PointService pointService;
    @Autowired
    private SystemConfigService systemConfigService;

    @Transactional(rollbackFor = Exception.class)
    public void add(PointDto pointDto) {
        //??????????????????
        List<Device> devices = deviceMapperImp.list(Wrappers.<Device>lambdaQuery().eq(Device::getId, pointDto.getRefDeviceId()));
        if (CollectionUtils.isEmpty(devices)) {
            throw new ParameterException("???????????????id?????????");
        }
        List<Point> existPoints = pointMapperImp.list(Wrappers.<Point>lambdaQuery().nested(i -> i.eq(Point::getTag, pointDto.getTag()).eq(Point::getNodeCode, pointDto.getNodeCode())).or(i -> i.eq(Point::getName, pointDto.getName())));
        if (CollectionUtils.isEmpty(existPoints)) {
            Point point = new Point();
            BeanUtils.copyProperties(pointDto, point);
            pointMapperImp.save(point);
        } else {
            throw new ParameterException(String.format("??????????????????:%s=%s,%s", pointDto.getNodeCode(), pointDto.getTag(), pointDto.getName()));
        }

    }

    /*?????????????????????????????????*/
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Optional.ofNullable(id).orElseThrow(() -> new ParameterException("??????id??????"));
        //???????????????????????????????????????
        List<AlarmRuleDto> alarmRuleDtos = alarmRuleService.getByPointId(id);
        if (CollectionUtils.isNotEmpty(alarmRuleDtos)) {
            //???????????????????????????
            alarmRuleDtos.forEach(a -> {
                alarmRuleService.delete(a.getId());
            });
            //????????????????????????????????????
            List<Long> ruleIds = alarmRuleDtos.stream().map(AlarmRuleDto::getId).distinct().collect(Collectors.toList());
            alarmRuleSwitchMapMapperImp.remove(Wrappers.<AlarmRuleSwitchMap>lambdaQuery().in(AlarmRuleSwitchMap::getRefAlarmRuleId, ruleIds));

        }
        //??????????????????
        switchRuleMapperImp.remove(Wrappers.<SwitchRule>lambdaQuery().eq(SwitchRule::getPointId, id));

        //????????????
        pointMapperImp.remove(Wrappers.<Point>lambdaQuery().eq(Point::getId, id));
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(PointDto pointDto) {
        Optional.ofNullable(pointDto).map(PointDto::getId).orElseThrow(() -> new ParameterException("??????id??????"));
        //?????????????????????or ??????
        List<Point> existPoints = pointMapperImp.list(Wrappers.<Point>lambdaQuery().nested(i -> i.eq(Point::getTag, pointDto.getTag()).eq(Point::getNodeCode, pointDto.getNodeCode())).or(i -> i.eq(Point::getName, pointDto.getName())));
        boolean isExist = existPoints.stream().anyMatch(p -> !(p.getId().equals(pointDto.getId())));
        if (isExist) {
            throw new ParameterException("???????????????????????????");
        }
        Point point = new Point();
        BeanUtils.copyProperties(pointDto, point);
        pointMapperImp.updateById(point);
    }

    public List<PointDto> getByDevicId(Long deviceId) {
        Optional.ofNullable(deviceId).orElseThrow(() -> new ParameterException("??????id??????"));
        List<Point> db_res = pointMapperImp.list(Wrappers.<Point>lambdaQuery().eq(Point::getRefDeviceId, deviceId));
        List<PointDto> res = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(db_res)) {
            db_res.forEach(d -> {
                PointDto pointDto = new PointDto();
                BeanUtils.copyProperties(d, pointDto);
                res.add(pointDto);
            });
        }

        return res;
    }

    public List<PointDto> list() {
        Map<String,String> stringStringMap=systemConfigService.getProcess().stream().collect(Collectors.toMap(s->s.split("-")[1],s->s.split("-")[0],(o,n)->n));
        List<Point> db_res = pointMapperImp.list();
        List<PointDto> res = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(db_res)) {
            db_res.forEach(d -> {
                PointDto pointDto = new PointDto();
                BeanUtils.copyProperties(d, pointDto);
                Device device = deviceMapperImp.getById(d.getRefDeviceId());
                if (device != null) {
                    pointDto.setDeviceName(stringStringMap.get(device.getProcess()) + "/" + device.getDeviceName());
                }
                res.add(pointDto);
            });
        }

        return res;
    }


    @Transactional(rollbackFor = Exception.class)
    public void deleteTranlatioTest() throws ParameterException {
        pointMapperImp.remove(Wrappers.<Point>lambdaQuery().eq(Point::getName, "123"));
        deviceService.deleteTranlatioTest();
        throw new ParameterException("????????????");
    }


    public void export(HttpServletResponse response) {
        List<List<String>> head = excelHead("????????????????????????");
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        //??????????????????
        List<Device> deviceList = deviceMapperImp.list();
        List<String> deviceNameList = deviceList.stream().map(s -> s.getDeviceName()).collect(Collectors.toList());

        try {
            String fileName = URLEncoder.encode("????????????????????????", "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
            EasyExcel.write(response.getOutputStream())
                    .head(head)
                    .sheet("??????")
                    //??????????????????
                    .registerWriteHandler(new ExcelUtils.DownSelectWriteHandler(2, 10000, 3, 3, deviceNameList))
                    //??????????????????
                    .doWrite(excelData());
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new ParameterException("??????????????????????????????.xlsx??????");
        }
    }


    public void imp0rt(MultipartFile file) {
        if (!file.getOriginalFilename().endsWith(".xlsx")) {
            throw new ParameterException("?????????.xlsx????????????");
        }
        //????????????,?????????5000?????????
        try {
            EasyExcel.read(file.getInputStream(), PointExcelDto.class, new ReadExcleLisenter(5000, pointMapperImp, deviceMapperImp, pointService)).headRowNumber(2).sheet().doRead();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new ParameterException("??????????????????");
        }
    }

    /**
     * ??????????????????
     */
    private List<List<String>> excelHead(String title) {
        /*????????????*/
        List<List<String>> list = new ArrayList<List<String>>();
        List<String> head0 = new ArrayList<String>();
        head0.addAll(Lists.newArrayList(title, "iot??????"));
        List<String> head1 = new ArrayList<String>();
        head1.addAll(Lists.newArrayList(title, "????????????"));
        List<String> head2 = new ArrayList<String>();
        head2.addAll(Lists.newArrayList(title, "iot????????????"));
        List<String> head3 = new ArrayList<String>();
        head3.addAll(Lists.newArrayList(title, "????????????"));
        list.add(head0);
        list.add(head1);
        list.add(head2);
        list.add(head3);

        return list;
    }


    /**
     * ????????????
     */
    private List<PointExcelDto> excelData() {
        List<PointExcelDto> res = new ArrayList<>();
        /*??????????????????*/
        List<Point> pointList = pointMapperImp.list();
        //????????????
        List<Device> deviceList = deviceMapperImp.list();
        Map<Long, Device> deviceMap = deviceList.stream().collect(Collectors.toMap(Device::getId, s -> s, (o, n) -> n));
        if (CollectionUtils.isNotEmpty(pointList)) {
            pointList.forEach(s -> {
                PointExcelDto pointExcelDto = new PointExcelDto();
                pointExcelDto.setName(s.getName());
                pointExcelDto.setNodeCode(s.getNodeCode());
                pointExcelDto.setRefDevice(deviceMap.get(s.getRefDeviceId()).getDeviceName());
                pointExcelDto.setTag(s.getTag());
                res.add(pointExcelDto);
            });

        }
        return res;
    }

    //???excle?????????
    public static class ReadExcleLisenter extends AnalysisEventListener<PointExcelDto> {
        private final int batchCount;
        private PointMapperImp pointMapperImp;

        private DeviceMapperImp deviceMapperImp;
        private PointService pointService;

        public ReadExcleLisenter(int batchCount
                , PointMapperImp pointMapperImp
                , DeviceMapperImp deviceMapperImp
                , PointService pointService
        ) {
            this.batchCount = batchCount;
            this.pointMapperImp = pointMapperImp;
            this.deviceMapperImp = deviceMapperImp;
            this.pointService = pointService;
        }

        @Override
        public void invoke(PointExcelDto data, AnalysisContext context) {
            int rowIndex = context.readRowHolder().getRowIndex() + 1;//?????????
//            log.info("rowIndex={}",rowIndex);
            Point point = new Point();
            //????????????
            Valider.valid(false, data.getTag(), new Success<Point>() {
                @Override
                public boolean exceute(String[] value, Point object, String errormessage) {
                    if (StringUtils.isNotBlank(value[0])) {
                        object.setTag(value[0].trim());
                        return true;
                    }
                    throw new ParameterException(String.format("%s ????????????", errormessage));
                }
            }, point, String.format("???%d?????????", rowIndex));
            //????????????????????????
            Valider.valid(false, data.getName(), new Success<Point>() {
                @Override
                public boolean exceute(String[] value, Point object, String errormessage) {
                    if (StringUtils.isNotBlank(value[0])) {
                        object.setName(value[0].trim());
                        return true;
                    }
                    throw new ParameterException(String.format("%s ??????????????????", errormessage));
                }
            }, point, String.format("???%d???????????????", rowIndex));
            //????????????
            Valider.valid(false, data.getNodeCode(), new Success<Point>() {
                @Override
                public boolean exceute(String[] value, Point object, String errormessage) {
                    if (StringUtils.isNotBlank(value[0])) {
                        object.setNodeCode(value[0].trim());
                        return true;
                    }
                    throw new ParameterException(String.format("%s ??????????????????", errormessage));
                }
            }, point, String.format("???%d????????????", rowIndex));
            //????????????
            Valider.valid(false, data.getRefDevice().toString(), new Success<Point>() {
                @Override
                public boolean exceute(String[] value, Point object, String errormessage) {
                    if (StringUtils.isNotBlank(value[0])) {
                        //????????????????????????
                        Device device = deviceMapperImp.getOne(Wrappers.<Device>lambdaQuery().eq(Device::getDeviceName, value[0]));
                        if (ObjectUtils.isNotEmpty(device)) {
                            object.setRefDeviceId(device.getId());
                            return true;
                        }
                    }
                    throw new ParameterException(String.format("%s ?????????????????????????????????", errormessage));
                }
            }, point, String.format("???%d???????????????", rowIndex));
            //????????????????????????????????????????????????????????????????????????
            List<Point> existPoint = pointMapperImp.list(Wrappers.<Point>lambdaQuery().eq(Point::getNodeCode, point.getNodeCode()).eq(Point::getTag, point.getTag()));
            if (existPoint.size() <= 1) {
                point.setId(existPoint.size() == 1 ? existPoint.get(0).getId() : null);
            } else {
                throw new ParameterException(String.format("%s=%s ?????????????????????", point.getNodeCode(), point.getTag()));
            }
            pointMapperImp.saveOrUpdate(point);
        }


        @Override
        public void doAfterAllAnalysed(AnalysisContext context) {

        }

    }

}



