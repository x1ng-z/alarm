package group.yzhs.alarm.utils;

import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.CellData;
import com.alibaba.excel.metadata.Head;
import com.alibaba.excel.util.BooleanUtils;
import com.alibaba.excel.util.StyleUtil;
import com.alibaba.excel.write.handler.CellWriteHandler;
import com.alibaba.excel.write.handler.SheetWriteHandler;
import com.alibaba.excel.write.merge.AbstractMergeStrategy;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteWorkbookHolder;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.metadata.style.WriteFont;
import com.alibaba.excel.write.style.AbstractCellStyleStrategy;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import com.alibaba.excel.write.style.column.AbstractColumnWidthStyleStrategy;
import com.alibaba.excel.write.style.row.AbstractRowHeightStyleStrategy;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;

import java.util.*;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/10/19 14:56
 */
@Slf4j
@UtilityClass
public class ExcelUtils {
    /**写excle数据设置**/
    //下来选项
    public static class DownSelectWriteHandler implements SheetWriteHandler {
        /*设置下拉选项区域*/

        private int firstRow, lastRow,  firstCol,  lastCol;
        private List<String> selectContext;
        //行列索引从0开始
        public DownSelectWriteHandler(int firstRow, int lastRow, int firstCol, int lastCol, List<String> selectContext) {
            this.firstRow = firstRow;
            this.lastRow = lastRow;
            this.firstCol = firstCol;
            this.lastCol = lastCol;
            this.selectContext = selectContext;
        }

        @Override
        public void beforeSheetCreate(WriteWorkbookHolder writeWorkbookHolder, WriteSheetHolder writeSheetHolder) {
            //do nothing
        }

        @Override
        public void afterSheetCreate(WriteWorkbookHolder writeWorkbookHolder, WriteSheetHolder writeSheetHolder/*SheetWriteHandlerContext context*/) {
            if(CollectionUtils.isNotEmpty(selectContext)){
                CellRangeAddressList cellRangeAddressList = new CellRangeAddressList( firstRow,  lastRow,  firstCol,  lastCol);
                DataValidationHelper helper = writeSheetHolder.getSheet().getDataValidationHelper();
                DataValidationConstraint constraint = helper.createExplicitListConstraint(selectContext.toArray(new String[0]));
                DataValidation dataValidation = helper.createValidation(constraint, cellRangeAddressList);
                writeSheetHolder.getSheet().addValidationData(dataValidation);
            }
        }
    }

    //单元格高度配置，现在是没这个需求
    public static class CustomCellWriteHeightConfig extends AbstractRowHeightStyleStrategy {
        /**
         * 默认高度
         */
        private static final Integer DEFAULT_HEIGHT = 300;

        @Override
        protected void setHeadColumnHeight(Row row, int relativeRowIndex) {
        }

        @Override
        protected void setContentColumnHeight(Row row, int relativeRowIndex) {
            Iterator<Cell> cellIterator = row.cellIterator();
            if (!cellIterator.hasNext()) {
                return;
            }

            // 默认为 1行高度
            Integer maxHeight = 1;
            while (cellIterator.hasNext()) {
                Cell cell = cellIterator.next();
                switch (cell.getCellTypeEnum()) {
                    case STRING:
                        if (cell.getStringCellValue().indexOf("\n") != -1) {
                            int length = cell.getStringCellValue().split("\n").length;
                            maxHeight = Math.max(maxHeight, length);
                        }
                        break;
                    default:
                        break;
                }
            }

            row.setHeight((short) (maxHeight * DEFAULT_HEIGHT));
        }
    }

    //列宽配置，根据需求改造！
    public class CustomCellWriteWeightConfig extends AbstractColumnWidthStyleStrategy {
        private Map<Integer, Map<Integer, Integer>> CACHE = new HashMap<>();

