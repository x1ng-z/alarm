package group.yzhs.alarm.service.alarm;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.common.collect.Lists;
import group.yzhs.alarm.constant.*;
import group.yzhs.alarm.exception.ParameterException;
import group.yzhs.alarm.mapper.impl.*;
import group.yzhs.alarm.model.dto.alarm.AlarmRuleDto;
import group.yzhs.alarm.model.dto.alarm.AlarmRuleExcelDto;
import group.yzhs.alarm.model.dto.device.PointExcelDto;
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
 * @date 2021/10/16 20:19
 */
@Slf4j
@Service
public class AlarmRuleService {
    private static final String SPLIT_PROCESS = "-";
    @Autowired
    private AlarmRuleMapperImp alarmRuleMapperImp;

    @Autowired
    private AlarmRuleSwitchMapMapperImp alarmRuleSwitchMapMapperImp;

    @Autowired
    private AlarmClassMapperImp alarmClassMapperImp;

    @Autowired
    private PointMapperImp pointMapperImp;

    @Autowired
    private SystemConfigService systemConfigService;


    @Autowired
    private SwitchMapperImp switchMapperImp;

    public static final String INFTY_ALARM_INTERVAL = "inf";

    private final static Map<String, LimiteModelEnum> modelEnumMap;
    private final static Map<String, AlarmModelEnum> alarmModelEnumMap;
    private final static Map<String, TrigerModelEnum> trigerModelEnumMap;
    private final static Map<String, ProductTypeEnum> producttypeEnum;

    static {
        modelEnumMap = Arrays.stream(LimiteModelEnum.values()).collect(Collectors.toMap(LimiteModelEnum::getCode, p -> p, (o, n) -> n));
        alarmModelEnumMap = Arrays.stream(AlarmModelEnum.values()).collect(Collectors.toMap(AlarmModelEnum::getCode, p -> p, (o, n) -> n));
        trigerModelEnumMap = Arrays.stream(TrigerModelEnum.values()).collect(Collectors.toMap(TrigerModelEnum::getCode, p -> p, (o, n) -> n));
        producttypeEnum = Arrays.stream(ProductTypeEnum.values()).collect(Collectors.toMap(ProductTypeEnum::getCode, p -> p, (o, n) -> n));
    }

    @Transactional(rollbackFor = Exception.class)
    public void add(AlarmRuleDto alarmRuleDto) {
        alarmRuleMapperImp.save(checkParam(alarmRuleDto));
    }

    /*删除报警规则，及其设置的开关映射*/
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Optional.ofNullable(id).orElseThrow(() -> new ParameterException("报警规则id不能为空"));
        //删除规则开关映射
        alarmRuleSwitchMapMapperImp.remove(Wrappers.<AlarmRuleSwitchMap>lambdaQuery().eq(AlarmRuleSwitchMap::getRefAlarmRuleId, id));
        //删除规则
        alarmRuleMapperImp.remove(Wrappers.<AlarmRule>lambdaQuery().eq(AlarmRule::getId, id));
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(AlarmRuleDto alarmRuleDto) {
        alarmRuleMapperImp.updateById(checkParam(alarmRuleDto));
    }


    public AlarmRuleDto getById(Long id) {
        Optional.ofNullable(id).orElseThrow(() -> new ParameterException("报警规则id不能为空"));
        AlarmRule alarmRule = alarmRuleMapperImp.getOne(Wrappers.<AlarmRule>lambdaQuery().eq(AlarmRule::getId, id));
        AlarmRuleDto alarmRuleDto = new AlarmRuleDto();
        if (ObjectUtils.isNotEmpty(alarmRule)) {
            BeanUtils.copyProperties(alarmRule, alarmRuleDto);
        }
        return alarmRuleDto;
    }


