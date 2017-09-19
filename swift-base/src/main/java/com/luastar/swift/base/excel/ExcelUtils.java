package com.luastar.swift.base.excel;

import com.google.common.collect.Lists;
import com.luastar.swift.base.json.JsonUtils;
import com.luastar.swift.base.utils.CollectionUtils;
import com.luastar.swift.base.utils.ObjUtils;
import com.luastar.swift.base.utils.StrUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * poi excel工具类
 */
public class ExcelUtils {

    private static Logger logger = LoggerFactory.getLogger(ExcelUtils.class);

    /**
     * 初始化一个XSSFWorkbook（.xlsx文件）
     *
     * @return
     */
    public static XSSFWorkbook newXlsxWorkbook() {
        return new XSSFWorkbook();
    }

    /**
     * 初始化一个HSSFWorkbook（.xls文件）
     *
     * @return
     */
    public static HSSFWorkbook newXlsWorkbook() {
        return new HSSFWorkbook();
    }

    /**
     * 将数据写入workbook
     *
     * @param workbook
     * @param sheetConfig
     * @throws Exception
     */
    public static void writeXlsxWorkbook(XSSFWorkbook workbook, ExportSheet... sheetConfig) throws Exception {
        if (workbook == null || ArrayUtils.isEmpty(sheetConfig)) {
            throw new IllegalArgumentException("excel导出参数错误！");
        }
        for (int i = 0, l = sheetConfig.length; i < l; i++) {
            if (StringUtils.isEmpty(sheetConfig[i].getName())) {
                sheetConfig[i].setName("sheet" + (i + 1));
            }
            writeXlsxSheet(workbook, sheetConfig[i]);
        }
    }

    /**
     * 将数据写入sheet
     *
     * @param workbook
     * @param sheetConfig
     */
    private static void writeXlsxSheet(XSSFWorkbook workbook, ExportSheet sheetConfig) throws Exception {
        if (workbook == null
                || sheetConfig == null
                || CollectionUtils.isEmpty(sheetConfig.getColumnList())) {
            throw new IllegalArgumentException("excel导出参数错误！");
        }
        CreationHelper createHelper = workbook.getCreationHelper();
        XSSFSheet sheet = workbook.createSheet(ObjUtils.ifNull(sheetConfig.getName(), "sheet1"));
        XSSFDataValidationHelper dvHelper = new XSSFDataValidationHelper(sheet);
        // 设置标题
        int columnNum = sheetConfig.getColumnList().size();
        List<ExportColumn> columnList = sheetConfig.getColumnList();
        XSSFRow rowTitle = sheet.createRow(0);
        for (int i = 0; i < columnNum; i++) {
            ExportColumn column = columnList.get(i);
            String title = ObjUtils.ifNull(column.getTitle(), "");
            XSSFCell cell = rowTitle.createCell(i);
            cell.setCellStyle(ObjUtils.ifNull(column.getTitleStyle(), sheetConfig.getTitleStyle()));
            cell.setCellValue(createHelper.createRichTextString(title));
            ExcelUtils.setColumnWidthTitle(column, sheet, i, title);
        }
        if (CollectionUtils.isEmpty(sheetConfig.getDataList())) {
            logger.info("sheet {} 数据为空", sheet.getSheetName());
            return;
        }
        // 设置内容
        int rowNum = sheetConfig.getDataList().size();
        for (int i = 0; i < rowNum; i++) {
            XSSFRow row = sheet.createRow(i + 1);
            Object data = sheetConfig.getDataList().get(i);
            for (int j = 0; j < columnNum; j++) {
                ExportColumn column = columnList.get(j);
                XSSFCell cell = row.createCell(j);
                cell.setCellStyle(ObjUtils.ifNull(column.getRowStyle(), sheetConfig.getRowStyle()));
                // 设置下拉框
                if (i == 0 && column.getType() == ExcelDataType.EnumValue && ArrayUtils.isNotEmpty(column.getValueArray())) {
                    XSSFDataValidationConstraint dvConstraint = (XSSFDataValidationConstraint) dvHelper.createExplicitListConstraint(column.getValueArray());
                    CellRangeAddressList addressList = new CellRangeAddressList(1, rowNum + 1, j, j);
                    XSSFDataValidation validation = (XSSFDataValidation) dvHelper.createValidation(dvConstraint, addressList);
                    validation.setSuppressDropDownArrow(true);
                    validation.setShowErrorBox(true);
                    sheet.addValidationData(validation);
                }
                // 获取列值
                Object valueObj;
                if (data instanceof Map) {
                    valueObj = ((Map) data).get(column.getProp());
                } else {
                    valueObj = PropertyUtils.getProperty(data, column.getProp());
                }
                if (valueObj == null) {
                    logger.info("获取不到对象{}的属性{}值", JsonUtils.toJson(data), column.getProp());
                    continue;
                }
                String value;
                if (column.getType() == ExcelDataType.EnumValue && valueObj instanceof IExcelEnum) {
                    value = ((IExcelEnum) valueObj).getValue();
                } else {
                    value = ObjUtils.toString(valueObj);
                }
                // 设置列值
                cell.setCellValue(createHelper.createRichTextString(ObjUtils.ifNull(value, "")));
                // 设置宽度
                ExcelUtils.setColumnWidthRow(column, sheet, j, value);
            }
        }
    }