        @Override
        protected void setColumnWidth(WriteSheetHolder writeSheetHolder, List<CellData> cellDataList, Cell cell, Head head, Integer integer, Boolean isHead) {
            boolean needSetWidth = isHead || !org.springframework.util.CollectionUtils.isEmpty(cellDataList);
            if (needSetWidth) {
                Map<Integer, Integer> maxColumnWidthMap = CACHE.get(writeSheetHolder.getSheetNo());
                if (maxColumnWidthMap == null) {
                    maxColumnWidthMap = new HashMap<>();
                    CACHE.put(writeSheetHolder.getSheetNo(), maxColumnWidthMap);
                }

                Integer columnWidth = this.dataLength(cellDataList, cell, isHead);
                if (columnWidth >= 0) {
                    if (columnWidth > 254) {
                        columnWidth = 254;
                    }

                    Integer maxColumnWidth = maxColumnWidthMap.get(cell.getColumnIndex());
                    if (maxColumnWidth == null || columnWidth > maxColumnWidth) {
                        maxColumnWidthMap.put(cell.getColumnIndex(), columnWidth);
                        Sheet sheet = writeSheetHolder.getSheet();
                        sheet.setColumnWidth(cell.getColumnIndex(), columnWidth * 256);
                    }

                }
            }
        }

        /**
         * 计算长度
         * @param cellDataList
         * @param cell
         * @param isHead
         * @return
         */
        private Integer dataLength(List<CellData> cellDataList, Cell cell, Boolean isHead) {
            if (isHead) {
                return cell.getStringCellValue().getBytes().length;
            } else {
                CellData cellData = cellDataList.get(0);
                CellDataTypeEnum type = cellData.getType();
                if (type == null) {
                    return -1;
                } else {
                    switch (type) {
                        case STRING:
                            // 换行符（数据需要提前解析好）
                            int index = cellData.getStringValue().indexOf("\n");
                            return index != -1 ?
                                    cellData.getStringValue().substring(0, index).getBytes().length + 1 : cellData.getStringValue().getBytes().length + 1;
                        case BOOLEAN:
                            return cellData.getBooleanValue().toString().getBytes().length;
                        case NUMBER:
                            return cellData.getNumberValue().toString().getBytes().length;
                        default:
                            return -1;
                    }
                }
            }
        }
    }

    //合并单元格配置，根据需求改造！
    public class MyMergeStrategy extends AbstractMergeStrategy {
        //合并坐标
        /**
         * 合并的开始行
         */
        private int firstRow;
        /**
         * 合并的结束行
         */
        private int lastRow;
        /**
         * 合并的开始列
         */
        private int firstColumn;
        /**
         * 合并的结束列
         */
        private int lastColumn;

        public MyMergeStrategy(int firstRow, int lastRow, int firstColumn, int lastColumn) {
            this.firstRow = firstRow;
            this.lastRow = lastRow;
            this.firstColumn = firstColumn;
            this.lastColumn = lastColumn;
        }

        @Override
        protected void merge(Sheet sheet, Cell cell, Head head, Integer integer) {
            /*这个 if判断很重要，否则每个单元格都会合并一次*/
            if (cell.getRowIndex() == this.firstRow && cell.getColumnIndex() == this.firstColumn) {
                CellRangeAddress cellAddresses = new CellRangeAddress(this.firstRow,this.lastRow,this.firstColumn,this.lastColumn);
                sheet.addMergedRegionUnsafe(cellAddresses);
            }
        }
    }

