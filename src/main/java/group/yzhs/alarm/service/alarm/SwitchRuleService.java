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
        Optional.ofNullable(id).orElseThrow(()->new ParameterException("开关规则id为空"));
        switchRuleMapperImp.remove(Wrappers.<SwitchRule>lambdaQuery().eq(SwitchRule::getId,id));
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(SwitchRuleDto switchRuleDto){
        Optional.ofNullable(switchRuleDto).map(SwitchRuleDto::getId).orElseThrow(()->new ParameterException("开关规则id为空"));
        SwitchRule switchRule=new SwitchRule();
        BeanUtils.copyProperties(switchRuleDto,switchRule);
        switchRuleMapperImp.updateById(switchRule);
    }

    public List<SwitchRuleDto> get(Long swicthId){
        Optional.ofNullable(swicthId).orElseThrow(()->new ParameterException("开关id为空"));
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
     *  // 手动设置列宽。第一个参数表示要为第几列设；，第二个参数表示列的宽度，n为列高的像素数。
     *         for(int i=0;i<keys.length;i++){
     *             sheet.setColumnWidth((short) i, (short) (35.7 * 150));
     *         }
     *
     * */
    @Deprecated
    public void export(HttpServletResponse response){
        //创建表头
        HSSFWorkbook wb = new HSSFWorkbook();// excel文件对象

        HSSFSheet sheet = wb.createSheet("开关规则");

        HSSFCellStyle cellStyle=wb.createCellStyle();
        cellStyle.setAlignment(HorizontalAlignment.CENTER);//水平居中
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);//垂直居中
//        sheet.setColumnWidth();设置类宽度
        CellRangeAddress region1 = new CellRangeAddress(0, 1, (short) 0, (short) 4);
        //参数1：起始行 参数2：终止行 参数3：起始列 参数4：终止列
        sheet.addMergedRegion(region1);
        HSSFRow headTitle = sheet.createRow(0);
        HSSFCell headTitleCell=headTitle.createCell(0);
        headTitleCell.setCellValue("点位报警开关规则表");
        headTitleCell.setCellStyle(cellStyle);
        HSSFRow headRow = sheet.createRow(2);
        //设置表头信息
        headRow.createCell(0).setCellValue("序号");
        headRow.createCell(1).setCellValue("报警开关名称");
        headRow.createCell(2).setCellValue("规则");
        headRow.createCell(3).setCellValue("位号");
        headRow.createCell(4).setCellValue("初始值");

        List<Switch> switches=switchMapperImp.list();
        //根据id来索引开关名称
        Map<Long,String> indexSwitch=switches.stream().collect(Collectors.toMap(Switch::getId,Switch::getName,(o, n)->n));
        //设置开关选项
        if(false&&CollectionUtils.isNotEmpty(switches)){
            String[] textlist=new String[0];
            List<String>  switchNames=switches.stream().distinct().map(s->s.getName()).collect(Collectors.toList());
            textlist=switchNames.toArray(textlist);//{ "列表1", "列表2", "列表3", "列表4", "列表5" };
            ExcelUtils.setHSSFValidation(sheet, textlist, 5, 10, 1, 1);// 第一列的前501行都设置为选择列表形式.
        }
        //规则选项设置
        List<String> switchRuleCodes= Arrays.stream(DeviceSwitchRuleEnum.values()).map(r->r.getCode()+":"+r.getDesc()).collect(Collectors.toList());
        if(false&&CollectionUtils.isNotEmpty(switchRuleCodes)) {
            ExcelUtils.setHSSFValidation(sheet, switchRuleCodes.toArray(new String[0]), 5, 10, 2, 2);// 第一列的前501行都设置为选择列表形式.
        }
        //位号选项设置
        List<Point> points=pointMapperImp.list();
        Map<Long,Point> indexPoint=new HashMap<>();
        if(false&&CollectionUtils.isNotEmpty(points)){
            indexPoint=points.stream().collect(Collectors.toMap(Point::getId,p->p,(o,n)->n));

            String[] textlist=new String[0];
            List<String>  pointNames=points.stream().distinct().map(s->s.getNodeCode()+":"+s.getTag()).collect(Collectors.toList());
            textlist=pointNames.toArray(textlist);//{ "列表1", "列表2", "列表3", "列表4", "列表5" };
            ExcelUtils.setHSSFValidation(sheet, textlist, 5, 10, 1, 1);// 第一列的前501行都设置为选择列表形式.

        }


        //设置
        List<SwitchRule> switchRules=switchRuleMapperImp.list();
        if(false&&CollectionUtils.isNotEmpty(switchRules)){
            Map<Long, Point> finalIndexPoint = indexPoint;
            switchRules.stream().forEach(a->{
                HSSFRow dataRow = sheet.createRow(sheet.getLastRowNum() + 1);
                dataRow.createCell(0).setCellValue(a.getId());//序号
                dataRow.createCell(1).setCellValue(indexSwitch.get(a.getRefSwitchId()));//报警开关名称
                dataRow.createCell(2).setCellValue(a.getRuleCode().getCode()+":"+a.getRuleCode().getDesc());//规则
                dataRow.createCell(3).setCellValue(finalIndexPoint.get(a.getPointId()).getNodeCode()+":"+ finalIndexPoint.get(a.getPointId()).getTag());//位号
                dataRow.createCell(4).setCellValue(a.getLimitValue().toString());//限制值
            });

        }
        // 下载导出
        String filename = "点位报警开关规则表";
        // 设置头信息
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/vnd.ms-excel;charset=UTF-8");
        //一定要设置成xlsx格式
        try {
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(filename + ".xls", "UTF-8"));
            //创建一个输出流
            ServletOutputStream outputStream = response.getOutputStream();
            //写入数据
            wb.write(outputStream);
            outputStream.flush();
            // 关闭
            outputStream.close();
            wb.close();
        } catch (IOException e) {
            log.error(e.getMessage(),e);
            throw new ParameterException("导出失败");
        }


    }


    /**
     * 通过easyexcel
     * */
    public void export2(HttpServletResponse response){
        List<List<String>> head = excelHead("报警开关规则");
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        //下拉选项开关名称
        List<Switch> switchList=switchMapperImp.list();
        List<String> swicthNameList=switchList.stream().map(s->s.getName()).collect(Collectors.toList());
        List<String> switchRuleRuleList= Arrays.stream(DeviceSwitchRuleEnum.values()).map(e->e.getDesc()+"="+e.getCode()).collect(Collectors.toList());

        try {
            String fileName = URLEncoder.encode("报警开关规则", "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
            EasyExcel.write(response.getOutputStream())
                    .head(head)
                    .sheet("模板")
                    //开关名称选项
                    .registerWriteHandler(new ExcelUtils.DownSelectWriteHandler(2,10000,1,1,swicthNameList))
                    //规则下拉选项
                    .registerWriteHandler(new ExcelUtils.DownSelectWriteHandler(2,10000,2,2,switchRuleRuleList))
                    .doWrite(excelData());
        } catch (IOException e) {
            log.error(e.getMessage(),e);
            throw new ParameterException("导出报警开关规则.xlsx异常");
        }

    }


    //**导入配置

    public void imp0rt(MultipartFile file){
        if(!file.getOriginalFilename().endsWith(".xlsx")){
            throw new ParameterException("请上传.xlsx格式文件");
        }
        //数据解析,只解析5000条数据
        try {
            EasyExcel.read(file.getInputStream(), SwitchRuleExcelDto.class, new ReadExcleLisenter(5000,pointMapperImp,switchMapperImp,switchRuleMapperImp)).headRowNumber(2).sheet().doRead();
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
        head0.addAll(Lists.newArrayList(title,"id(不要修改)"));
        List<String> head1 = new ArrayList<String>();
        head1.addAll(Lists.newArrayList(title,"开关名称"));
        List<String> head2 = new ArrayList<String>();
        head2.addAll(Lists.newArrayList(title,"开关规则"));

        List<String> head3 = new ArrayList<String>();
        head3.addAll(Lists.newArrayList(title,"点位名称(iot节点编码=iot位号编码)"));

        List<String>  head4= new ArrayList<String>();
        head4.addAll(Lists.newArrayList(title,"限制值"));

        list.add(head0);
        list.add(head1);
        list.add(head2);
        list.add(head3);
        list.add(head4);
        return list;
    }


    /**
    * 表单数据
    * */
    private List<SwitchRuleExcelDto> excelData(){
        List<SwitchRuleExcelDto> res=new ArrayList<>();
        /*查询转化规则*/
        List<SwitchRule> switchRuleList=switchRuleMapperImp.list();
        //查询开关
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
                //查询point
                Point point=pointMapperImp.getById(s.getPointId());
                if(ObjectUtils.isNotEmpty(point)){
                    switchRuleExcelDto.setPoint(point.getNodeCode()+"="+point.getTag());
                }
                res.add(switchRuleExcelDto);
            });

        }
        return res;
    }




    //读excle监听类
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
            int rowIndex=context.readRowHolder().getRowIndex()+1;//第几行