    @Transactional(rollbackFor = Exception.class)
    public void updateAlarmRuleAndSwitchMapmping(AlarmRule alarmRule, String[] swicthNames, String errorPrefix) {
        if (0 != swicthNames.length) {
            //删除规则映射
            if (ObjectUtils.isNotEmpty(alarmRule.getId())) {
                //移除先前的映射
                alarmRuleSwitchMapMapperImp.remove(Wrappers.<AlarmRuleSwitchMap>lambdaQuery().eq(AlarmRuleSwitchMap::getRefAlarmRuleId, alarmRule.getId()));
                //更新报警配置
                alarmRuleMapperImp.updateById(alarmRule);

            } else {
                //添加新的报警规则
                alarmRuleMapperImp.save(alarmRule);
            }
            for (String switchName : swicthNames) {
                //查询开关
                Switch aSwitch = switchMapperImp.getOne(Wrappers.<Switch>lambdaQuery().eq(Switch::getName, switchName));
                if (ObjectUtils.isNotEmpty(aSwitch)) {
                    //插入映射
                    AlarmRuleSwitchMap alarmRuleSwitchMap = new AlarmRuleSwitchMap();
                    alarmRuleSwitchMap.setRefAlarmRuleId(alarmRule.getId());
                    alarmRuleSwitchMap.setRefSwitchId(aSwitch.getId());
                    alarmRuleSwitchMapMapperImp.save(alarmRuleSwitchMap);
                } else {
                    throw new ParameterException(String.format("%s 开关不存在", errorPrefix));
                }

            }

        }
    }


    public void export(HttpServletResponse response) {
        List<String> headcontext = Arrays.asList("id(不能修改)", "主报警模式", "次报警模式", "报警类别", "位号(节点编码=位号)", "报警组", "限制值", "报警内容(占位符：_time_(时间内容填充),_value(数据填充))", "是否语音报警", "是否微信推送", "报警开关(多个开关用=分割)");
        List<List<String>> head = excelHead("点位报警规则", headcontext);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        //下拉选项主报警模式
        List<String> alarmModleList = AlarmModelEnum.ALARM_MODEL_ENUM_MAP.values().stream().map(a -> a.getName() + "=" + a.getCode()).collect(Collectors.toList());
        //下拉选项次报警模式
        List<String> subalarmModleList = LimiteModelEnum.LIMITE_MODEL_ENUM_MAP.values().stream().map(a -> a.getName() + "=" + a.getCode()).collect(Collectors.toList());
        subalarmModleList.addAll(TrigerModelEnum.TRIGER_MODEL_ENUM_MAP.values().stream().map(a -> a.getName() + "=" + a.getCode()).collect(Collectors.toList()));
        //报警类别
        List<String> selectAlarmClassList = alarmClassMapperImp.list().stream().map(a -> a.getName() + "=" + a.getCode()).collect(Collectors.toList());
        //报警组
        List<String> selectAlarmGroupList = systemConfigService.getProcess().stream().map(s -> s.replace("-", "=")).collect(Collectors.toList());//ProductTypeEnum.PRODUCT_TYPE_ENUM_MAP.values().stream().map(a->a.getDecs()+"="+a.getCode()).collect(Collectors.toList());
        //是否语音报警
        List<String> selectIsAudio = Lists.newArrayList("FALSE", "TRUE");

        //报警开关
        List<String> selectAlarmSwitch = switchMapperImp.list().stream().map(s -> s.getName()).collect(Collectors.toList());
        try {
            String fileName = URLEncoder.encode("点位报警规则", "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
            EasyExcel.write(response.getOutputStream())
                    .head(head)
                    .sheet("模板")
                    //下拉选项主报警模式
                    .registerWriteHandler(new ExcelUtils.DownSelectWriteHandler(2, 10000, 1, 1, alarmModleList))
                    //下拉选项次报警模式
                    .registerWriteHandler(new ExcelUtils.DownSelectWriteHandler(2, 10000, 2, 2, subalarmModleList))
                    //报警类别
                    .registerWriteHandler(new ExcelUtils.DownSelectWriteHandler(2, 10000, 3, 3, selectAlarmClassList))
                    //报警组
                    .registerWriteHandler(new ExcelUtils.DownSelectWriteHandler(2, 10000, 5, 5, selectAlarmGroupList))
                    //是否语音报警
                    .registerWriteHandler(new ExcelUtils.DownSelectWriteHandler(2, 10000, 8, 8, selectIsAudio))
                    //是否微信推送
                    .registerWriteHandler(new ExcelUtils.DownSelectWriteHandler(2, 10000, 9, 9, selectIsAudio))
                    //报警开关
                    .registerWriteHandler(new ExcelUtils.DownSelectWriteHandler(2, 10000, 10, 10, selectAlarmSwitch))
                    .doWrite(excelData());
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new ParameterException("导出点位报警规则.xlsx异常");
        }
    }


    public void imp0rt(MultipartFile file, AlarmRuleService alarmRuleService) {
        if (!file.getOriginalFilename().endsWith(".xlsx")) {
            throw new ParameterException("请上传.xlsx格式文件");
        }
        //数据解析,只解析5000条数据
        try {
            EasyExcel.read(file.getInputStream(),
                    AlarmRuleExcelDto.class,
                    new ReadExcleLisenter(5000, pointMapperImp, alarmClassMapperImp, alarmRuleMapperImp, alarmRuleSwitchMapMapperImp, switchMapperImp, alarmRuleService, systemConfigService))
                    .headRowNumber(2)
                    .sheet()
                    .doRead();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new ParameterException("文件处理异常");
        }
    }


    public List<AlarmRuleDto> getByPointId(Long pointId) {
        Optional.ofNullable(pointId).orElseThrow(() -> new ParameterException("点位id不能为空"));
        List<AlarmRule> alarmRuleList = alarmRuleMapperImp.list(Wrappers.<AlarmRule>lambdaQuery().eq(AlarmRule::getPointId, pointId));
        List<AlarmRuleDto> res = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(alarmRuleList)) {
            alarmRuleList.stream().forEach(a -> {
                AlarmRuleDto alarmRuleDto = new AlarmRuleDto();
                BeanUtils.copyProperties(a, alarmRuleDto);
                res.add(alarmRuleDto);
            });
        }
        return res;
    }