    //单元格样式配置1(函数方式)
    public static HorizontalCellStyleStrategy createHeaderAndContextStyle(){
        // 方法1 使用已有的策略 推荐
        // HorizontalCellStyleStrategy 每一行的样式都一样 或者隔行一样
        // AbstractVerticalCellStyleStrategy 每一列的样式都一样 需要自己回调每一页
        String fileName = "handlerStyleWrite" + System.currentTimeMillis() + ".xlsx";
        // 头的策略
        WriteCellStyle headWriteCellStyle = new WriteCellStyle();
        // 背景设置为红色
        headWriteCellStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
        WriteFont headWriteFont = new WriteFont();
        headWriteFont.setFontHeightInPoints((short)20);
        headWriteCellStyle.setWriteFont(headWriteFont);
        // 内容的策略
        WriteCellStyle contentWriteCellStyle = new WriteCellStyle();
        // 这里需要指定 FillPatternType 为FillPatternType.SOLID_FOREGROUND 不然无法显示背景颜色.头默认了 FillPatternType所以可以不指定
        contentWriteCellStyle.setFillPatternType(FillPatternType.SOLID_FOREGROUND);
        // 背景绿色
        contentWriteCellStyle.setFillForegroundColor(IndexedColors.GREEN.getIndex());
        WriteFont contentWriteFont = new WriteFont();
        // 字体大小
        contentWriteFont.setFontHeightInPoints((short)20);
        contentWriteCellStyle.setWriteFont(contentWriteFont);
        // 这个策略是 头是头的样式 内容是内容的样式 其他的策略可以自己实现
        return new HorizontalCellStyleStrategy(headWriteCellStyle, contentWriteCellStyle);
    }
    //单元格样式配置2(注册处理器的方式)
    public static class CellStyleWriteHandler extends AbstractCellStyleStrategy {
        @Override
        protected void initCellStyle(Workbook workbook) {

        }

        @Override
        protected void setHeadCellStyle(Cell cell, Head head, Integer relativeRowIndex) {

        }

        @Override
        protected void setContentCellStyle(Cell cell, Head head, Integer relativeRowIndex) {
            // 第一个单元格
            // 设置列宽
            Integer colorIndex=Integer.valueOf(IndexedColors.RED.index);
            List<Integer>columnIndexs=new ArrayList<>();
            Sheet sheet = cell.getSheet();
            sheet.setColumnWidth(cell.getColumnIndex(), 14 * 256);
//            writeSheetHolder.getSheet().getRow(0).setHeight((short)(1.8*256));
            Workbook workbook = cell.getSheet().getWorkbook();
            Drawing<?> drawing = sheet.createDrawingPatriarch();

            // 设置标题字体样式
            WriteCellStyle headWriteCellStyle = new WriteCellStyle();
            WriteFont headWriteFont = new WriteFont();
            headWriteFont.setFontName("宋体");
            headWriteFont.setFontHeightInPoints((short)14);
            headWriteFont.setBold(true);
            if (CollectionUtils.isNotEmpty(columnIndexs) &&
                    colorIndex != null &&
                    columnIndexs.contains(cell.getColumnIndex())) {
                // 设置字体颜色
                headWriteFont.setColor(colorIndex.shortValue());
            }
            headWriteCellStyle.setWriteFont(headWriteFont);
            headWriteCellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            CellStyle cellStyle = StyleUtil.buildHeadCellStyle(workbook, headWriteCellStyle);
            cell.setCellStyle(cellStyle);
        }


    }

    /**写excle数据设置**/


    /**
     * 设置某些列的值只能输入预制的数据,显示下拉框.
     *
     * @param sheet 要设置的sheet.
     * @param textlist 下拉框显示的内容
     * @param firstRow 开始行
     * @param endRow 结束行
     * @param firstCol 开始列
     * @param endCol 结束列
     * @return 设置好的sheet.
     */
    public  HSSFSheet setHSSFValidation(HSSFSheet sheet,
                                              String[] textlist, int firstRow, int endRow, int firstCol,
                                              int endCol) {
        // 加载下拉列表内容
        DVConstraint constraint = DVConstraint
                .createExplicitListConstraint(textlist);
        // 设置数据有效性加载在哪个单元格上,四个参数分别是：起始行、终止行、起始列、终止列
        CellRangeAddressList regions = new CellRangeAddressList(firstRow,
                endRow, firstCol, endCol);
        // 数据有效性对象
        HSSFDataValidation data_validation_list = new HSSFDataValidation(
                regions, constraint);
        sheet.addValidationData(data_validation_list);
        return sheet;
    }



