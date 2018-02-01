package com.luastar.swift.tools.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class ColAndRsVO extends PropBaseVO {

    private static final Logger logger = LoggerFactory.getLogger(ColAndRsVO.class);

    protected List<ColumnVO> colList;
    protected List<Map<String, Object>> valueList;

    public List<ColumnVO> getColList() {
        return colList;
    }

    public void setColList(List<ColumnVO> colList) {
        this.colList = colList;
    }

    public List<Map<String, Object>> getValueList() {
        return valueList;
    }

    public void setValueList(List<Map<String, Object>> valueList) {
        this.valueList = valueList;
    }

}