    /**
     * 将数据写入workbook
     *
     * @param workbook
     * @param sheetConfig
     * @throws Exception
     */
    public static void writeXlsWorkbook(HSSFWorkbook workbook, ExportSheet... sheetConfig) throws Exception {
        if (ArrayUtils.isEmpty(sheetConfig)) {
            throw new IllegalArgumentException("excel导出参数错误！");
        }
        for (int i = 0, l = sheetConfig.length; i < l; i++) {
            if (StringUtils.isEmpty(sheetConfig[i].getName())) {
                sheetConfig[i].setName("sheet" + (i + 1));
            }
            writeXlsSheet(workbook, sheetConfig[i]);
        }
    }

    /**
     * 将数据写入sheet
     *
     * @param workbook
     * @param sheetConfig
     * @throws Exception
     */
    private static void writeXlsSheet(HSSFWorkbook workbook, ExportSheet sheetConfig) throws Exception {
        if (workbook == null
                || sheetConfig == null
                || CollectionUtils.isEmpty(sheetConfig.getColumnList())) {
            throw new IllegalArgumentException("excel导出参数错误！");
        }
        CreationHelper createHelper = workbook.getCreationHelper();
        HSSFSheet sheet = workbook.createSheet(ObjUtils.ifNull(sheetConfig.getName(), "sheet1"));
        // 设置标题
        int columnNum = sheetConfig.getColumnList().size();
        List<ExportColumn> columnList = sheetConfig.getColumnList();
        HSSFRow rowTitle = sheet.createRow(0);
        for (int i = 0; i < columnNum; i++) {
            ExportColumn column = columnList.get(i);
            String title = ObjUtils.ifNull(column.getTitle(), "");
            HSSFCell cell = rowTitle.createCell(i);
            cell.setCellValue(createHelper.createRichTextString(title));
            cell.setCellStyle(ObjUtils.ifNull(column.getTitleStyle(), sheetConfig.getTitleStyle()));
            ExcelUtils.setColumnWidthTitle(column, sheet, i, title);
        }
        if (CollectionUtils.isEmpty(sheetConfig.getDataList())) {
            logger.info("sheet {} 数据为空", sheet.getSheetName());
            return;
        }
        // 设置内容
        int rowNum = sheetConfig.getDataList().size();
        for (int i = 0; i < rowNum; i++) {
            HSSFRow row = sheet.createRow(i + 1);
            Object data = sheetConfig.getDataList().get(i);
            for (int j = 0; j < columnNum; j++) {
                ExportColumn column = columnList.get(j);
                HSSFCell cell = row.createCell(j);
                cell.setCellStyle(ObjUtils.ifNull(column.getRowStyle(), sheetConfig.getRowStyle()));
                if (i == 0 && column.getType() == ExcelDataType.EnumValue && ArrayUtils.isNotEmpty(column.getValueArray())) {
                    CellRangeAddressList addressList = new CellRangeAddressList(1, rowNum + 1, j, j);
                    DVConstraint dvConstraint = DVConstraint.createExplicitListConstraint(column.getValueArray());
                    DataValidation dataValidation = new HSSFDataValidation(addressList, dvConstraint);
                    dataValidation.setSuppressDropDownArrow(true);
                    dataValidation.setShowErrorBox(true);
                    sheet.addValidationData(dataValidation);
                }
                // 获取列值
                Object valueObj;
                if (data instanceof Map) {
                    valueObj = ((Map) data).get(column.getProp());
                } else {
                    valueObj = PropertyUtils.getProperty(data, column.getProp());
                }
                if (valueObj == null) {
                    logger.info("获取不到对象{}的属性{}值", JsonUtils.toJson(data), column.getProp());
                    continue;
                }
                String value;
                if (column.getType() == ExcelDataType.EnumValue && valueObj instanceof IExcelEnum) {
                    value = ((IExcelEnum) valueObj).getValue();
                } else {
                    value = ObjUtils.toString(valueObj);
                }
                // 设置列值
                cell.setCellValue(createHelper.createRichTextString(value));
                // 设置宽度
                ExcelUtils.setColumnWidthRow(column, sheet, j, value);
            }
        }
    }

