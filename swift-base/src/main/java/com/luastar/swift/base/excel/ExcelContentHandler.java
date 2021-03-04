package com.luastar.swift.base.excel;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.luastar.swift.base.utils.DateUtils;
import com.luastar.swift.base.utils.ObjUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class ExcelContentHandler implements XSSFSheetXMLHandler.SheetContentsHandler {

    private static final Logger logger = LoggerFactory.getLogger(ExcelContentHandler.class);

    /**
     * 读取配置
     */
    private ImportSheet importSheet;
    /**
     * 列映射
     */
    private Map<Integer, ImportColumn> columnMap = Maps.newHashMap();
    /**
     * 当前行
     */
    private int currentRow = -1;
    /**
     * 当前列
     */
    private int currentCol = -1;

    /**
     * 当前行数据
     */
    private Object currentRowData = null;
    /**
     * 当前行错误信息
     */
    private String currentRowMsg = null;

    public ExcelContentHandler(ImportSheet importSheet) {
        if (importSheet == null) {
            throw new IllegalArgumentException("importSheet must be not null");
        }
        this.importSheet = importSheet;
    }

    @Override
    public void startRow(int rowNum) {
        currentRow = rowNum;
        currentCol = -1;
        if (currentRow > 0) {
            try {
                currentRowData = importSheet.getDataClass().newInstance();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public void endRow(int rowNum) {
        if (rowNum > 0) {
            ExcelData data = new ExcelData(currentRow, currentRowData, currentRowMsg);
            logger.info(JSON.toJSONString(data));
            if (importSheet.getDataList().size() <= importSheet.getDataLimit()) {
                importSheet.addData(data);
            } else {
                logger.info("超过读取数量【{}】限制，丢弃", importSheet.getDataLimit());
            }
        }
    }

    @Override
    public void cell(String cellReference, String formattedValue, XSSFComment xssfComment) {
        if (cellReference == null) {
            cellReference = new CellAddress(currentRow, currentCol).formatAsString();
        }
        currentCol = (new CellReference(cellReference)).getCol();
        if (currentRow == 0) {
            List<ImportColumn> columnList = importSheet.getColumnList();
            for (ImportColumn column : columnList) {
                if (column.getTitle().equals(formattedValue)) {
                    columnMap.put(currentCol, column);
                }
            }
        } else if (currentRow > 0) {
            ImportColumn column = columnMap.get(currentCol);
            if (column != null) {
                try {
                    switch (column.getType()) {
                        case LongValue:
                            PropertyUtils.setProperty(currentRowData, column.getProp(), ObjUtils.toLong(formattedValue));
                            break;
                        case IntegerValue:
                            PropertyUtils.setProperty(currentRowData, column.getProp(), ObjUtils.toInteger(formattedValue));
                            break;
                        case BigDecimalValue:
                            PropertyUtils.setProperty(currentRowData, column.getProp(), ObjUtils.toBigDecimal(formattedValue));
                            break;
                        case BooleanValue:
                            PropertyUtils.setProperty(currentRowData, column.getProp(), ObjUtils.toBoolean(formattedValue));
                            break;
                        case DateValue:
                            PropertyUtils.setProperty(currentRowData, column.getProp(), DateUtils.parse(formattedValue));
                            break;
                        case EnumValue:
                            if (column.getStaticClass() == null || StringUtils.isEmpty(column.getStaticMethodName())) {
                                logger.info("列【{}】没有设置获取枚举的静态类和方法。", column.getTitle());
                                break;
                            }
                            Object value = MethodUtils.invokeStaticMethod(column.getStaticClass(), column.getStaticMethodName(), formattedValue);
                            if (value == null) {
                                logger.info("列【{}】获取不到枚举值。", column.getTitle(), column.getProp(), formattedValue);
                                break;
                            }
                            PropertyUtils.setProperty(currentRowData, column.getProp(), value);
                            break;
                        default:
                            PropertyUtils.setProperty(currentRowData, column.getProp(), formattedValue);
                            break;
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    currentRowMsg = column.getTitle() + "赋值失败，原因为：" + e.getMessage();
                }
            }
        }
    }

    @Override
    public void headerFooter(String text, boolean isHeader, String tagName) {

    }

}
