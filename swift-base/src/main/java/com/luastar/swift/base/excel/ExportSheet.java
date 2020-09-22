package com.luastar.swift.base.excel;

import org.apache.poi.ss.usermodel.CellStyle;

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
     * 是否追加数据
     */
    private Boolean isAppend;
    /**
     * 标题行高
     */
    private Integer titleHeight;
    /**
     * 数据行高
     */
    private Integer dataHeight;
    /**
     * 标题样式
     */
    private CellStyle titleStyle;
    /**
     * 行样式
     */
    private CellStyle rowStyle;
    /**
     * 奇数行样式
     */
    private CellStyle oddRowStyle;
    /**
     * 偶数行样式
     */
    private CellStyle evenRowStyle;

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

    public Boolean getAppend() {
        return isAppend;
    }

    public ExportSheet setAppend(Boolean append) {
        isAppend = append;
        return this;
    }

    public Integer getTitleHeight() {
        return titleHeight;
    }

    public ExportSheet setTitleHeight(Integer titleHeight) {
        this.titleHeight = titleHeight;
        return this;
    }

    public Integer getDataHeight() {
        return dataHeight;
    }

    public ExportSheet setDataHeight(Integer dataHeight) {
        this.dataHeight = dataHeight;
        return this;
    }

    public CellStyle getTitleStyle() {
        return titleStyle;
    }

    public ExportSheet setTitleStyle(CellStyle titleStyle) {
        this.titleStyle = titleStyle;
        return this;
    }

    public CellStyle getRowStyle() {
        return rowStyle;
    }

    public ExportSheet setRowStyle(CellStyle rowStyle) {
        this.rowStyle = rowStyle;
        return this;
    }

    public CellStyle getOddRowStyle() {
        return oddRowStyle;
    }

    public ExportSheet setOddRowStyle(CellStyle oddRowStyle) {
        this.oddRowStyle = oddRowStyle;
        return this;
    }

    public CellStyle getEvenRowStyle() {
        return evenRowStyle;
    }

    public ExportSheet setEvenRowStyle(CellStyle evenRowStyle) {
        this.evenRowStyle = evenRowStyle;
        return this;
    }

}
