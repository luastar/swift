package com.luastar.swift.base.excel;

/**
 * excel导出列定义
 */
public class ImportColumn {

    /**
     * 标题
     */
    private String title;
    /**
     * 属性
     */
    private String prop;
    /**
     * 类型
     */
    private ExcelDataType type;
    /**
     * 如果为枚举类型，需要设置从value获取枚举的静态方法
     */
    private String staticMethodName;
    /**
     * 所在列
     */
    private Integer columnIndex;

    public ImportColumn(String title, String prop, ExcelDataType type) {
        this.title = title;
        this.prop = prop;
        this.type = type;
    }

    public ImportColumn(String title, String prop, ExcelDataType type, String staticMethodName) {
        this.title = title;
        this.prop = prop;
        this.type = type;
        this.staticMethodName = staticMethodName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getProp() {
        return prop;
    }

    public void setProp(String prop) {
        this.prop = prop;
    }

    public ExcelDataType getType() {
        return type;
    }

    public void setType(ExcelDataType type) {
        this.type = type;
    }

    public String getStaticMethodName() {
        return staticMethodName;
    }

    public void setStaticMethodName(String staticMethodName) {
        this.staticMethodName = staticMethodName;
    }

    public Integer getColumnIndex() {
        return columnIndex;
    }

    public void setColumnIndex(Integer columnIndex) {
        this.columnIndex = columnIndex;
    }
    
}
