package com.luastar.swift.base.excel;

import java.util.List;

/**
 * excel sheet 配置
 */
public class ImportSheet {

    /**
     * sheet index
     */
    private Integer index = 0;
    /**
     * 列配置
     */
    private List<ImportColumn> columnList;
    /**
     * 数据类
     */
    private Class<?> dataClass;
    /**
     * 数据
     */
    private List<ExcelData> dataList;

    public ImportSheet(List<ImportColumn> columnList, Class<?> dataClass) {
        this.columnList = columnList;
        this.dataClass = dataClass;
    }

    public ImportSheet(Integer index, List<ImportColumn> columnList, Class<?> dataClass) {
        this.index = index;
        this.columnList = columnList;
        this.dataClass = dataClass;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public Class<?> getDataClass() {
        return dataClass;
    }

    public void setDataClass(Class<?> dataClass) {
        this.dataClass = dataClass;
    }

    public List<ImportColumn> getColumnList() {
        return columnList;
    }

    public void setColumnList(List<ImportColumn> columnList) {
        this.columnList = columnList;
    }

    public List<ExcelData> getDataList() {
        return dataList;
    }

    public void setDataList(List<ExcelData> dataList) {
        this.dataList = dataList;
    }

}
