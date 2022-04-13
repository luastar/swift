package com.luastar.swift.base.excel;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.luastar.swift.base.utils.CollectionUtils;
import com.luastar.swift.base.utils.DateUtils;
import com.luastar.swift.base.utils.ObjUtils;
import com.luastar.swift.base.utils.StrUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.util.XMLHelper;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.*;
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
     * 初始化一个SXSSFWorkbook（.xlsx文件）
     * 支持大数据量导出，注意使用完后调用dispose()方法
     * <code>
     * try {
     * ExcelUtils.writeBigXlsxWorkbook(workbook, exportSheet);
     * workbook.write(new FileOutputStream(new File("/Users/zhuminghua/Downloads/test.xlsx")));
     * } finally {
     * workbook.dispose();
     * }
     * </code>
     *
     * @return
     */
    public static SXSSFWorkbook newBigXlsxWorkbook() {
        SXSSFWorkbook sxssfWorkbook = new SXSSFWorkbook();
        sxssfWorkbook.setCompressTempFiles(true);
        return sxssfWorkbook;
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
     * 将数据写入workbook
     *
     * @param workbook
     * @param sheetConfig
     * @throws Exception
     */
    public static void writeBigXlsxWorkbook(SXSSFWorkbook workbook, ExportSheet... sheetConfig) throws Exception {
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
    private static void writeXlsxSheet(Workbook workbook, ExportSheet sheetConfig) throws Exception {
        if (workbook == null
                || sheetConfig == null
                || CollectionUtils.isEmpty(sheetConfig.getColumnList())) {
            throw new IllegalArgumentException("excel导出参数错误！");
        }
        CreationHelper createHelper = workbook.getCreationHelper();
        DataFormat dataFormat = createHelper.createDataFormat();
        String sheetName = ObjUtils.ifNull(sheetConfig.getName(), "sheet1");
        Sheet sheet = workbook.getSheet(sheetName);
        if (sheet == null) {
            sheet = workbook.createSheet(sheetName);
            if (sheet instanceof SXSSFSheet) {
                ((SXSSFSheet) sheet).trackAllColumnsForAutoSizing();
            }
        }
        DataValidationHelper dvHelper = sheet.getDataValidationHelper();
        List<ExportColumn> columnList = sheetConfig.getColumnList();
        int columnNum = columnList.size();
        // 开始行
        int startRow = 0;
        if (sheetConfig.getAppend() != null && sheetConfig.getAppend()) {
            startRow = sheet.getLastRowNum() + 1;
        }
        if (startRow == 0) {
            // 设置标题
            Row rowTitle = sheet.createRow(0);
            // 设置标题行高
            if (sheetConfig.getTitleHeight() != null
                    && sheetConfig.getTitleHeight() >= 0
                    && sheetConfig.getTitleHeight() <= 409) {
                rowTitle.setHeight((short) (sheetConfig.getTitleHeight() * 20));
            }
            for (int i = 0; i < columnNum; i++) {
                ExportColumn column = columnList.get(i);
                String title = ObjUtils.ifNull(column.getTitle(), "");
                Cell cell = rowTitle.createCell(i);
                cell.setCellStyle(ObjUtils.ifNull(column.getTitleStyle(), sheetConfig.getTitleStyle()));
                cell.setCellValue(createHelper.createRichTextString(title));
                // 设置批注
                if (ObjUtils.isNotEmpty(column.getTitleComment())) {
                    // 创建备注锚点
                    ClientAnchor anchor = createHelper.createClientAnchor();
                    // 设备锚点位置，解决 multiple cell comments in one cell are not allowed 问题
                    anchor.setRow1(cell.getRowIndex());
                    anchor.setCol1(cell.getColumnIndex());
                    anchor.setRow2(cell.getRowIndex() + 4);
                    anchor.setCol2(cell.getColumnIndex() + 3);
                    // 创建备注
                    Comment comment = sheet.createDrawingPatriarch().createCellComment(anchor);
                    comment.setString(createHelper.createRichTextString(column.getTitleComment()));
                    cell.setCellComment(comment);
                }
                // 设置隐藏列
                sheet.setColumnHidden(i, column.isHidden());
            }
            startRow = 1;
        }
        // 设置内容
        if (CollectionUtils.isNotEmpty(sheetConfig.getDataList())) {
            int rowNum = sheetConfig.getDataList().size();
            for (int i = 0; i < rowNum; i++) {
                Row row = sheet.createRow(startRow + i);
                logger.info("写入第{}/{}条数据", i + 1, rowNum);
                Object data = sheetConfig.getDataList().get(i);
                // 设置数据行高
                if (sheetConfig.getDataHeight() != null
                        && sheetConfig.getDataHeight() >= 0
                        && sheetConfig.getDataHeight() <= 409) {
                    row.setHeight((short) (sheetConfig.getDataHeight() * 20));
                }
                for (int j = 0; j < columnNum; j++) {
                    ExportColumn column = columnList.get(j);
                    Cell cell = row.createCell(j);
                    // 数据值和样式
                    Object dataValue = null;
                    CellStyle dataStyle = null;
                    try {
                        dataValue = PropertyUtils.getProperty(data, column.getProp());
                        Object cellStyle = PropertyUtils.getProperty(data, "cellStyle");
                        if (cellStyle != null && cellStyle instanceof CellStyle) {
                            dataStyle = (CellStyle) cellStyle;
                        }
                    } catch (Exception e) {
                        // 忽略获取数据异常
                    }
                    String nullValue = ObjUtils.ifNull(column.getIfNull(), sheetConfig.getIfNull());
                    // 设置样式
                    setCellStyle(dataFormat, sheetConfig, column, cell, row.getRowNum() + 1, dataStyle);
                    // 设置下拉框
                    if (i == 0
                            && column.getType() == ExcelDataType.EnumValue
                            && ArrayUtils.isNotEmpty(column.getValueArray())) {
                        DataValidationConstraint dvConstraint = dvHelper.createExplicitListConstraint(column.getValueArray());
                        CellRangeAddressList addressList = new CellRangeAddressList(startRow, rowNum + 1, j, j);
                        DataValidation validation = dvHelper.createValidation(dvConstraint, addressList);
                        validation.setSuppressDropDownArrow(true);
                        validation.setShowErrorBox(true);
                        sheet.addValidationData(validation);
                    }
                    if (dataValue == null) {
                        if (nullValue != null) {
                            // 设置为空时的显示值
                            cell.setCellValue(nullValue);
                        }
                        continue;
                    }
                    // 设置不同类型的值
                    if (column.getType() == ExcelDataType.EnumValue && dataValue instanceof IExcelEnum) {
                        String value = ((IExcelEnum) dataValue).getValue();
                        cell.setCellValue(ObjUtils.ifNull(value, ""));
                    } else if (column.getType() == ExcelDataType.IntegerValue
                            || column.getType() == ExcelDataType.LongValue) {
                        BigDecimal value = ObjUtils.toBigDecimal(dataValue, BigDecimal.ZERO).setScale(0);
                        if (value.toString().length() <= 12) {
                            cell.setCellValue(value.doubleValue());
                        } else {
                            cell.setCellValue(value.toString());
                        }
                    } else if (column.getType() == ExcelDataType.BigDecimalValue) {
                        BigDecimal value = ObjUtils.toBigDecimal(dataValue, BigDecimal.ZERO).setScale(column.getScale(), BigDecimal.ROUND_HALF_UP);
                        cell.setCellValue(value.doubleValue());
                    } else if (column.getType() == ExcelDataType.DateValue) {
                        cell.setCellValue(DateUtils.format((Date) (dataValue)));
                    } else {
                        cell.setCellValue(ObjUtils.toString(dataValue, ""));
                    }
                }
            }
        }
        // 最后设置自动列宽
        for (int i = 0; i < columnNum; i++) {
            ExportColumn column = columnList.get(i);
            if (column.getWidth() != null && column.getWidth() > 0 && column.getWidth() <= 255) {
                sheet.setColumnWidth(i, column.getWidth() * 256 + 200);
            } else {
                sheet.autoSizeColumn(i);
            }
        }
    }

    /**
     * 不能对每个单元格都复制一个样式出来设置
     * 如果列没有设置过样式，复制行样式保存到列样式上，
     * 这样一列最多只有几个样式
     * excel对样式上限有限制（64000），数据量大时会导致文件打不开
     *
     * @param dataFormat
     * @param sheetConfig
     * @param column
     * @param cell
     * @param rowNum
     * @param dataStyle
     */
    private static void setCellStyle(DataFormat dataFormat,
                                     ExportSheet sheetConfig,
                                     ExportColumn column,
                                     Cell cell,
                                     int rowNum,
                                     CellStyle dataStyle) {
        // 是否偶数行
        boolean even = rowNum % 2 == 0;
        // 使用的样式类型（1：数据样式，2：行样式，3：偶数行样式，4：奇数行样式）
        int styleType = 2;
        // 单元格样式，优先使用数据样式，其次列样式，再其次行样式
        CellStyle cellStyle;
        if (column.getDataStyle(rowNum) == null) {
            if (dataStyle == null) {
                if (column.getRowStyle() == null) {
                    CellStyle rowStyle = ObjUtils.ifNull(column.getRowStyle(), sheetConfig.getRowStyle());
                    if (rowStyle == null) {
                        if (even) {
                            // 偶数行样式
                            styleType = 3;
                            if (column.getEvenRowStyle() == null) {
                                CellStyle evenStyle = ObjUtils.ifNull(column.getEvenRowStyle(), sheetConfig.getEvenRowStyle());
                                if (evenStyle == null) {
                                    cellStyle = cloneStyleFrom(cell, cell.getCellStyle());
                                } else {
                                    cellStyle = cloneStyleFrom(cell, evenStyle);
                                }
                            } else {
                                cellStyle = column.getEvenRowStyle();
                            }
                        } else {
                            // 奇数行样式
                            styleType = 4;
                            if (column.getOddRowStyle() == null) {
                                CellStyle oddStyle = ObjUtils.ifNull(column.getOddRowStyle(), sheetConfig.getOddRowStyle());
                                if (oddStyle == null) {
                                    cellStyle = cloneStyleFrom(cell, cell.getCellStyle());
                                } else {
                                    cellStyle = cloneStyleFrom(cell, oddStyle);
                                }
                            } else {
                                cellStyle = column.getOddRowStyle();
                            }
                        }
                    } else {
                        styleType = 2;
                        cellStyle = cloneStyleFrom(cell, rowStyle);
                    }
                } else {
                    styleType = 2;
                    cellStyle = column.getRowStyle();
                }
            } else {
                styleType = 1;
                cellStyle = cloneStyleFrom(cell, dataStyle);
            }
        } else {
            styleType = 1;
            cellStyle = column.getDataStyle(rowNum);
        }
        // 设置特殊样式
        if (column.getType() == ExcelDataType.IntegerValue
                || column.getType() == ExcelDataType.LongValue) {
            // 数值类型默认居右
            cellStyle.setAlignment(HorizontalAlignment.RIGHT);
            cellStyle.setDataFormat(dataFormat.getFormat("#0"));
        } else if (column.getType() == ExcelDataType.BigDecimalValue) {
            // 数值类型默认居右
            cellStyle.setAlignment(HorizontalAlignment.RIGHT);
            String format = "#,##0.00";
            if (column.getScale() > 0) {
                format = "#,##0." + StringUtils.repeat("0", column.getScale());
            }
            cellStyle.setDataFormat(dataFormat.getFormat(format));
        } else if (column.getType() == ExcelDataType.StringValue) {
            // 文本格式
            cellStyle.setDataFormat(dataFormat.getFormat("@"));
        }
        // 保存列样式（1：数据样式，2：行样式，3：偶数行样式，4：奇数行样式）
        switch (styleType) {
            case 1:
                column.setDataStyle(rowNum, cellStyle);
                break;
            case 2:
                column.setRowStyle(cellStyle);
                break;
            case 3:
                column.setEvenRowStyle(cellStyle);
                break;
            case 4:
                column.setOddRowStyle(cellStyle);
                break;
            default:
                column.setRowStyle(cellStyle);
                break;
        }
        // 设置单元格样式
        cell.setCellStyle(cellStyle);
    }

    /**
     * 样式复制
     *
     * @param cell
     * @param src
     * @return
     */
    private static CellStyle cloneStyleFrom(Cell cell, CellStyle src) {
        if (cell == null || src == null) {
            return null;
        }
        CellStyle cellStyle = cell.getSheet().getWorkbook().createCellStyle();
        cellStyle.cloneStyleFrom(src);
        return cellStyle;
    }

    /**
     * 从文件读取excel
     *
     * @param file
     * @param sheetConfig
     * @throws Exception
     * @deprecated 读取大文件可能会产生OOM异常，建议使用 readBigXlsxExcel 方法
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
     * 从文件读取excel
     *
     * @param file
     * @param sheetConfig
     * @throws Exception
     */
    public static void readBigXlsxExcel(File file, ImportSheet... sheetConfig) throws Exception {
        if (file == null || ArrayUtils.isEmpty(sheetConfig)) {
            throw new IllegalArgumentException("excel导入参数错误！");
        }
        Map<Integer, ImportSheet> sheetMap = Maps.newHashMap();
        for (int i = 0; i < sheetConfig.length; i++) {
            if (sheetConfig[i].getIndex() == null) {
                sheetConfig[i].setIndex(i);
            }
            sheetMap.put(sheetConfig[i].getIndex(), sheetConfig[i]);
        }
        try (OPCPackage pkg = OPCPackage.open(file, PackageAccess.READ)) {
            ReadOnlySharedStringsTable strings = new ReadOnlySharedStringsTable(pkg);
            XSSFReader reader = new XSSFReader(pkg);
            StylesTable styles = reader.getStylesTable();
            XSSFReader.SheetIterator iter = (XSSFReader.SheetIterator) reader.getSheetsData();
            Integer sheetIndex = 0;
            while (iter.hasNext()) {
                try (InputStream stream = iter.next()) {
                    ImportSheet importSheet = sheetMap.get(sheetIndex);
                    if (importSheet != null) {
                        InputSource source = new InputSource(stream);
                        XMLReader parser = XMLHelper.newXMLReader();
                        ContentHandler handler = new XSSFSheetXMLHandler(
                                styles,
                                strings,
                                new ExcelContentHandler(importSheet),
                                new DataFormatter(),
                                false);
                        parser.setContentHandler(handler);
                        parser.parse(source);
                    }
                    sheetIndex++;
                }
            }
        }
    }

    /**
     * 从输入流读取excel
     *
     * @param sheetConfig
     * @return
     * @deprecated 读取大文件可能会产生OOM异常，建议使用 readBigXlsxExcel 方法
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
     * 从输入流读取excel
     *
     * @param sheetConfig
     * @return
     */
    public static void readBigXlsxExcel(InputStream inputStream, ImportSheet... sheetConfig) throws Exception {
        if (inputStream == null || ArrayUtils.isEmpty(sheetConfig)) {
            throw new IllegalArgumentException("excel导入参数错误！");
        }
        Map<Integer, ImportSheet> sheetMap = Maps.newHashMap();
        for (int i = 0; i < sheetConfig.length; i++) {
            if (sheetConfig[i].getIndex() == null) {
                sheetConfig[i].setIndex(i);
            }
            sheetMap.put(sheetConfig[i].getIndex(), sheetConfig[i]);
        }
        // 解决 Zip bomb detected 问题
        ZipSecureFile.setMinInflateRatio(-1.0d);
        // 解析文件
        try (OPCPackage pkg = OPCPackage.open(inputStream)) {
            ReadOnlySharedStringsTable strings = new ReadOnlySharedStringsTable(pkg);
            XSSFReader reader = new XSSFReader(pkg);
            StylesTable styles = reader.getStylesTable();
            XSSFReader.SheetIterator iter = (XSSFReader.SheetIterator) reader.getSheetsData();
            Integer sheetIndex = 0;
            while (iter.hasNext()) {
                try (InputStream stream = iter.next()) {
                    InputSource source = new InputSource(stream);
                    XMLReader parser = XMLHelper.newXMLReader();
                    ContentHandler handler = new XSSFSheetXMLHandler(
                            styles,
                            strings,
                            new ExcelContentHandler(sheetMap.get(sheetIndex)),
                            new DataFormatter(),
                            false);
                    parser.setContentHandler(handler);
                    parser.parse(source);
                    sheetIndex++;
                }
            }
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
                setPropList.add(setProperty(column, cell, data, formulaEvaluator));
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
     * @deprecated 建议使用新版xlsx文件
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
     * @deprecated 建议使用新版xlsx文件
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
                setPropList.add(setProperty(column, cell, data, formulaEvaluator));
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
                if (cellValue instanceof Double) {
                    // 数字类型转字符串可能出现精度问题，需要特殊处理
                    NumberFormat numberFormat = NumberFormat.getInstance();
                    numberFormat.setGroupingUsed(false);
                    cellValue = numberFormat.format(cellValue);
                }
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
        XSSFWorkbook workbook = newXlsxWorkbook();
//        SXSSFWorkbook workbook = newBigXlsxWorkbook();
        List<Map<String, Object>> resultList = Lists.newArrayList();
        resultList.add(new ImmutableMap.Builder<String, Object>()
                .put("col1", "row1")
                .put("col2", "男")
                .put("col3", 9999999999999998L)
                .put("col4", 123456.654321)
                .put("col5", 123456)
                .put("col6", true)
                .put("col7", new Date())
                .put("col8", "好长好长的值哈哈哈，好长好长的值哈哈哈")
                .build());
        resultList.add(new ImmutableMap.Builder<String, Object>()
                .put("col1", "row2")
                .put("col2", "女")
                .put("col3", 999999999999999L)
                .put("col5", -123456.123456)
                .put("col6", false)
                .put("col7", new Date())
                .put("col8", "123")
                .build());
        resultList.add(new ImmutableMap.Builder<String, Object>()
                .put("col1", "row1")
                .put("col2", "男")
                .put("col3", 9999999999999998L)
                .put("col4", 123456.654321)
                .put("col5", 123456)
                .put("col6", true)
                .put("col7", new Date())
                .put("col8", "123")
                .build());
        resultList.add(new ImmutableMap.Builder<String, Object>()
                .put("col1", "row2")
                .put("col2", "女")
                .put("col3", 999999999999999L)
                .put("col4", 34.6)
                .put("col5", -123456.123456)
                .put("col6", false)
                .put("col7", new Date())
                .put("col8", "123")
                .put("cellStyle", workbook.createCellStyle())
                .build());
        List<ExportColumn> columnList = Lists.newArrayList(
                new ExportColumn("测试列1", "col1", ExcelDataType.StringValue, true)
                        .setTitleComment("第一列批注"),
                new ExportColumn("测试列2", "col2", ExcelDataType.EnumValue, SexEnum.getValues())
                        .setTitleComment("第二列批注"),
                new ExportColumn("测试列3", "col3", ExcelDataType.LongValue)
                        .setTitleComment("第三列批注\n第三列批注，第三列批注，第三列批注，第三列批注，第三列批注，第三列批注")
                        .setWidth(30),
                new ExportColumn("测试列4测试列4测试列4测试列4测试列4测试列4", "col4", ExcelDataType.BigDecimalValue, 3)
                        .setTitleComment("第四例批注")
                        .setIfNull("-"),
                new ExportColumn("测试列5", "col5", ExcelDataType.BigDecimalValue, 5),
                new ExportColumn("测试列6", "col6", ExcelDataType.BooleanValue),
                new ExportColumn("测试列7", "col7", ExcelDataType.DateValue),
                new ExportColumn("测试列8", "col8", ExcelDataType.StringValue)
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
                .setTitleHeight(60)
                .setDataHeight(30)
                .setOddRowStyle(oddStyle)
                .setEvenRowStyle(evenStyle);
        try {
            writeXlsxSheet(workbook, exportSheet);
            writeXlsxSheet(workbook, exportSheet.setAppend(true));
//            writeBigXlsxWorkbook(workbook, exportSheet);
            workbook.write(new FileOutputStream(new File("/Users/zhuminghua/Desktop/test.xlsx")));
        } finally {
//            workbook.dispose();
        }
    }

    /**
     * 读例子
     *
     * @throws Exception
     */
    private static void readExample() throws Exception {
        File file = new File("/Users/zhuminghua/Desktop/company_employee_202101251755.xlsx");
        List<ImportColumn> columnList = Lists.newArrayList(
                new ImportColumn("员工姓名", "t1", ExcelDataType.StringValue),
                new ImportColumn("员工手机号", "t2", ExcelDataType.StringValue),
                new ImportColumn("员工身份证号", "t3", ExcelDataType.StringValue),
                new ImportColumn("直属部门", "t4", ExcelDataType.StringValue)
        );
        ImportSheet importSheet = new ImportSheet(columnList, LinkedHashMap.class);
        readBigXlsxExcel(file, importSheet);
        System.out.println(JSON.toJSONString(importSheet.getDataList()));
        System.out.println("end");
    }

}
