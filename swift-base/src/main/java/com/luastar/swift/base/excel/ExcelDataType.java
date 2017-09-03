package com.luastar.swift.base.excel;

/**
 * excel列数据类型
 */
public enum ExcelDataType {

    StringValue(10, "String"),
    LongValue(20, "Long"),
    IntegerValue(30, "Int"),
    BigDecimalValue(40, "BigDecimal"),
    BooleanValue(50, "Boolean"),
    DateValue(60, "Date"),
    EnumValue(90, "enum");

    private int key;
    private String value;

    ExcelDataType(int key, String value) {
        this.key = key;
        this.value = value;
    }

    public static ExcelDataType valueOf(int key) {
        for (ExcelDataType type : values()) {
            if (type.getKey() == key) {
                return type;
            }
        }
        return null;
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
