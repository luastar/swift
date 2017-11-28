package com.luastar.swift.base.excel;

import org.apache.poi.ss.usermodel.CellStyle;

/**
 * excel导出列定义
 */
public class ExportColumn {

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
     * 小数点位数，主要针对BigDecimalValue类型
     */
    private int scale;
    /**
     * 类型值
     */
    private String[] valueArray;
    /**
     * 自动计算宽度
     */
    private boolean autoWidth;
    /**
     * 宽度（1~255）
     */
    private Integer width;
    /**
     * 标题样式
     */
    private CellStyle titleStyle;
    /**
     * 行样式
     */
    private CellStyle rowStyle;

    public ExportColumn(String title, String prop, ExcelDataType type) {
        this(title, prop, type, 2);
    }

    public ExportColumn(String title, String prop, ExcelDataType type, int scale) {
        this.title = title;
        this.prop = prop;
        this.type = type;
        this.scale = scale;
    }

    public ExportColumn(String title, String prop, ExcelDataType type, String[] valueArray) {
        this.title = title;
        this.prop = prop;
        this.type = type;
        this.valueArray = valueArray;
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

    public int getScale() {
        return scale;
    }

    public void setScale(int scale) {
        this.scale = scale;
    }

    public String[] getValueArray() {
        return valueArray;
    }

    public void setValueArray(String[] valueArray) {
        this.valueArray = valueArray;
    }

    public boolean isAutoWidth() {
        return autoWidth;
    }

    public void setAutoWidth(boolean autoWidth) {
        this.autoWidth = autoWidth;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
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

}
