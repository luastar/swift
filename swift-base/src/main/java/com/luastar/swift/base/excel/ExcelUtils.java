package com.luastar.swift.base.excel;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.luastar.swift.base.utils.CollectionUtils;
import com.luastar.swift.base.utils.DateUtils;
import com.luastar.swift.base.utils.ObjUtils;
import com.luastar.swift.base.utils.StrUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * poi excel工具类
 */
public class ExcelUtils {

    private static Logger logger = LoggerFactory.getLogger(ExcelUtils.class);

    private static final int MAX_COLUMN_WIDTH = 50;

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
        DataFormat dataFormat = createHelper.createDataFormat();
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
            sheet.setColumnHidden(i, column.isHidden());
        }
        if (CollectionUtils.isEmpty(sheetConfig.getDataList())) {
            logger.info("sheet {} 数据为空", sheet.getSheetName());
            return;
        }
        // 设置内容
        int rowNum = sheetConfig.getDataList().size();
        for (int i = 0; i < rowNum; i++) {
            XSSFRow row = sheet.createRow(i + 1);
            logger.info("写入第{}/{}条数据", row.getRowNum(), rowNum);
            Object data = sheetConfig.getDataList().get(i);
            for (int j = 0; j < columnNum; j++) {
                ExportColumn column = columnList.get(j);
                XSSFCell xssfCell = row.createCell(j);
                // 是否偶数行
                boolean even = (i + 1) % 2 == 0;
                // 行样式
                CellStyle rowCellStyle = ObjUtils.ifNull(column.getRowStyle(), sheetConfig.getRowStyle());
                if (rowCellStyle == null) {
                    if (even) {
                        // 偶数行样式
                        rowCellStyle = ObjUtils.ifNull(column.getEvenRowStyle(), sheetConfig.getEvenRowStyle());
                    } else {
                        // 奇数行样式
                        rowCellStyle = ObjUtils.ifNull(column.getOddRowStyle(), sheetConfig.getOddRowStyle());
                    }
                }
                xssfCell.setCellStyle(rowCellStyle);
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
                    String ifNull = ObjUtils.ifNull(column.getIfNull(), sheetConfig.getIfNull());
                    if (ifNull != null) {
                        // 设置宽度
                        ExcelUtils.setColumnWidthRow(column, sheet, j, ifNull);
                        // 设置为空时的显示值
                        xssfCell.setCellValue(ifNull);
                    }
                    continue;
                }
                // 设置宽度
                ExcelUtils.setColumnWidthRow(column, sheet, j, ObjUtils.toString(valueObj));
                // 设置不同类型的值
                if (column.getType() == ExcelDataType.EnumValue && valueObj instanceof IExcelEnum) {
                    String value = ((IExcelEnum) valueObj).getValue();
                    xssfCell.setCellValue(ObjUtils.ifNull(value, ""));
                } else if (column.getType() == ExcelDataType.IntegerValue
                        || column.getType() == ExcelDataType.LongValue) {
                    BigDecimal value = ObjUtils.toBigDecimal(valueObj, BigDecimal.ZERO).setScale(0);
                    if (value.toString().length() <= 12) {
                        // 注意此处不要直接使用样式clone，excel对样式数量有限制，数据量大时会导致导出的文件打不开
                        if (even) {
                            if (column.getEvenRowStyle() == null) {
                                XSSFCellStyle cellStyle = (XSSFCellStyle) xssfCell.getCellStyle().clone();
                                cellStyle.setDataFormat(dataFormat.getFormat("#0"));
                                column.setEvenRowStyle(cellStyle);
                            } else {
                                column.getEvenRowStyle().setDataFormat(dataFormat.getFormat("#0"));
                            }
                            xssfCell.setCellStyle(column.getEvenRowStyle());
                        } else {
                            if (column.getOddRowStyle() == null) {
                                XSSFCellStyle cellStyle = (XSSFCellStyle) xssfCell.getCellStyle().clone();
                                cellStyle.setDataFormat(dataFormat.getFormat("#0"));
                                column.setOddRowStyle(cellStyle);
                            } else {
                                column.getOddRowStyle().setDataFormat(dataFormat.getFormat("#0"));
                            }
                            xssfCell.setCellStyle(column.getOddRowStyle());
                        }
                        xssfCell.setCellValue(value.longValue());
                    } else {
                        xssfCell.setCellValue(value.toString());
                    }
                } else if (column.getType() == ExcelDataType.BigDecimalValue) {
                    BigDecimal value = ObjUtils.toBigDecimal(valueObj, BigDecimal.ZERO).setScale(column.getScale(), BigDecimal.ROUND_HALF_UP);
                    String format = "#,##0.00";
                    if (column.getScale() > 0 && column.getScale() <= 8) {
                        format = "#,##0." + StringUtils.repeat("0", column.getScale());
                    }
                    if (even) {
                        if (column.getEvenRowStyle() == null) {
                            XSSFCellStyle cellStyle = (XSSFCellStyle) xssfCell.getCellStyle().clone();
                            cellStyle.setDataFormat(dataFormat.getFormat(format));
                            column.setEvenRowStyle(cellStyle);
                        } else {
                            column.getEvenRowStyle().setDataFormat(dataFormat.getFormat(format));
                        }
                        xssfCell.setCellStyle(column.getEvenRowStyle());
                    } else {
                        if (column.getOddRowStyle() == null) {
                            XSSFCellStyle cellStyle = (XSSFCellStyle) xssfCell.getCellStyle().clone();
                            cellStyle.setDataFormat(dataFormat.getFormat(format));
                            column.setOddRowStyle(cellStyle);
                        } else {
                            column.getOddRowStyle().setDataFormat(dataFormat.getFormat(format));
                        }
                        xssfCell.setCellStyle(column.getOddRowStyle());
                    }
                    xssfCell.setCellValue(value.doubleValue());
                } else if (column.getType() == ExcelDataType.DateValue) {
                    xssfCell.setCellValue(DateUtils.format((Date) (valueObj)));
                } else {
                    xssfCell.setCellValue(ObjUtils.toString(valueObj, ""));
                }
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
        DataFormat dataFormat = createHelper.createDataFormat();
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
            sheet.setColumnHidden(i, column.isHidden());
        }
        if (CollectionUtils.isEmpty(sheetConfig.getDataList())) {
            logger.info("sheet {} 数据为空", sheet.getSheetName());
            return;
        }
        // 设置内容
        int rowNum = sheetConfig.getDataList().size();
        for (int i = 0; i < rowNum; i++) {
            HSSFRow row = sheet.createRow(i + 1);
            logger.info("写入第{}/{}条数据", row.getRowNum(), rowNum);
            Object data = sheetConfig.getDataList().get(i);
            for (int j = 0; j < columnNum; j++) {
                ExportColumn column = columnList.get(j);
                HSSFCell hssfCell = row.createCell(j);
                // 行样式
                CellStyle rowCellStyle = ObjUtils.ifNull(column.getRowStyle(), sheetConfig.getRowStyle());
                if (rowCellStyle == null) {
                    if ((i + 1) % 2 == 0) {
                        // 偶数行样式
                        CellStyle evenRowCellStyle = ObjUtils.ifNull(column.getEvenRowStyle(), sheetConfig.getEvenRowStyle());
                        hssfCell.setCellStyle(evenRowCellStyle);
                    } else {
                        // 奇数行样式
                        CellStyle oddRowCellStyle = ObjUtils.ifNull(column.getOddRowStyle(), sheetConfig.getOddRowStyle());
                        hssfCell.setCellStyle(oddRowCellStyle);
                    }
                } else {
                    hssfCell.setCellStyle(rowCellStyle);
                }
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
                    String ifNull = ObjUtils.ifNull(column.getIfNull(), sheetConfig.getIfNull());
                    if (ifNull != null) {
                        // 设置宽度
                        ExcelUtils.setColumnWidthRow(column, sheet, j, ifNull);
                        // 设置为空时的显示值
                        hssfCell.setCellValue(ifNull);
                    }
                    continue;
                }
                // 设置宽度
                ExcelUtils.setColumnWidthRow(column, sheet, j, ObjUtils.toString(valueObj));
                // 设置不同类型的值
                if (column.getType() == ExcelDataType.EnumValue && valueObj instanceof IExcelEnum) {
                    String value = ((IExcelEnum) valueObj).getValue();
                    hssfCell.setCellValue(ObjUtils.ifNull(value, ""));
                } else if (column.getType() == ExcelDataType.IntegerValue
                        || column.getType() == ExcelDataType.LongValue) {
                    BigDecimal value = ObjUtils.toBigDecimal(valueObj, BigDecimal.ZERO).setScale(0);
                    if (value.toString().length() <= 12) {
                        HSSFCellStyle cellStyle = hssfCell.getCellStyle();
                        cellStyle.setDataFormat(dataFormat.getFormat("#0"));
                        hssfCell.setCellStyle(cellStyle);
                        hssfCell.setCellValue(value.longValue());
                    } else {
                        hssfCell.setCellValue(value.toString());
                    }
                } else if (column.getType() == ExcelDataType.BigDecimalValue) {
                    BigDecimal value = ObjUtils.toBigDecimal(valueObj, BigDecimal.ZERO).setScale(column.getScale(), BigDecimal.ROUND_HALF_UP);
                    String format = "#,##0.00";
                    if (column.getScale() > 0 && column.getScale() <= 8) {
                        format = "#,##0." + StringUtils.repeat("0", column.getScale());
                    }
                    HSSFCellStyle cellStyle = hssfCell.getCellStyle();
                    cellStyle.setDataFormat(dataFormat.getFormat(format));
                    hssfCell.setCellStyle(cellStyle);
                    hssfCell.setCellValue(value.doubleValue());
                } else if (column.getType() == ExcelDataType.DateValue) {
                    hssfCell.setCellValue(DateUtils.format((Date) (valueObj)));
                } else {
                    hssfCell.setCellValue(ObjUtils.toString(valueObj, ""));
                }
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
        // 公式执行器
        CreationHelper createHelper = workbook.getCreationHelper();
        FormulaEvaluator formulaEvaluator = createHelper.createFormulaEvaluator();
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
        XSSFRow titleRow = sheet.getRow(firstRowNum);
        int titleNum = columnList.size();
        int columnNum = titleRow.getLastCellNum();
        for (int i = 0; i < titleNum; i++) {
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
            if (row == null) {
                ExcelData excelData = new ExcelData(i, data);
                excelData.setCheckMsg("获取行数据为空");
                dataList.add(excelData);
                continue;
            }
            // 行不为空
            List<String> setPropList = Lists.newArrayList();
            for (int j = 0; j < titleNum; j++) {
                ImportColumn column = columnList.get(j);
                XSSFCell cell = row.getCell(column.getColumnIndex());
                setPropList.add(ExcelUtils.setProperty(column, cell, data, formulaEvaluator));
            }
            ExcelData excelData = new ExcelData(i, data);
            // 赋值失败的列
            List setErrList = setPropList.stream().filter(rs -> rs != null).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(setErrList)) {
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
        // 公式执行器
        CreationHelper createHelper = workbook.getCreationHelper();
        FormulaEvaluator formulaEvaluator = createHelper.createFormulaEvaluator();
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
        HSSFRow titleRow = sheet.getRow(firstRowNum);
        int titleNum = columnList.size();
        int columnNum = titleRow.getLastCellNum();
        for (int i = 0; i < titleNum; i++) {
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
            if (row == null) {
                ExcelData excelData = new ExcelData(i, data);
                excelData.setCheckMsg("获取行数据为空");
                dataList.add(excelData);
                continue;
            }
            // 行不为空
            List<String> setPropList = Lists.newArrayList();
            for (int j = 0; j < titleNum; j++) {
                ImportColumn column = columnList.get(j);
                HSSFCell cell = row.getCell(column.getColumnIndex());
                setPropList.add(ExcelUtils.setProperty(column, cell, data, formulaEvaluator));
            }
            ExcelData excelData = new ExcelData(row.getRowNum(), data);
            // 赋值失败的列
            List setErrList = setPropList.stream().filter(rs -> rs != null).collect(Collectors.toList());
            if (!setErrList.isEmpty()) {
                String msg = StrUtils.formatString("获取属性失败：{0}", StringUtils.join(setErrList, ","));
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
        setColumnWidthRow(column, sheet, colIndex, title);
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
                // 前后加几个字符的边距，跟字体大小有关，先设置成固定的
                int width = value.getBytes("UTF-8").length + 5;
                // 最大设置成最大值，太长了也难看
                if (width >= MAX_COLUMN_WIDTH) {
                    width = MAX_COLUMN_WIDTH;
                }
                // 如果比默认值大，则设置更大的值
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
    private static String setProperty(ImportColumn column, Cell cell, Object data, FormulaEvaluator formulaEvaluator) {
        try {
            if (cell == null || data == null) {
                return null;
            }
            CellType cellType = cell.getCellTypeEnum();
            if (cellType == CellType.FORMULA) {
                // 如果是公式，先执行公式
                cellType = formulaEvaluator.evaluate(cell).getCellTypeEnum();
            }
            Object cellValue;
            switch (cellType) {
                case NUMERIC:
                    if (DateUtil.isCellDateFormatted(cell)) {
                        cellValue = cell.getDateCellValue();
                    } else {
                        cellValue = cell.getNumericCellValue();
                    }
                    break;
                case BOOLEAN:
                    cellValue = cell.getBooleanCellValue();
                    break;
                default:
                    cellValue = cell.getStringCellValue();
                    break;
            }
            if (column.getType() == ExcelDataType.StringValue) {
                // 如果需要获取字符串，转换格式为文本类型后获取字符串值，避免出现取到数值类型为xx.0的值。
                cell.setCellType(CellType.STRING);
                cellValue = cell.getStringCellValue();
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
                if (cellValue instanceof Date) {
                    PropertyUtils.setProperty(data, column.getProp(), cellValue);
                } else {
                    // 尝试转换
                    cellValue = DateUtils.parse(ObjUtils.toString(cellValue), "yyyy-MM-dd HH:mm:ss");
                    if (cellValue == null) {
                        logger.info("列【{}】获取不到日期值。", column.getTitle());
                        return column.getTitle();
                    }
                    PropertyUtils.setProperty(data, column.getProp(), cellValue);
                }
            } else if (column.getType() == ExcelDataType.EnumValue) {
                if (column.getStaticClass() == null || StringUtils.isEmpty(column.getStaticMethodName())) {
                    logger.info("列【{}】没有设置获取枚举的静态类和方法。", column.getTitle());
                    return column.getTitle();
                }
                Object value = MethodUtils.invokeStaticMethod(column.getStaticClass(), column.getStaticMethodName(), cell.getStringCellValue());
                if (value == null) {
                    logger.info("列【{}】获取不到枚举值。", column.getTitle(), column.getProp(), cell.getStringCellValue());
                    return column.getTitle();
                }
                PropertyUtils.setProperty(data, column.getProp(), value);
            } else {
                PropertyUtils.setProperty(data, column.getProp(), cell.getStringCellValue());
            }
            return null;
        } catch (Exception e) {
            logger.warn("列【" + column.getTitle() + "】获取值异常：" + e.getMessage(), e);
            return column.getTitle();
        }
    }

    public static void main(String[] args) throws Exception {
        writeExample();
//        readExample();
    }

    /**
     * 写例子
     *
     * @throws Exception
     */
    private static void writeExample() throws Exception {
        List<Map<String, Object>> resultList = Lists.newArrayList();
        resultList.add(new ImmutableMap.Builder<String, Object>()
                .put("col1", "row1")
                .put("col2", "男")
                .put("col3", 9999999999999998L)
                .put("col4", 123456.654321)
                .put("col5", 123456)
                .put("col6", true)
                .put("col7", new Date())
                .build());
        resultList.add(new ImmutableMap.Builder<String, Object>()
                .put("col1", "row2")
                .put("col2", "女")
                .put("col3", 999999999999999L)
                .put("col4", 34.6)
                .put("col5", -123456.123456)
                .put("col6", false)
                .put("col7", new Date())
                .build());
        resultList.add(new ImmutableMap.Builder<String, Object>()
                .put("col1", "row1")
                .put("col2", "男")
                .put("col3", 9999999999999998L)
                .put("col4", 123456.654321)
                .put("col5", 123456)
                .put("col6", true)
                .put("col7", new Date())
                .build());
        resultList.add(new ImmutableMap.Builder<String, Object>()
                .put("col1", "row2")
                .put("col2", "女")
                .put("col3", 999999999999999L)
                .put("col4", 34.6)
                .put("col5", -123456.123456)
                .put("col6", false)
                .put("col7", new Date())
                .build());
        XSSFWorkbook workbook = ExcelUtils.newXlsxWorkbook();
        List<ExportColumn> columnList = Lists.newArrayList(
                new ExportColumn("测试列1", "col1", ExcelDataType.StringValue, true),
                new ExportColumn("测试列2", "col2", ExcelDataType.EnumValue, SexEnum.getValues()),
                new ExportColumn("测试列3", "col3", ExcelDataType.LongValue),
                new ExportColumn("测试列4", "col4", ExcelDataType.BigDecimalValue, 3),
                new ExportColumn("测试列5", "col5", ExcelDataType.BigDecimalValue, 5),
                new ExportColumn("测试列6", "col6", ExcelDataType.BooleanValue),
                new ExportColumn("测试列7", "col7", ExcelDataType.DateValue)
        );

        CellStyle oddStyle = workbook.createCellStyle();
        oddStyle.setWrapText(true); // 设置自动换行
        oddStyle.setAlignment(HorizontalAlignment.LEFT); // 左右对齐
        oddStyle.setVerticalAlignment(VerticalAlignment.CENTER); // 垂直对齐
        oddStyle.setFillForegroundColor(IndexedColors.BLUE.getIndex());
        oddStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        CellStyle evenStyle = workbook.createCellStyle();
        evenStyle.setWrapText(true); // 设置自动换行
        evenStyle.setAlignment(HorizontalAlignment.LEFT); // 左右对齐
        evenStyle.setVerticalAlignment(VerticalAlignment.CENTER); // 垂直对齐
        evenStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
        evenStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        ExportSheet exportSheet = new ExportSheet(columnList, resultList)
                .setOddRowStyle(oddStyle)
                .setEvenRowStyle(evenStyle);
        ExcelUtils.writeXlsxWorkbook(workbook, exportSheet);
        workbook.write(new FileOutputStream(new File("/Users/zhuminghua/Downloads/test.xlsx")));
    }

    /**
     * 读例子
     *
     * @throws Exception
     */
    private static void readExample() throws Exception {
        File file = new File("/Users/zhuminghua/Downloads/test.xlsx");
        List<ImportColumn> columnList = Lists.newArrayList(
                new ImportColumn("测试列1", "col1", ExcelDataType.StringValue),
                new ImportColumn("测试列2", "col2", ExcelDataType.EnumValue, SexEnum.class, "getByValue"),
                new ImportColumn("测试列3", "col3", ExcelDataType.StringValue)
        );
        ImportSheet importSheet = new ImportSheet(columnList, LinkedHashMap.class);
        ExcelUtils.readXlsxExcel(file, importSheet);
        System.out.println(JSON.toJSONString(importSheet.getDataList()));
    }

}