    private AlarmRule checkParam(AlarmRuleDto alarmRuleDto) {

        if (!INFTY_ALARM_INTERVAL.equals(alarmRuleDto.getAlarmInterval())) {
            try {
                Long.parseLong(alarmRuleDto.getAlarmInterval());
            } catch (NumberFormatException e) {
                throw new ParameterException("alarmInterval 只支持设置位整型/inf");
            }
        }

        AlarmRule alarmRule = new AlarmRule();
        //id 校验
        if (ObjectUtils.isNotEmpty(alarmRuleDto.getId())) {
            List<AlarmRule> existAlarmRuleList = alarmRuleMapperImp.list(Wrappers.<AlarmRule>lambdaQuery().eq(AlarmRule::getId, alarmRuleDto.getId()));
            if (existAlarmRuleList.size() != 1) {
                throw new ParameterException("给定的报警规则id无法找到或存在多条条报警规则");
            }
            alarmRule.setId(alarmRuleDto.getId());

        }





        /*判断报警模式是否在规定的编码范围内，并且设置待插入的报警规则的模式*/
        Boolean isValidAlarmMod = Arrays.stream(AlarmModelEnum.values()).anyMatch(m -> {
            if (m.getCode().equals(alarmRuleDto.getAlarmMode())) {
                alarmRule.setAlarmMode(m.getCode());
                return true;
            }
            return false;
        });


        Boolean isValidLiAlarmMod = Arrays.stream(LimiteModelEnum.values()).anyMatch(lm -> {
            if (lm.getCode().equals(alarmRuleDto.getAlarmSubMode())) {
                alarmRule.setAlarmSubMode(lm.getCode());
                return true;
            }
            return false;
        });


        Boolean isValidTmAlarmMod = Arrays.stream(TrigerModelEnum.values()).anyMatch(tm -> {
            if (tm.getCode().equals(alarmRuleDto.getAlarmSubMode())) {
                alarmRule.setAlarmSubMode(tm.getCode());
                return true;
            }
            return false;
        });

        if (isValidAlarmMod && (isValidLiAlarmMod || isValidTmAlarmMod)) {

        } else {
            throw new ParameterException("报警模式编码不匹配");
        }


        /*报警组的校验*/

        ;
        Boolean groupCheck = systemConfigService.getProcess().stream().anyMatch(pt -> {

            if (pt.split(SPLIT_PROCESS)[1].equals(alarmRuleDto.getAlarmGroup())) {
                alarmRule.setAlarmGroup(pt.split(SPLIT_PROCESS)[1]);
                return true;
            }
            return false;
        });

        if (!groupCheck) {
            throw new ParameterException("报警组编码不匹配");
        }
        alarmRule.setIsWxPush(alarmRuleDto.getIsWxPush());
        alarmRule.setIsAudio(alarmRuleDto.getIsAudio());
        alarmRule.setLimiteValue(alarmRuleDto.getLimiteValue());
        alarmRule.setPointId(alarmRuleDto.getPointId());
        alarmRule.setAlarmClassId(alarmRuleDto.getAlarmClassId());
        alarmRule.setAlarmTemple(alarmRuleDto.getAlarmTemple());
        alarmRule.setAlarmInterval(alarmRuleDto.getAlarmInterval());
        return alarmRule;
    }


