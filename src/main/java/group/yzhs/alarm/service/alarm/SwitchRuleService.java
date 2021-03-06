package group.yzhs.alarm.service.alarm;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.metadata.CellData;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.property.ExcelContentProperty;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.common.collect.Lists;
import group.yzhs.alarm.constant.DeviceSwitchRuleEnum;
import group.yzhs.alarm.exception.ParameterException;
import group.yzhs.alarm.mapper.impl.PointMapperImp;
import group.yzhs.alarm.mapper.impl.SwitchMapperImp;
import group.yzhs.alarm.mapper.impl.SwitchRuleMapperImp;
import group.yzhs.alarm.model.dto.device.SwitchRuleDto;
import group.yzhs.alarm.model.dto.device.SwitchRuleExcelDto;
import group.yzhs.alarm.model.entity.Point;
import group.yzhs.alarm.model.entity.Switch;
import group.yzhs.alarm.model.entity.SwitchRule;
import group.yzhs.alarm.service.valid.Success;
import group.yzhs.alarm.service.valid.Valider;
import group.yzhs.alarm.utils.ExcelUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/10/18 11:23
 */
@Service
@Slf4j
public class SwitchRuleService {

    private final static String Separator="=";
    @Autowired
    private SwitchRuleMapperImp switchRuleMapperImp;

    @Autowired
    private SwitchMapperImp switchMapperImp;

    @Autowired
    private PointMapperImp pointMapperImp;