    /**
     * 从文件读取excel
     *
     * @param file
     * @param sheetConfig
     * @throws Exception
     */
    public static void readXlsxExcel(File file, ImportSheet... sheetConfig) throws Exception {
        if (file == null || ArrayUtils.isEmpty(sheetConfig)) {
            throw new IllegalArgumentException("excel导入参数错误！");
        }
        XSSFWorkbook workbook = new XSSFWorkbook(file);
        for (int i = 0; i < sheetConfig.length; i++) {
            if (sheetConfig[i].getIndex() == null) {
                sheetConfig[i].setIndex(i);
            }
            readXlsxSheet(workbook, sheetConfig[i]);
        }
    }

    /**
     * 从输入流读取excel
     *
     * @param sheetConfig
     * @return
     */
    public static void readXlsxExcel(InputStream inputStream, ImportSheet... sheetConfig) throws Exception {
        if (inputStream == null || ArrayUtils.isEmpty(sheetConfig)) {
            throw new IllegalArgumentException("excel导入参数错误！");
        }
        XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
        for (int i = 0; i < sheetConfig.length; i++) {
            if (sheetConfig[i].getIndex() == null) {
                sheetConfig[i].setIndex(i);
            }
            readXlsxSheet(workbook, sheetConfig[i]);
        }
    }

    /**
     * 从sheet中读取数据
     *
     * @param workbook
     * @param sheetConfig
     * @throws Exception
     */
    private static void readXlsxSheet(XSSFWorkbook workbook, ImportSheet sheetConfig) throws Exception {
        if (workbook == null || sheetConfig == null
                || sheetConfig.getDataClass() == null
                || sheetConfig.getColumnList() == null) {
            throw new IllegalArgumentException("excel导入参数错误！");
        }
        int sheetNum = workbook.getNumberOfSheets();
        if (sheetConfig.getIndex() >= sheetNum) {
            String msg = StrUtils.formatString("sheet【{0}】不存在", sheetConfig.getIndex() + 1);
            throw new RuntimeException(msg);
        }
        XSSFSheet sheet = workbook.getSheetAt(sheetConfig.getIndex());
        int firstRowNum = sheet.getFirstRowNum();
        int lastRowNum = sheet.getLastRowNum();
        if (lastRowNum < 1) {
            String msg = StrUtils.formatString("sheet【{0}】数据为空", sheet.getSheetName());
            throw new RuntimeException(msg);
        }
        // 通过标题找对应的列
        List<String> columnNotFound = Lists.newArrayList();
        List<ImportColumn> columnList = sheetConfig.getColumnList();
        int columnNum = columnList.size();
        XSSFRow titleRow = sheet.getRow(firstRowNum);
        for (int i = 0; i < columnNum; i++) {
            ImportColumn column = columnList.get(i);
            for (int j = 0; j < columnNum; j++) {
                XSSFCell cell = titleRow.getCell(j);
                if (cell != null && column.getTitle().equals(cell.getStringCellValue())) {
                    column.setColumnIndex(j);
                }
            }
            if (column.getColumnIndex() == null) {
                columnNotFound.add(column.getTitle());
            }
        }
        // 找不到对应的列
        if (columnNotFound.size() > 0) {
            String msg = StrUtils.formatString("列【{0}】不存在", StringUtils.join(columnNotFound, "，"));
            throw new RuntimeException(msg);
        }
        // 获取数据
        List<ExcelData> dataList = Lists.newArrayList();
        for (int i = firstRowNum + 1; i <= lastRowNum; i++) {
            XSSFRow row = sheet.getRow(i);
            Object data = sheetConfig.getDataClass().newInstance();
            List<String> setRsList = Lists.newArrayList();
            for (int j = 0; j < columnNum; j++) {
                ImportColumn column = columnList.get(j);
                XSSFCell cell = row.getCell(column.getColumnIndex());
                setRsList.add(ExcelUtils.setProperty(column, cell, data));
            }
            ExcelData excelData = new ExcelData(row.getRowNum(), data);
            // 赋值失败的列
            List setErrList = setRsList.stream().filter(rs -> rs != null).collect(Collectors.toList());
            if (!setErrList.isEmpty()) {
                String msg = StrUtils.formatString("获取以下属性的值失败：{0}", StringUtils.join(setErrList, ","));
                excelData.setCheckMsg(msg);
            }
            dataList.add(excelData);
        }
        sheetConfig.setDataList(dataList);
    }