    /**
     * 动态生成表头
     */
    private List<List<String>> excelHead(String title, List<String> headcontext) {
        /*表头数据*/
        List<List<String>> list = new ArrayList<List<String>>();

        headcontext.forEach(h -> {
            List<String> head0 = new ArrayList<String>();
            head0.addAll(Lists.newArrayList(title, h));
            list.add(head0);
        });
        return list;
    }


    /**
     * 表单数据
     */
    private List<AlarmRuleExcelDto> excelData() {
        List<AlarmRuleExcelDto> res = new ArrayList<>();
        /*查询转化规则*/
        List<AlarmRule> pointList = alarmRuleMapperImp.list();
        //生料-sl,烧成-sc,制成-zc,key=sl,value=生料=sl
        Map<String, String> processCodeAndName = systemConfigService.getProcess().stream().collect(Collectors.toMap(s -> s.split(SPLIT_PROCESS)[1], s -> s.replace(SPLIT_PROCESS, "="), (o, n) -> n));
        if (CollectionUtils.isNotEmpty(pointList)) {
            pointList.forEach(s -> {
                AlarmRuleExcelDto alarmRuleExcelDto = new AlarmRuleExcelDto();

                alarmRuleExcelDto.setId(s.getId().toString());

                alarmRuleExcelDto.setAlarmMode(AlarmModelEnum.ALARM_MODEL_ENUM_MAP.get(s.getAlarmMode()).getName() + "=" + AlarmModelEnum.ALARM_MODEL_ENUM_MAP.get(s.getAlarmMode()).getCode());

                String subAlarmMode = Optional.ofNullable(LimiteModelEnum.LIMITE_MODEL_ENUM_MAP.get(s.getAlarmSubMode()))
                        .map(p -> p.getName() + "=" + p.getCode())
                        .orElseGet(() -> TrigerModelEnum.TRIGER_MODEL_ENUM_MAP.get(s.getAlarmSubMode()).getName() + "=" + TrigerModelEnum.TRIGER_MODEL_ENUM_MAP.get(s.getAlarmSubMode()).getCode());
                alarmRuleExcelDto.setAlarmSubMode(subAlarmMode);

                alarmRuleExcelDto.setAlarmTemple(s.getAlarmTemple());

                AlarmClass alarmClass = alarmClassMapperImp.getById(s.getAlarmClassId());
                alarmRuleExcelDto.setAlarmClass(alarmClass.getName() + "=" + alarmClass.getCode());

                alarmRuleExcelDto.setIsAudio(s.getIsAudio());
                alarmRuleExcelDto.setIsWxPush(s.getIsWxPush());

                alarmRuleExcelDto.setLimiteValue(s.getLimiteValue());


//                ProductTypeEnum alarmGroup=ProductTypeEnum.PRODUCT_TYPE_ENUM_MAP.get(s.getAlarmGroup().getCode());

                alarmRuleExcelDto.setAlarmGroup(processCodeAndName.get(s.getAlarmGroup()));

                Point point = pointMapperImp.getById(s.getPointId());
                if (ObjectUtils.isEmpty(point)) {
                    throw new ParameterException(String.format("未查询到规则id=%d对应的点位信息", s.getId()));
                }
                alarmRuleExcelDto.setPoint(point.getNodeCode() + "=" + point.getTag());
                StringJoiner alarmSwitchNameList = new StringJoiner("=");
                List<AlarmRuleSwitchMap> alarmRuleSwitchMapList = alarmRuleSwitchMapMapperImp.list(Wrappers.<AlarmRuleSwitchMap>lambdaQuery().eq(AlarmRuleSwitchMap::getRefAlarmRuleId, s.getId()));
                List<Switch> switchList = new ArrayList<>();
                if (CollectionUtils.isNotEmpty(alarmRuleSwitchMapList)) {
                    switchList = switchMapperImp.list(Wrappers.<Switch>lambdaQuery().in(Switch::getId, alarmRuleSwitchMapList.stream().map(AlarmRuleSwitchMap::getRefSwitchId).distinct().collect(Collectors.toList())));
                }
                switchList.forEach(a -> {
                    alarmSwitchNameList.add(a.getName());
                });
                alarmRuleExcelDto.setAlarmSwitch(alarmSwitchNameList.toString());
                res.add(alarmRuleExcelDto);
            });

        }
        return res;
    }

