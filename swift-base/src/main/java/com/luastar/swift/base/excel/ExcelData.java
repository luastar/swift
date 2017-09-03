package com.luastar.swift.base.excel;

/**
 * excel读入数据
 */
public class ExcelData {

    /**
     * 数据行数
     */
    private int row;

    /**
     * 数据对象
     */
    private Object data;

    /**
     * 校验信息
     */
    private String checkMsg;

    public ExcelData(int row, Object data) {
        this.row = row;
        this.data = data;
    }

    public ExcelData(int row, Object data, String checkMsg) {
        this.row = row;
        this.data = data;
        this.checkMsg = checkMsg;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getCheckMsg() {
        return checkMsg;
    }

    public void setCheckMsg(String checkMsg) {
        this.checkMsg = checkMsg;
    }

}
