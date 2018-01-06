package com.luastar.swift.base.entity;

import com.luastar.swift.base.utils.ObjUtils;

import java.math.BigDecimal;
import java.util.LinkedHashMap;

/**
 * 增加一些常用获取值方法
 */
public class SwiftLinkedHashMap<K, V> extends LinkedHashMap<K,V> {

    public String getString(K key) {
        return ObjUtils.toString(get(key));
    }

    public String getString(K key, String defaultValue) {
        return ObjUtils.toString(get(key), defaultValue);
    }

    public Integer getInteger(K key) {
        return ObjUtils.toInteger(get(key));
    }

    public Integer getInteger(K key, Integer defaultValue) {
        return ObjUtils.toInteger(get(key), defaultValue);
    }

    public Long getLong(K key) {
        return ObjUtils.toLong(get(key));
    }

    public Long getLong(K key, Long defaultValue) {
        return ObjUtils.toLong(get(key), defaultValue);
    }

    public BigDecimal getBigDecimal(K key) {
        return ObjUtils.toBigDecimal(get(key));
    }

    public BigDecimal getBigDecimal(K key, BigDecimal defaultValue) {
        return ObjUtils.toBigDecimal(get(key), defaultValue);
    }

    public Boolean getBoolean(K key) {
        return ObjUtils.toBoolean(get(key));
    }

    public Boolean getBoolean(K key, Boolean defaultValue) {
        return ObjUtils.toBoolean(get(key), defaultValue);
    }

}