    @Transactional(rollbackFor = Exception.class)
    public void add(SwitchRuleDto switchRuleDto){
        SwitchRule switchRule=new SwitchRule();
        BeanUtils.copyProperties(switchRuleDto,switchRule);
        switchRuleMapperImp.save(switchRule);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id){
        Optional.ofNullable(id).orElseThrow(()->new ParameterException("????????????id??????"));
        switchRuleMapperImp.remove(Wrappers.<SwitchRule>lambdaQuery().eq(SwitchRule::getId,id));
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(SwitchRuleDto switchRuleDto){
        Optional.ofNullable(switchRuleDto).map(SwitchRuleDto::getId).orElseThrow(()->new ParameterException("????????????id??????"));
        SwitchRule switchRule=new SwitchRule();
        BeanUtils.copyProperties(switchRuleDto,switchRule);
        switchRuleMapperImp.updateById(switchRule);
    }

    public List<SwitchRuleDto> get(Long swicthId){
        Optional.ofNullable(swicthId).orElseThrow(()->new ParameterException("??????id??????"));
        List<SwitchRule> db_res=switchRuleMapperImp.list(Wrappers.<SwitchRule>lambdaQuery().eq(SwitchRule::getRefSwitchId,swicthId));
        List<SwitchRuleDto> res=new ArrayList<>();
        if(CollectionUtils.isNotEmpty(db_res)){
            db_res.forEach(d->{
                SwitchRuleDto switchRuleDto=new SwitchRuleDto();
                BeanUtils.copyProperties(d,switchRuleDto);
                Point point=pointMapperImp.getById(d.getPointId());
                if(ObjectUtils.isNotEmpty(point)){
                    switchRuleDto.setPointName(point.getName());
                }

                res.add(switchRuleDto);
            });
        }
        return res;
    }


    /**
     *poi
     *  // ??????????????????????????????????????????????????????????????????????????????????????????????????????n????????????????????????
     *         for(int i=0;i<keys.length;i++){
     *             sheet.setColumnWidth((short) i, (short) (35.7 * 150));
     *         }
     *
     * */
    @Deprecated
    public void export(HttpServletResponse response){
        //????????????
        HSSFWorkbook wb = new HSSFWorkbook();// excel????????????

        HSSFSheet sheet = wb.createSheet("????????????");

        HSSFCellStyle cellStyle=wb.createCellStyle();
        cellStyle.setAlignment(HorizontalAlignment.CENTER);//????????????
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);//????????????
//        sheet.setColumnWidth();???????????????
        CellRangeAddress region1 = new CellRangeAddress(0, 1, (short) 0, (short) 4);
        //??????1???????????? ??????2???????????? ??????3???????????? ??????4????????????
        sheet.addMergedRegion(region1);
        HSSFRow headTitle = sheet.createRow(0);
        HSSFCell headTitleCell=headTitle.createCell(0);
        headTitleCell.setCellValue("???????????????????????????");
        headTitleCell.setCellStyle(cellStyle);
        HSSFRow headRow = sheet.createRow(2);
        //??????????????????
        headRow.createCell(0).setCellValue("??????");
        headRow.createCell(1).setCellValue("??????????????????");
        headRow.createCell(2).setCellValue("??????");
        headRow.createCell(3).setCellValue("??????");
        headRow.createCell(4).setCellValue("?????????");

        List<Switch> switches=switchMapperImp.list();
        //??????id?????????????????????
        Map<Long,String> indexSwitch=switches.stream().collect(Collectors.toMap(Switch::getId,Switch::getName,(o, n)->n));
        //??????????????????
        if(false&&CollectionUtils.isNotEmpty(switches)){
            String[] textlist=new String[0];
            List<String>  switchNames=switches.stream().distinct().map(s->s.getName()).collect(Collectors.toList());
            textlist=switchNames.toArray(textlist);//{ "??????1", "??????2", "??????3", "??????4", "??????5" };
            ExcelUtils.setHSSFValidation(sheet, textlist, 5, 10, 1, 1);// ???????????????501?????????????????????????????????.
        }
        //??????????????????
        List<String> switchRuleCodes= Arrays.stream(DeviceSwitchRuleEnum.values()).map(r->r.getCode()+":"+r.getDesc()).collect(Collectors.toList());
        if(false&&CollectionUtils.isNotEmpty(switchRuleCodes)) {
            ExcelUtils.setHSSFValidation(sheet, switchRuleCodes.toArray(new String[0]), 5, 10, 2, 2);// ???????????????501?????????????????????????????????.
        }
        //??????????????????
        List<Point> points=pointMapperImp.list();
        Map<Long,Point> indexPoint=new HashMap<>();
        if(false&&CollectionUtils.isNotEmpty(points)){
            indexPoint=points.stream().collect(Collectors.toMap(Point::getId,p->p,(o,n)->n));

            String[] textlist=new String[0];
            List<String>  pointNames=points.stream().distinct().map(s->s.getNodeCode()+":"+s.getTag()).collect(Collectors.toList());
            textlist=pointNames.toArray(textlist);//{ "??????1", "??????2", "??????3", "??????4", "??????5" };
            ExcelUtils.setHSSFValidation(sheet, textlist, 5, 10, 1, 1);// ???????????????501?????????????????????????????????.

        }


        //??????
        List<SwitchRule> switchRules=switchRuleMapperImp.list();
        if(false&&CollectionUtils.isNotEmpty(switchRules)){
            Map<Long, Point> finalIndexPoint = indexPoint;
            switchRules.stream().forEach(a->{
                HSSFRow dataRow = sheet.createRow(sheet.getLastRowNum() + 1);
                dataRow.createCell(0).setCellValue(a.getId());//??????
                dataRow.createCell(1).setCellValue(indexSwitch.get(a.getRefSwitchId()));//??????????????????
                dataRow.createCell(2).setCellValue(a.getRuleCode().getCode()+":"+a.getRuleCode().getDesc());//??????
                dataRow.createCell(3).setCellValue(finalIndexPoint.get(a.getPointId()).getNodeCode()+":"+ finalIndexPoint.get(a.getPointId()).getTag());//??????
                dataRow.createCell(4).setCellValue(a.getLimitValue().toString());//?????????
            });

        }
        // ????????????
        String filename = "???????????????????????????";
        // ???????????????
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/vnd.ms-excel;charset=UTF-8");
        //??????????????????xlsx??????
        try {
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(filename + ".xls", "UTF-8"));
            //?????????????????????
            ServletOutputStream outputStream = response.getOutputStream();
            //????????????
            wb.write(outputStream);
            outputStream.flush();
            // ??????
            outputStream.close();
            wb.close();
        } catch (IOException e) {
            log.error(e.getMessage(),e);
            throw new ParameterException("????????????");
        }


    }


    /**
     * ??????easyexcel
     * */
    public void export2(HttpServletResponse response){
        List<List<String>> head = excelHead("??????????????????");
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        //????????????????????????
        List<Switch> switchList=switchMapperImp.list();
        List<String> swicthNameList=switchList.stream().map(s->s.getName()).collect(Collectors.toList());
        List<String> switchRuleRuleList= Arrays.stream(DeviceSwitchRuleEnum.values()).map(e->e.getDesc()+"="+e.getCode()).collect(Collectors.toList());

        try {
            String fileName = URLEncoder.encode("??????????????????", "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
            EasyExcel.write(response.getOutputStream())
                    .head(head)
                    .sheet("??????")
                    //??????????????????
                    .registerWriteHandler(new ExcelUtils.DownSelectWriteHandler(2,10000,1,1,swicthNameList))
                    //??????????????????
                    .registerWriteHandler(new ExcelUtils.DownSelectWriteHandler(2,10000,2,2,switchRuleRuleList))
                    .doWrite(excelData());
        } catch (IOException e) {
            log.error(e.getMessage(),e);
            throw new ParameterException("????????????????????????.xlsx??????");
        }

    }


    //**????????????

    public void imp0rt(MultipartFile file){
        if(!file.getOriginalFilename().endsWith(".xlsx")){
            throw new ParameterException("?????????.xlsx????????????");
        }
        //????????????,?????????5000?????????
        try {
            EasyExcel.read(file.getInputStream(), SwitchRuleExcelDto.class, new ReadExcleLisenter(5000,pointMapperImp,switchMapperImp,switchRuleMapperImp)).headRowNumber(2).sheet().doRead();
        } catch (IOException e) {
           log.error(e.getMessage(),e);
           throw new ParameterException("??????????????????");
        }

    }




    /**
     * ??????????????????
     * */
    private List<List<String>> excelHead(String title) {
        /*????????????*/
        List<List<String>> list = new ArrayList<List<String>>();
        List<String> head0 = new ArrayList<String>();
        head0.addAll(Lists.newArrayList(title,"id(????????????)"));
        List<String> head1 = new ArrayList<String>();
        head1.addAll(Lists.newArrayList(title,"????????????"));
        List<String> head2 = new ArrayList<String>();
        head2.addAll(Lists.newArrayList(title,"????????????"));

        List<String> head3 = new ArrayList<String>();
        head3.addAll(Lists.newArrayList(title,"????????????(iot????????????=iot????????????)"));

        List<String>  head4= new ArrayList<String>();
        head4.addAll(Lists.newArrayList(title,"?????????"));

        list.add(head0);
        list.add(head1);
        list.add(head2);
        list.add(head3);
        list.add(head4);
        return list;
    }


    /**
    * ????????????
    * */
    private List<SwitchRuleExcelDto> excelData(){
        List<SwitchRuleExcelDto> res=new ArrayList<>();
        /*??????????????????*/
        List<SwitchRule> switchRuleList=switchRuleMapperImp.list();
        //????????????
        List<Switch> switchList=switchMapperImp.list();
        Map<Long,Switch> switchMap=switchList.stream().collect(Collectors.toMap(Switch::getId,s->s,(o,n)->n));
        Map<String,DeviceSwitchRuleEnum> switchRuleEnumMap=Arrays.stream(DeviceSwitchRuleEnum.values()).collect(Collectors.toMap(DeviceSwitchRuleEnum::getCode,e->e,(o,n)->n));
        if(CollectionUtils.isNotEmpty(switchRuleList)){
            switchRuleList.forEach(s->{
                SwitchRuleExcelDto switchRuleExcelDto=new SwitchRuleExcelDto();
                switchRuleExcelDto.setId(s.getId().toString());
                switchRuleExcelDto.setRefSwitch(switchMap.containsKey(s.getRefSwitchId())?switchMap.get(s.getRefSwitchId()).getName():"");
                switchRuleExcelDto.setRuleCode(switchRuleEnumMap.get(s.getRuleCode().getCode()).getDesc()+"="+switchRuleEnumMap.get(s.getRuleCode().getCode()).getCode());
                switchRuleExcelDto.setLimitValue(s.getLimitValue());
                //??????point
                Point point=pointMapperImp.getById(s.getPointId());
                if(ObjectUtils.isNotEmpty(point)){
                    switchRuleExcelDto.setPoint(point.getNodeCode()+"="+point.getTag());
                }
                res.add(switchRuleExcelDto);
            });

        }
        return res;
    }




    //???excle?????????
    public static class ReadExcleLisenter extends AnalysisEventListener<SwitchRuleExcelDto> {
        private final int batchCount;
        private PointMapperImp pointMapperImp;
        private SwitchMapperImp switchMapperImp;
        private SwitchRuleMapperImp switchRuleMapperImp;
        public ReadExcleLisenter(int batchCount
                , PointMapperImp pointMapperImp
                                 , SwitchMapperImp switchMapperImp
                                 , SwitchRuleMapperImp switchRuleMapperImp
        ) {
            this.batchCount= batchCount;
            this.pointMapperImp=pointMapperImp;
            this.switchMapperImp=switchMapperImp;
            this. switchRuleMapperImp= switchRuleMapperImp;
        }

        @Override
        public void invoke(SwitchRuleExcelDto data, AnalysisContext context) {
            int rowIndex=context.readRowHolder().getRowIndex()+1;//?????????
//            log.info("rowIndex={}",rowIndex);
            SwitchRule switchRule=new SwitchRule();
            //??????????????????????????????
            Valider.valid(true,data.getRuleCode(), new Success<SwitchRule>(){
                @Override
                public boolean exceute(String[] value, SwitchRule object,String errormessage) {
                    object.setRuleCode(DeviceSwitchRuleEnum.deviceSwitchRuleEnumMapping.get(value[1]));
                    return true;
                }
            },switchRule,String.format("???%d???????????????",rowIndex));
            //????????????????????????
            Valider.valid(true,data.getPoint(), new Success<SwitchRule>(){
                private PointMapperImp _pointMapperImp;

                public Success<SwitchRule> setpointMapperImp(PointMapperImp pointMapperImp){
                    this._pointMapperImp=pointMapperImp;
                    return this;
                }

                @Override
                public boolean exceute(String[] value, SwitchRule object,String errormessage) {

                    Point point=_pointMapperImp.getOne(Wrappers.<Point>lambdaQuery().eq(Point::getTag,value[1]).eq(Point::getNodeCode,value[0]));
                    if(ObjectUtils.isNotEmpty(point)){
                        object.setPointId(point.getId());
                        return true;
                    }
                    throw new ParameterException(String.format("?????????????????????????????????:%s=%s",value[0],value[1]));
                }
            }.setpointMapperImp(pointMapperImp),switchRule,String.format("???%d?????????",rowIndex));
            //????????????
            Valider.valid(false,data.getRefSwitch(), new Success<SwitchRule>(){
                private SwitchMapperImp switchMapperImp;

                public Success<SwitchRule> setpointMapperImp(SwitchMapperImp switchMapperImp){
                    this.switchMapperImp=switchMapperImp;
                    return this;
                }

                @Override
                public boolean exceute(String[] value, SwitchRule object,String errormessage) {

                    Switch aSwitch=switchMapperImp.getOne(Wrappers.<Switch>lambdaQuery().eq(Switch::getName,value[0]));
                    if(ObjectUtils.isNotEmpty(aSwitch)){
                        object.setRefSwitchId(aSwitch.getId());
                        return true;
                    }
                    throw new ParameterException(String.format("%s?????????????????????:%s",errormessage,value[0]));
                }
            }.setpointMapperImp(switchMapperImp),switchRule,String.format("???%d?????????",rowIndex));
            //????????????
            Valider.valid(false,data.getLimitValue().toString(), new Success<SwitchRule>(){
                @Override
                public boolean exceute(String[] value, SwitchRule object,String errormessage) {
                    if(StringUtils.isNotBlank(value[0])){
                        object.setLimitValue(BigDecimal.valueOf(Double.valueOf(value[0])));
                        return true;
                    }
                    throw new ParameterException(String.format("%s?????????????????????:%s",errormessage,value[0]));
                }
            },switchRule,String.format("???%d?????????",rowIndex));
            //????????????????????????????????????????????????????????????????????????
            if(StringUtils.isNotBlank(data.getId())){
                switchRule.setId(Long.parseLong(data.getId()));
            }
            switchRuleMapperImp.saveOrUpdate(switchRule);

           /*
            SwitchRule aSwitch=switchRuleMapperImp.getOne(Wrappers.<SwitchRule>lambdaQuery()
                    .eq(SwitchRule::getRefSwitchId,switchRule.getRefSwitchId())
                    .eq(SwitchRule::getPointId,switchRule.getPointId())
                    .eq(SwitchRule::getRuleCode,switchRule.getRuleCode()));
            if(ObjectUtils.isNotEmpty(aSwitch)){
                switchRule.setId(aSwitch.getId());
                switchRuleMapperImp.updateById(switchRule);
            }else {
                switchRuleMapperImp.save(switchRule);
            }*/

        }


        @Override
        public void doAfterAllAnalysed(AnalysisContext context) {

        }

        private void validData(SwitchRuleExcelDto data, SwitchRule switchRule){
            validRuleCode(data.getRuleCode(),switchRule);

            validPoint(data.getPoint(),switchRule);

            //                data.getRefSwitch();
            //                data.getLimitValue();
            //                ;
        }

        private void validRuleCode(String ruleCode,SwitchRule switchRule){
            if(StringUtils.isNotBlank(ruleCode)&&ruleCode.trim().matches("^(.+)"+Separator+"(.+)$")){
                String[] ruleCodeArrays=ruleCode.split(Separator);
                if(ruleCodeArrays.length==2){
                    if(DeviceSwitchRuleEnum.deviceSwitchRuleEnumMapping.containsKey(ruleCodeArrays[1])){
                        switchRule.setRuleCode(DeviceSwitchRuleEnum.deviceSwitchRuleEnumMapping.get(ruleCodeArrays[1]));
                    }
                }
            }
            throw new ParameterException("?????????????????????");
        }

        private void validPoint(String point,SwitchRule switchRule){

        }

    }



    public static class CustomStringStringConverter implements Converter<BigDecimal> {


        @Override
        public Class<?> supportJavaTypeKey() {
            return BigDecimal.class;
        }

        @Override
        public CellDataTypeEnum supportExcelTypeKey() {
            return CellDataTypeEnum.STRING;
        }

        /**
         * ???????????????????????????
         *
         * @param cellData
         *            NotNull
         * @param contentProperty
         *            Nullable
         * @param globalConfiguration
         *            NotNull
         * @return
         */
        @Override
        public BigDecimal convertToJavaData(CellData cellData, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) throws Exception {
            return BigDecimal.valueOf(Double.valueOf(cellData.getStringValue()));
        }

        /**
         * ?????????????????????????????? ?????????
         *
         * @param value
         *            NotNull
         * @param contentProperty
         *            Nullable
         * @param globalConfiguration
         *            NotNull
         * @return
         */
//        @Override
//        public WriteCellData<?> convertToExcelData(String value, ExcelContentProperty contentProperty,
//                                                   GlobalConfiguration globalConfiguration) {
//            return new WriteCellData<>(value);
//        }

        @Override
        public CellData convertToExcelData(BigDecimal value, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) throws Exception {
            return new CellData<>(value.toString()) ;
        }

    }


}
