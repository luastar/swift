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

    public CellStyle getTitleStyle() {
        return titleStyle;
    }

    public void setTitleStyle(CellStyle titleStyle) {
        this.titleStyle = titleStyle;
    }

    public CellStyle getRowStyle() {
        return rowStyle;
    }

    public void setRowStyle(CellStyle rowStyle) {
        this.rowStyle = rowStyle;
    }

    public CellStyle getOddRowStyle() {
        return oddRowStyle;
    }

    public void setOddRowStyle(CellStyle oddRowStyle) {
        this.oddRowStyle = oddRowStyle;
    }

    public CellStyle getEvenRowStyle() {
        return evenRowStyle;
    }

    public void setEvenRowStyle(CellStyle evenRowStyle) {
        this.evenRowStyle = evenRowStyle;
    }

}
