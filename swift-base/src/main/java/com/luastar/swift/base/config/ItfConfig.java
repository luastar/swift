package com.luastar.swift.base.config;

import java.util.Map;
import java.util.regex.Pattern;

public interface ItfConfig {

    Pattern VALUE_RESOLVER_PATTERN = Pattern.compile("\\$\\{\\w+\\}", Pattern.CASE_INSENSITIVE);

    String getString(String key);

    String getString(String key, String defaultValue);

    String[] getStringArray(String key);

    String[] getStringArray(String key, String sep);

    int getInt(String key, int defaultValue);

    long getLong(String key, long defaultValue);

    float getFloat(String key, float defaultValue);

    double getDouble(String key, double defaultValue);

    boolean containsKey(String key);

    void putAll(Map<?, ?> propMap);


}