    /**
     * 设置单元格上提示
     *
     * @param sheet
     *            要设置的sheet.
     * @param promptTitle
     *            标题
     * @param promptContent
     *            内容
     * @param firstRow
     *            开始行
     * @param endRow
     *            结束行
     * @param firstCol
     *            开始列
     * @param endCol
     *            结束列
     * @return 设置好的sheet.
     */
    public  HSSFSheet setHSSFPrompt(HSSFSheet sheet, String promptTitle,
                                          String promptContent, int firstRow, int endRow, int firstCol,
                                          int endCol) {
        // 构造constraint对象
        DVConstraint constraint = DVConstraint
                .createCustomFormulaConstraint("BB1");
        // 四个参数分别是：起始行、终止行、起始列、终止列
        CellRangeAddressList regions = new CellRangeAddressList(firstRow,
                endRow, firstCol, endCol);
        // 数据有效性对象
        HSSFDataValidation data_validation_view = new HSSFDataValidation(
                regions, constraint);
        data_validation_view.createPromptBox(promptTitle, promptContent);
        sheet.addValidationData(data_validation_view);
        return sheet;
    }



//    /*
//     * 列头单元格样式
//     */
//    public HSSFCellStyle getColumnTopStyle(HSSFWorkbook workbook) {
//
//        // 设置字体
//        HSSFFont font = workbook.createFont();
//        //设置字体大小
//        font.setFontHeightInPoints((short)11);
//        //字体加粗
////        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
//        //设置字体名字
//        font.setFontName("Courier New");
//        //设置样式;
//        HSSFCellStyle style = workbook.createCellStyle();
//        //设置底边框;
//        style.setBorderBottom(BorderStyle.THIN);
//        //设置底边框颜色;
//        style.setBottomBorderColor(HSSFColor.BLACK.index);
//        //设置左边框;
//        style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
//        //设置左边框颜色;
//        style.setLeftBorderColor(HSSFColor.BLACK.index);
//        //设置右边框;
//        style.setBorderRight(HSSFCellStyle.BORDER_THIN);
//        //设置右边框颜色;
//        style.setRightBorderColor(HSSFColor.BLACK.index);
//        //设置顶边框;
//        style.setBorderTop(HSSFCellStyle.BORDER_THIN);
//        //设置顶边框颜色;
//        style.setTopBorderColor(HSSFColor.BLACK.index);
//        //在样式用应用设置的字体;
//        style.setFont(font);
//        //设置自动换行;
//        style.setWrapText(false);
//        //设置水平对齐的样式为居中对齐;
//        style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
//        //设置垂直对齐的样式为居中对齐;
//        style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
//
//        return style;
//
//    }
//
//    /*
//     * 列数据信息单元格样式
//     */
//    public static HSSFCellStyle getStyle(HSSFWorkbook workbook) {
//        // 设置字体
//        HSSFFont font = workbook.createFont();
//        //设置字体大小
//        //font.setFontHeightInPoints((short)10);
//        //字体加粗
//        //font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
//        //设置字体名字
//        font.setFontName("Courier New");
//        //设置样式;
//        HSSFCellStyle style = workbook.createCellStyle();
//        //设置底边框;
//        style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
//        //设置底边框颜色;
//        style.setBottomBorderColor(HSSFColor.BLACK.index);
//        //设置左边框;
//        style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
//        //设置左边框颜色;
//        style.setLeftBorderColor(HSSFColor.BLACK.index);
//        //设置右边框;
//        style.setBorderRight(HSSFCellStyle.BORDER_THIN);
//        //设置右边框颜色;
//        style.setRightBorderColor(HSSFColor.BLACK.index);
//        //设置顶边框;
//        style.setBorderTop(HSSFCellStyle.BORDER_THIN);
//        //设置顶边框颜色;
//        style.setTopBorderColor(HSSFColor.BLACK.index);
//        //在样式用应用设置的字体;
//        style.setFont(font);
//        //设置自动换行;
//        style.setWrapText(false);
//        //设置水平对齐的样式为居中对齐;
//        style.setAlignment(CENTER);
//        //设置垂直对齐的样式为居中对齐;
//        style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
//
//        return style;
//
//    }
}
