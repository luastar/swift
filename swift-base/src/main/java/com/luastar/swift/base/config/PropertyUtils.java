package com.luastar.swift.base.config;

import com.luastar.swift.base.utils.StrUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

public class PropertyUtils {

    private static final ItfConfig cfg = ConfigFactory.getConfig();

    private PropertyUtils() {
    }

    public static String getString(String key) {
        return cfg.getString(key);
    }

    public static String getString(String key, String defaultVal) {
        return cfg.getString(key, defaultVal);
    }

    public static String[] getStringArray(String key) {
        return cfg.getStringArray(key);
    }

    public static String[] getStringArray(String key, String sep) {
        return cfg.getStringArray(key, sep);
    }

    public static int getInt(String key) {
        return cfg.getInt(key, 0);
    }

    public static int getInt(String key, int defaultVal) {
        return cfg.getInt(key, defaultVal);
    }

    public static long getLong(String key) {
        return cfg.getLong(key, -1L);
    }

    public static long getLong(String key, long defaultVal) {
        return cfg.getLong(key, defaultVal);
    }

    public static double getDouble(String key) {
        return cfg.getDouble(key, -1D);
    }

    public static double getDouble(String key, double defaultVal) {
        return cfg.getDouble(key, defaultVal);
    }

    public static String getMessage(String key, Map<String, Object> args) {
        return StrUtils.formatString(cfg.getString(key), args);
    }

}
