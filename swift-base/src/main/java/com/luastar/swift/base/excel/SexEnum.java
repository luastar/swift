package com.luastar.swift.base.excel;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 测试枚举
 */
public enum SexEnum {

    male(1, "男"),
    female(2, "女");

    public static SexEnum getByKey(Integer key) {
        if (key == null) {
            return null;
        }
        for (SexEnum item : values()) {
            if (item.getKey().equals(key)) {
                return item;
            }
        }
        return null;
    }

    public static SexEnum getByValue(String value) {
        if (StringUtils.isEmpty(value)) {
            return null;
        }
        for (SexEnum item : values()) {
            if (item.getValue().equals(value)) {
                return item;
            }
        }
        return null;
    }

    public static String[] getValues() {
        return Arrays.stream(values())
                .map(item -> item.getValue())
                .collect(Collectors.toList())
                .toArray(new String[values().length]);
    }

    private Integer key;
    private String value;

    SexEnum(Integer key, String value) {
        this.key = key;
        this.value = value;
    }

    public Integer getKey() {
        return key;
    }

    public void setKey(Integer key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