    /**
     * 从文件读取excel
     *
     * @param file
     * @param sheetConfig
     * @throws Exception
     */
    public static void readXlsExcel(File file, ImportSheet... sheetConfig) throws Exception {
        if (file == null || ArrayUtils.isEmpty(sheetConfig)) {
            throw new IllegalArgumentException("excel导入参数错误！");
        }
        FileInputStream fis = new FileInputStream(file);
        HSSFWorkbook workbook = new HSSFWorkbook(fis);
        for (int i = 0; i < sheetConfig.length; i++) {
            if (sheetConfig[i].getIndex() == null) {
                sheetConfig[i].setIndex(i);
            }
            readXlsSheet(workbook, sheetConfig[i]);
        }
    }

    /**
     * 从输入流读取excel
     *
     * @param inputStream
     * @param sheetConfig
     * @throws Exception
     */
    public static void readXlsExcel(InputStream inputStream, ImportSheet... sheetConfig) throws Exception {
        if (inputStream == null || ArrayUtils.isEmpty(sheetConfig)) {
            throw new IllegalArgumentException("excel导入参数错误！");
        }
        HSSFWorkbook workbook = new HSSFWorkbook(inputStream);
        for (int i = 0; i < sheetConfig.length; i++) {
            if (sheetConfig[i].getIndex() == null) {
                sheetConfig[i].setIndex(i);
            }
            readXlsSheet(workbook, sheetConfig[i]);
        }
    }