    //读excle监听类
    public static class ReadExcleLisenter extends AnalysisEventListener<AlarmRuleExcelDto> {
        private final int batchCount;
        private PointMapperImp pointMapperImp;
        private AlarmClassMapperImp alarmClassMapperImp;
        private AlarmRuleMapperImp alarmRuleMapperImp;
        private AlarmRuleSwitchMapMapperImp alarmRuleSwitchMapMapperImp;
        private SwitchMapperImp switchMapperImp;
        private AlarmRuleService alarmRuleService;
        private Map<String, String> processCodeAndName;

        public ReadExcleLisenter(int batchCount
                , PointMapperImp pointMapperImp
                , AlarmClassMapperImp alarmClassMapperImp
                , AlarmRuleMapperImp alarmRuleMapperImp
                , AlarmRuleSwitchMapMapperImp alarmRuleSwitchMapMapperImp
                , SwitchMapperImp switchMapperImp
                , AlarmRuleService alarmRuleService,
                                 SystemConfigService systemConfigService
        ) {
            this.batchCount = batchCount;
            this.pointMapperImp = pointMapperImp;
            this.alarmClassMapperImp = alarmClassMapperImp;
            this.alarmRuleMapperImp = alarmRuleMapperImp;
            this.alarmRuleSwitchMapMapperImp = alarmRuleSwitchMapMapperImp;
            this.switchMapperImp = switchMapperImp;
            this.alarmRuleService = alarmRuleService;
            this.processCodeAndName = systemConfigService.getProcess().stream().collect(Collectors.toMap(s -> s.split(SPLIT_PROCESS)[1], s -> s.split(SPLIT_PROCESS)[1], (o, n) -> n));

        }

