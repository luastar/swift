package com.luastar.swift.base.config;

import com.luastar.swift.base.utils.ClassLoaderUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.regex.Matcher;

public class ConfigImpl implements ItfConfig {

    private static final Logger logger = LoggerFactory.getLogger(ConfigImpl.class);

    protected Properties properties;

    public ConfigImpl(String[] propFiles) {
        properties = ClassLoaderUtils.getProperties(propFiles);
    }

    public boolean containsKey(String key) {
        return properties.containsKey(key);
    }

    public String getString(String key) {
        return getString(key, StringUtils.EMPTY);
    }

    public String getString(String key, String defaultValue) {
        String value = getAndProcessValue(key);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    private String getAndProcessValue(String key) {
        String value = properties.getProperty(key);
        if (StringUtils.isBlank(value)) {
            return null;
        }
        StringBuffer result = new StringBuffer();
        Matcher matcher = ItfConfig.VALUE_RESOLVER_PATTERN.matcher(value);
        while (matcher.find()) {
            String resolveKey = StringUtils.substringBetween(matcher.group(), "${", "}");
            String resolveValue = properties.getProperty(resolveKey);
            if (StringUtils.isNotBlank(resolveValue)) {
                matcher.appendReplacement(result, resolveValue);
            }
        }
        matcher.appendTail(result);
        return result.toString();
    }

    public String[] getStringArray(String key) {
        return getStringArray(key, null);
    }

    public String[] getStringArray(String key, String sep) {
        String value = getAndProcessValue(key);
        if (value == null) {
            return null;
        }
        return value.split(sep == null ? "," : sep);
    }

    public int getInt(String key, int defaultValue) {
        String value = getAndProcessValue(key);
        if (value == null) {
            return defaultValue;
        }
        return NumberUtils.toInt(value, defaultValue);
    }

    public long getLong(String key, long defaultValue) {
        String value = getAndProcessValue(key);
        if (value == null) {
            return defaultValue;
        }
        return NumberUtils.toLong(value, defaultValue);
    }

    public float getFloat(String key, float defaultValue) {
        String value = getAndProcessValue(key);
        if (value == null) {
            return defaultValue;
        }
        return NumberUtils.toFloat(value, defaultValue);
    }

    public double getDouble(String key, double defaultValue) {
        String value = getAndProcessValue(key);
        if (value == null) {
            return defaultValue;
        }
        return NumberUtils.toDouble(value, defaultValue);
    }

}