    /**
     * 从sheet中读取数据
     *
     * @param workbook
     * @param sheetConfig
     * @throws Exception
     */
    private static void readXlsSheet(HSSFWorkbook workbook, ImportSheet sheetConfig) throws Exception {
        if (workbook == null || sheetConfig == null
                || sheetConfig.getDataClass() == null
                || sheetConfig.getColumnList() == null) {
            throw new IllegalArgumentException("excel导入参数错误！");
        }
        List<String> errorList = Lists.newArrayList();
        int sheetNum = workbook.getNumberOfSheets();
        if (sheetConfig.getIndex() >= sheetNum) {
            String msg = StrUtils.formatString("sheet【{0}】不存在", sheetConfig.getIndex() + 1);
            throw new RuntimeException(msg);
        }
        HSSFSheet sheet = workbook.getSheetAt(sheetConfig.getIndex());
        int firstRowNum = sheet.getFirstRowNum();
        int lastRowNum = sheet.getLastRowNum();
        if (lastRowNum <= 1) {
            String msg = StrUtils.formatString("sheet【{0}】数据为空", sheet.getSheetName());
            throw new RuntimeException(msg);
        }
        // 通过标题找对应的列
        List<String> columnNotFound = Lists.newArrayList();
        List<ImportColumn> columnList = sheetConfig.getColumnList();
        int columnNum = columnList.size();
        HSSFRow titleRow = sheet.getRow(firstRowNum);
        for (int i = 0; i < columnNum; i++) {
            ImportColumn column = columnList.get(i);
            for (int j = 0; j < columnNum; j++) {
                HSSFCell cell = titleRow.getCell(j);
                if (cell != null && column.getTitle().equals(cell.getStringCellValue())) {
                    column.setColumnIndex(j);
                }
            }
            if (column.getColumnIndex() == null) {
                columnNotFound.add(column.getTitle());
            }
        }
        // 找不到对应的列
        if (columnNotFound.size() > 0) {
            String msg = StrUtils.formatString("列【{0}】不存在", StringUtils.join(columnNotFound, "，"));
            throw new RuntimeException(msg);
        }
        // 获取数据
        List<ExcelData> dataList = Lists.newArrayList();
        for (int i = firstRowNum + 1; i <= lastRowNum; i++) {
            HSSFRow row = sheet.getRow(i);
            Object data = sheetConfig.getDataClass().newInstance();
            List<String> setRsList = Lists.newArrayList();
            for (int j = 0; j < columnNum; j++) {
                ImportColumn column = columnList.get(j);
                HSSFCell cell = row.getCell(column.getColumnIndex());
                setRsList.add(ExcelUtils.setProperty(column, cell, data));
            }
            ExcelData excelData = new ExcelData(row.getRowNum(), data);
            // 赋值失败的列
            List setErrList = setRsList.stream().filter(rs -> rs != null).collect(Collectors.toList());
            if (!setErrList.isEmpty()) {
                String msg = StrUtils.formatString("获取以下属性的值失败：{0}", StringUtils.join(setErrList, ","));
                excelData.setCheckMsg(msg);
            }
            dataList.add(excelData);
        }
        sheetConfig.setDataList(dataList);
    }

