package com.luastar.swift.base.excel;

import com.google.common.collect.Maps;
import org.apache.poi.ss.usermodel.CellStyle;

import java.util.Map;

/**
 * excel导出列定义
 */
public class ExportColumn {

    /**
     * 标题
     */
    private String title;
    /**
     * 标题批注
     */
    private String titleComment;
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
    private int scale = 2;
    /**
     * 类型值
     */
    private String[] valueArray;
    /**
     * 自动计算宽度
     */
    private boolean autoWidth;
    /**
     * 是否隐藏列
     */
    private boolean hidden = false;
    /**
     * 宽度（1~255），如果没有设置，则 autoWidth = true
     */
    private Integer width;
    /**
     * 为null时显示值
     */
    private String ifNull;
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
    /**
     * 数据行样式
     */
    private Map<Integer, CellStyle> dataStyleMap;

    public ExportColumn(String title, String prop, ExcelDataType type) {
        this.title = title;
        this.prop = prop;
        this.type = type;
    }

    public ExportColumn(String title, String prop, ExcelDataType type, boolean hidden) {
        this.title = title;
        this.prop = prop;
        this.type = type;
        this.hidden = hidden;
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

    public ExportColumn setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getTitleComment() {
        return titleComment;
    }

    public ExportColumn setTitleComment(String titleComment) {
        this.titleComment = titleComment;
        return this;
    }

    public String getProp() {
        return prop;
    }

    public ExportColumn setProp(String prop) {
        this.prop = prop;
        return this;
    }

    public ExcelDataType getType() {
        return type;
    }

    public ExportColumn setType(ExcelDataType type) {
        this.type = type;
        return this;
    }

    public int getScale() {
        return scale;
    }

    public ExportColumn setScale(int scale) {
        this.scale = scale;
        return this;
    }

    public String[] getValueArray() {
        return valueArray;
    }

    public ExportColumn setValueArray(String[] valueArray) {
        this.valueArray = valueArray;
        return this;
    }

    public boolean isAutoWidth() {
        return autoWidth;
    }

    public ExportColumn setAutoWidth(boolean autoWidth) {
        this.autoWidth = autoWidth;
        return this;
    }

    public boolean isHidden() {
        return hidden;
    }

    public ExportColumn setHidden(boolean hidden) {
        this.hidden = hidden;
        return this;
    }

    public Integer getWidth() {
        return width;
    }

    public ExportColumn setWidth(Integer width) {
        this.width = width;
        return this;
    }

    public String getIfNull() {
        return ifNull;
    }

    public ExportColumn setIfNull(String ifNull) {
        this.ifNull = ifNull;
        return this;
    }

    public CellStyle getTitleStyle() {
        return titleStyle;
    }

    public ExportColumn setTitleStyle(CellStyle titleStyle) {
        this.titleStyle = titleStyle;
        return this;
    }

    public CellStyle getRowStyle() {
        return rowStyle;
    }

    public ExportColumn setRowStyle(CellStyle rowStyle) {
        this.rowStyle = rowStyle;
        return this;
    }

    public CellStyle getOddRowStyle() {
        return oddRowStyle;
    }

    public ExportColumn setOddRowStyle(CellStyle oddRowStyle) {
        this.oddRowStyle = oddRowStyle;
        return this;
    }

    public CellStyle getEvenRowStyle() {
        return evenRowStyle;
    }

    public ExportColumn setEvenRowStyle(CellStyle evenRowStyle) {
        this.evenRowStyle = evenRowStyle;
        return this;
    }

    public synchronized void setDataStyle(int row, CellStyle dataStyle) {
        if (dataStyleMap == null) {
            dataStyleMap = Maps.newHashMap();
        }
        dataStyleMap.put(row, dataStyle);
    }

    public CellStyle getDataStyle(int row) {
        if (dataStyleMap == null) {
            return null;
        }
        return dataStyleMap.get(row);
    }

}
