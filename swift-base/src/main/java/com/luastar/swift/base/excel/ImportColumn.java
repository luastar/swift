package com.luastar.swift.base.excel;

/**
 * excel导入列定义
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
     * 如果为枚举类型，需要设置从value获取枚举的类
     */
    private Class staticClass;
    /**
     * 如果为枚举类型，需要设置从value获取枚举的静态方法
     */
    private String staticMethodName;
    /**
     * 所在列，不用设置，会自动按标题查找后设置
     */
    private Integer columnIndex;

    public ImportColumn(String title, String prop, ExcelDataType type) {
        this.title = title;
        this.prop = prop;
        this.type = type;
    }

    public ImportColumn(String title, String prop, ExcelDataType type, Class staticClass, String staticMethodName) {
        this.title = title;
        this.prop = prop;
        this.type = type;
        this.staticClass = staticClass;
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

    public Class getStaticClass() {
        return staticClass;
    }

    public void setStaticClass(Class staticClass) {
        this.staticClass = staticClass;
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