//            log.info("rowIndex={}",rowIndex);
            SwitchRule switchRule=new SwitchRule();
            //编码规则校验和初始化
            Valider.valid(true,data.getRuleCode(), new Success<SwitchRule>(){
                @Override
                public boolean exceute(String[] value, SwitchRule object,String errormessage) {
                    object.setRuleCode(DeviceSwitchRuleEnum.deviceSwitchRuleEnumMapping.get(value[1]));
                    return true;
                }
            },switchRule,String.format("第%d行编码规则",rowIndex));
            //点位校验和初始化
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
                    throw new ParameterException(String.format("点位表中找不到目标点位:%s=%s",value[0],value[1]));
                }
            }.setpointMapperImp(pointMapperImp),switchRule,String.format("第%d行点位",rowIndex));
            //开关校验
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
                    throw new ParameterException(String.format("%s找不到目标开关:%s",errormessage,value[0]));
                }
            }.setpointMapperImp(switchMapperImp),switchRule,String.format("第%d行开关",rowIndex));
            //数据校验
            Valider.valid(false,data.getLimitValue().toString(), new Success<SwitchRule>(){
                @Override
                public boolean exceute(String[] value, SwitchRule object,String errormessage) {
                    if(StringUtils.isNotBlank(value[0])){
                        object.setLimitValue(BigDecimal.valueOf(Double.valueOf(value[0])));
                        return true;
                    }
                    throw new ParameterException(String.format("%s找不到目标开关:%s",errormessage,value[0]));
                }
            },switchRule,String.format("第%d行数据",rowIndex));
            //检查有无数据，有数据就进行更新，无数据就直接插入
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
            throw new ParameterException("规则编码不匹配");
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
         * 这里读的时候会调用
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
         * 这里是写的时候会调用 不用管
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