    /**
     * 全部样式设置，用于参考
     *
     * @param wb
     * @return
     */
    public static CellStyle createCellStyleDemo(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        // 设置文本格式
        style.setDataFormat(wb.createDataFormat().getFormat("@"));
        // 设置自动换行
        style.setWrapText(true);
        // 左右对齐
        style.setAlignment(HorizontalAlignment.LEFT);
        // 垂直对齐
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        // 设置边框
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        // 设置边框颜色
        style.setTopBorderColor(IndexedColors.BLACK.getIndex());
        style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        style.setRightBorderColor(IndexedColors.BLACK.getIndex());
        // 设置背景色
        style.setFillBackgroundColor(IndexedColors.GREY_25_PERCENT.index);
        // 设置前景色
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.index);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        // 设置字体
        Font font = wb.createFont();
        font.setFontHeightInPoints((short) 16);
        font.setItalic(false);//斜体
        font.setStrikeout(false);//删除线
        style.setFont(font);
        return style;
    }

    /**
     * 创建excel标题样式
     *
     * @param wb
     * @return
     */
    public static CellStyle createCellStyleTitle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        // 设置文本格式
        style.setDataFormat(wb.createDataFormat().getFormat("@"));
        // 设置自动换行
        style.setWrapText(true);
        // 左右对齐
        style.setAlignment(HorizontalAlignment.LEFT);
        // 垂直对齐
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        // 设置前景色
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.index);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        // 设置字体
        Font font = wb.createFont();
        font.setFontHeightInPoints((short) 16);
        font.setItalic(false);//斜体
        font.setStrikeout(false);//删除线
        style.setFont(font);
        return style;
    }

    /**
     * 创建excel普通行样式
     *
     * @param wb
     * @return
     */
    public static CellStyle createCellStyleRow(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        // 设置文本格式
        style.setDataFormat(wb.createDataFormat().getFormat("@"));
        // 设置自动换行
        style.setWrapText(true);
        // 左右对齐
        style.setAlignment(HorizontalAlignment.LEFT);
        // 垂直对齐
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    /**
     * 设置标题的宽度
     *
     * @param column
     * @param sheet
     * @param colIndex
     * @param title
     * @throws UnsupportedEncodingException
     */
    private static void setColumnWidthTitle(ExportColumn column, Sheet sheet, int colIndex, String title) throws UnsupportedEncodingException {
        if (column.getWidth() == null) {
            column.setAutoWidth(true);
            column.setWidth(sheet.getDefaultColumnWidth());
        }
        if (column.isAutoWidth()) {
            if (StringUtils.isNotEmpty(title)) {
                // 如果比默认值大，则设置更大的值
                int width = title.getBytes("UTF-8").length * 2; // 直接按2倍算
                // 最大设置成100，太长了也难看
                if (width >= 100) {
                    width = 100;
                }
                if (width > column.getWidth()) {
                    column.setWidth(width);
                    sheet.setColumnWidth(colIndex, width * 256);
                }
            }
        } else {
            sheet.setColumnWidth(colIndex, column.getWidth() * 256);
        }
    }

    /**
     * 设置数据行的宽度
     *
     * @param column
     * @param sheet
     * @param colIndex
     * @param value
     * @throws UnsupportedEncodingException
     */
    private static void setColumnWidthRow(ExportColumn column, Sheet sheet, int colIndex, String value) throws UnsupportedEncodingException {
        if (column.isAutoWidth()) {
            if (StringUtils.isNotEmpty(value)) {
                // 如果比默认值大，则设置更大的值
                int width = value.getBytes("UTF-8").length + 5; // 前后加点边距
                // 最大设置成100，太长了也难看
                if (width >= 100) {
                    width = 100;
                }
                if (width > column.getWidth()) {
                    column.setWidth(width);
                    sheet.setColumnWidth(colIndex, width * 256);
                }
            }
        }
    }

    /**
     * 从cell读取属性赋值给对象
     * 返回赋值失败的列名
     *
     * @param column
     * @param cell
     * @param data
     * @return
     */
    private static String setProperty(ImportColumn column, Cell cell, Object data) {
        try {
            if (cell == null || data == null) {
                return null;
            }
            Object cellValue;
            switch (cell.getCellTypeEnum()) {
                case NUMERIC:
                    cellValue = BigDecimal.valueOf(cell.getNumericCellValue());
                    break;
                case BOOLEAN:
                    cellValue = cell.getBooleanCellValue();
                    break;
                default:
                    cellValue = cell.getStringCellValue();
                    break;
            }
            if (column.getType() == ExcelDataType.StringValue) {
                PropertyUtils.setProperty(data, column.getProp(), ObjUtils.toString(cellValue));
            } else if (column.getType() == ExcelDataType.LongValue) {
                PropertyUtils.setProperty(data, column.getProp(), ObjUtils.toLong(cellValue));
            } else if (column.getType() == ExcelDataType.IntegerValue) {
                PropertyUtils.setProperty(data, column.getProp(), ObjUtils.toInteger(cellValue));
            } else if (column.getType() == ExcelDataType.BigDecimalValue) {
                PropertyUtils.setProperty(data, column.getProp(), ObjUtils.toBigDecimal(cellValue));
            } else if (column.getType() == ExcelDataType.BooleanValue) {
                PropertyUtils.setProperty(data, column.getProp(), ObjUtils.toBoolean(cellValue));
            } else if (column.getType() == ExcelDataType.DateValue) {
                PropertyUtils.setProperty(data, column.getProp(), cell.getDateCellValue());
            } else if (column.getType() == ExcelDataType.EnumValue) {
                if (StringUtils.isEmpty(column.getStaticMethodName())) {
                    logger.warn("没有设置获取枚举【{}】的静态方法", column.getTitle());
                    return column.getTitle();
                }
                Field enumField = FieldUtils.getField(data.getClass(), column.getProp(), true);
                if (enumField == null) {
                    logger.warn("获取不到枚举【{}】的属性【{}】定义", column.getTitle(), column.getProp());
                    return column.getTitle();
                }
                Object value = MethodUtils.invokeStaticMethod(enumField.getType(), column.getStaticMethodName(), cell.getStringCellValue());
                if (value == null) {
                    logger.info("获取不到枚举【{}】【{}】对应的值【{}】", column.getTitle(), column.getProp(), cell.getStringCellValue());
                    return column.getTitle();
                }
                PropertyUtils.setProperty(data, column.getProp(), value);
            } else {
                PropertyUtils.setProperty(data, column.getProp(), cell.getStringCellValue());
            }
            return null;
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
            return column.getTitle();
        }
    }

}
