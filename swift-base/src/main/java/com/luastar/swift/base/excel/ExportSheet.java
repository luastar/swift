package com.luastar.swift.base.excel;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;

import java.util.List;

/**
 * excel sheet 配置
 */
public class ExportSheet {

    /**
     * sheet名称
     */
    private String name;
    /**
     * 列配置
     */
    private List<ExportColumn> columnList;
    /**
     * 数据
     */
    private List<?> dataList;
    /**
     * 为null时显示值
     */
    private String ifNull;
    /**
     * 标题样式
     */
    private XSSFCellStyle titleStyle;
    /**
     * 行样式
     */
    private XSSFCellStyle rowStyle;
    /**
     * 奇数行样式
     */
    private XSSFCellStyle oddRowStyle;
    /**
     * 偶数行样式
     */
    private XSSFCellStyle evenRowStyle;

    public ExportSheet(List<ExportColumn> columnList, List<?> dataList) {
        this.columnList = columnList;
        this.dataList = dataList;
    }

    public ExportSheet(String name, List<ExportColumn> columnList, List<?> dataList) {
        this.name = name;
        this.columnList = columnList;
        this.dataList = dataList;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ExportColumn> getColumnList() {
        return columnList;
    }

    public void setColumnList(List<ExportColumn> columnList) {
        this.columnList = columnList;
    }

    public List<?> getDataList() {
        return dataList;
    }

    public void setDataList(List<?> dataList) {
        this.dataList = dataList;
    }

    public String getIfNull() {
        return ifNull;
    }

    public ExportSheet setIfNull(String ifNull) {
        this.ifNull = ifNull;
        return this;
    }

    public CellStyle getTitleStyle() {
        return titleStyle;
    }

    public ExportSheet setTitleStyle(XSSFCellStyle titleStyle) {
        this.titleStyle = titleStyle;
        return this;
    }

    public XSSFCellStyle getRowStyle() {
        return rowStyle;
    }

    public ExportSheet setRowStyle(XSSFCellStyle rowStyle) {
        this.rowStyle = rowStyle;
        return this;
    }

    public XSSFCellStyle getOddRowStyle() {
        return oddRowStyle;
    }

    public ExportSheet setOddRowStyle(XSSFCellStyle oddRowStyle) {
        this.oddRowStyle = oddRowStyle;
        return this;
    }

    public XSSFCellStyle getEvenRowStyle() {
        return evenRowStyle;
    }

    public ExportSheet setEvenRowStyle(XSSFCellStyle evenRowStyle) {
        this.evenRowStyle = evenRowStyle;
        return this;
    }

}