        @Override
        public void invoke(AlarmRuleExcelDto data, AnalysisContext context) {
            int rowIndex = context.readRowHolder().getRowIndex() + 1;//第几行
//            log.info("rowIndex={}",rowIndex);
            AlarmRule alarmRule = new AlarmRule();
            //id
            if (ObjectUtils.isNotEmpty(data.getId())) {
                Valider.valid(false, data.getId().toString(), new Success<AlarmRule>() {
                    @Override
                    public boolean exceute(String[] value, AlarmRule object, String errormessage) {
                        if (StringUtils.isNotBlank(value[0])) {
                            AlarmRule existAlarmRule = alarmRuleMapperImp.getById(Long.parseLong(value[0]));
                            if (ObjectUtils.isNotEmpty(existAlarmRule)) {
                                object.setId(Long.parseLong(value[0].trim()));
                                return true;
                            } else {
                                throw new ParameterException(String.format("%s 规则不存在", errormessage));
                            }
                        }
                        throw new ParameterException(String.format("%s 为空", errormessage));
                    }
                }, alarmRule, String.format("第%d行id", rowIndex));

            }
            //报警模式
            Valider.valid(true, data.getAlarmMode(), new Success<AlarmRule>() {
                @Override
                public boolean exceute(String[] value, AlarmRule object, String errormessage) {
                    if (StringUtils.isNotBlank(value[1])) {
                        AlarmModelEnum alarmModelEnum = AlarmModelEnum.ALARM_MODEL_ENUM_MAP.get(value[1]);
                        if (ObjectUtils.isNotEmpty(alarmModelEnum)) {
                            object.setAlarmMode(alarmModelEnum.getCode());
                            return true;
                        }

                    }
                    throw new ParameterException(String.format("%s 为空", errormessage));
                }
            }, alarmRule, String.format("第%d报警模式", rowIndex));
            //次报警模式
            Valider.valid(true, data.getAlarmSubMode(), new Success<AlarmRule>() {
                @Override
                public boolean exceute(String[] value, AlarmRule object, String errormessage) {
                    if (StringUtils.isNotBlank(value[1])) {
                        String subAlarmModel = Optional.ofNullable(LimiteModelEnum.LIMITE_MODEL_ENUM_MAP.get(value[1])).map(p -> p.getCode()).orElseGet(() -> TrigerModelEnum.TRIGER_MODEL_ENUM_MAP.get(value[1]).getCode());
                        object.setAlarmSubMode(subAlarmModel);
                        return true;
                    }
                    throw new ParameterException(String.format("%s 为空", errormessage));
                }
            }, alarmRule, String.format("第%d次报警模式", rowIndex));
            //报警类别
            Valider.valid(true, data.getAlarmClass(), new Success<AlarmRule>() {
                @Override
                public boolean exceute(String[] value, AlarmRule object, String errormessage) {
                    if (StringUtils.isNotBlank(value[1])) {
                        //查询设备是否存在
                        AlarmClass alarmClass = alarmClassMapperImp.getOne(Wrappers.<AlarmClass>lambdaQuery().eq(AlarmClass::getCode, value[1]));
                        if (ObjectUtils.isNotEmpty(alarmClass)) {
                            object.setAlarmClassId(alarmClass.getId());
                            return true;
                        }
                    }
                    throw new ParameterException(String.format("%s 为空或没有配置", errormessage));
                }
            }, alarmRule, String.format("第%d行报警类别", rowIndex));
            //位号
            Valider.valid(true, data.getPoint(), new Success<AlarmRule>() {
                @Override
                public boolean exceute(String[] value, AlarmRule object, String errormessage) {
                    if (StringUtils.isNotBlank(value[1])) {
                        //查询设备是否存在
                        Point point = pointMapperImp.getOne(Wrappers.<Point>lambdaQuery().eq(Point::getNodeCode, value[0]).eq(Point::getTag, value[1]));
                        if (ObjectUtils.isNotEmpty(point)) {
                            object.setPointId(point.getId());
                            return true;
                        }
                    }
                    throw new ParameterException(String.format("%s 为空或没有配置", errormessage));
                }
            }, alarmRule, String.format("第%d行位号", rowIndex));
            //报警组
            Valider.valid(true, data.getAlarmGroup(), new Success<AlarmRule>() {
                @Override
                public boolean exceute(String[] value, AlarmRule object, String errormessage) {
                    if (StringUtils.isNotBlank(value[1])) {
                        //查询报警组是否存在
                        String productTypeEnum = Optional.ofNullable(processCodeAndName.get(value[1])).orElseThrow(() -> new ParameterException(String.format("%s 设置错误", errormessage)));
                        if (ObjectUtils.isNotEmpty(productTypeEnum)) {
                            object.setAlarmGroup(productTypeEnum);
                            return true;
                        }
                    }
                    throw new ParameterException(String.format("%s 为空或没有配置", errormessage));
                }
            }, alarmRule, String.format("第%d行报警组", rowIndex));
            //限制值
            Valider.valid(false, data.getLimiteValue().toString(), new Success<AlarmRule>() {
                @Override
                public boolean exceute(String[] value, AlarmRule object, String errormessage) {
                    if (StringUtils.isNotBlank(value[0])) {
                        object.setLimiteValue(BigDecimal.valueOf(Double.valueOf(value[0])));
                        return true;
                    }
                    throw new ParameterException(String.format("%s 为空或没有配置", errormessage));
                }
            }, alarmRule, String.format("第%d行限制值", rowIndex));
            //报警内容
            Valider.valid(false, data.getAlarmTemple(), new Success<AlarmRule>() {
                @Override
                public boolean exceute(String[] value, AlarmRule object, String errormessage) {
                    if (StringUtils.isNotBlank(value[0])) {
                        //查询报警组是否存在
                        object.setAlarmTemple(value[0]);
                        return true;

                    }
                    throw new ParameterException(String.format("%s 为空或没有配置", errormessage));
                }
            }, alarmRule, String.format("第%d行报警内容", rowIndex));
            //是否语音
            Valider.valid(false, data.getIsAudio().toString(), new Success<AlarmRule>() {
                @Override
                public boolean exceute(String[] value, AlarmRule object, String errormessage) {
                    if (StringUtils.isNotBlank(value[0])) {
                        //查询报警组是否存在
                        object.setIsAudio(Boolean.valueOf(value[0]));
                        return true;

                    }
                    throw new ParameterException(String.format("%s 为空或没有配置", errormessage));
                }
            }, alarmRule, String.format("第%d行是否语音", rowIndex));
            //是否微信推送
            Valider.valid(false, data.getIsWxPush().toString(), new Success<AlarmRule>() {
                @Override
                public boolean exceute(String[] value, AlarmRule object, String errormessage) {
                    if (StringUtils.isNotBlank(value[0])) {
                        //查询报警组是否存在
                        object.setIsWxPush(Boolean.valueOf(value[0]));
                        return true;

                    }
                    throw new ParameterException(String.format("%s 为空或没有配置", errormessage));
                }
            }, alarmRule, String.format("第%d行是否微信推送", rowIndex));
            //报警开关
            Valider.valid(false, data.getAlarmSwitch(), new Success<AlarmRule>() {
                @Override
                public boolean exceute(String[] value, AlarmRule object, String errormessage) {
                    String[] switchNames;
                    if (StringUtils.isNotBlank(value[0])) {
                        switchNames = value[0].split("=");
                    } else {
                        return true;
                    }
                    //最后的数据更新
                    alarmRuleService.updateAlarmRuleAndSwitchMapmping(object, switchNames, errormessage);
                    return true;
                }
            }, alarmRule, String.format("第%d行报警开关", rowIndex));

            alarmRuleMapperImp.saveOrUpdate(alarmRule);
        }


        @Override
        public void doAfterAllAnalysed(AnalysisContext context) {

        }

    }


    @Deprecated
    private AlarmRuleDto copyAlarmRule(AlarmRule alarmRule) {
        return AlarmRuleDto.builder()
                .alarmMode(alarmRule.getAlarmMode())
                .alarmClassId(alarmRule.getAlarmClassId())
                .alarmSubMode(alarmRule.getAlarmSubMode())
                .alarmTemple(alarmRule.getAlarmTemple())
                .isAudio(alarmRule.getIsAudio())
                .id(alarmRule.getId())
                .pointId(alarmRule.getPointId())
                .limiteValue(alarmRule.getLimiteValue())
                .alarmGroup(alarmRule.getAlarmGroup()/*.getCode()*/)
                .build();
    }


}
