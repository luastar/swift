package com.luastar.swift.base.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ObjUtils {

    private static final String REGEXP_FORMAT_STRING = "(\\{\\d\\})";
    private static final Pattern pattern = Pattern.compile(REGEXP_FORMAT_STRING, Pattern.CASE_INSENSITIVE);

    public static <T> T ifNull(T object, T defaultValue) {
        return object != null ? object : defaultValue;
    }

    public static String toString(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof String) {
            return (String) obj;
        }
        return obj.toString();
    }

    public static String toString(Object obj, String defaultValue) {
        if (obj == null) {
            return defaultValue;
        }
        if (obj instanceof String) {
            return (String) obj;
        }
        return obj.toString();
    }

    public static String toStringTrim(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof String) {
            return ((String) obj).trim();
        }
        return obj.toString().trim();
    }

    public static String toStringTrim(Object obj, String defaultValue) {
        if (obj == null) {
            return defaultValue;
        }
        if (obj instanceof String) {
            return ((String) obj).trim();
        }
        return obj.toString().trim();
    }

    public static String prop2String(Object obj) {
        if (obj == null) {
            return null;
        }
        return ToStringBuilder.reflectionToString(obj);
    }

    public static Integer toInteger(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Integer) {
            return (Integer) obj;
        }
        try {
            return new Integer(obj.toString().trim());
        } catch (Exception e) {
        }
        return null;
    }

    public static Integer toInteger(Object obj, Integer defaultValue) {
        Integer value = toInteger(obj);
        if (value != null) {
            return value;
        }
        return defaultValue;
    }

    public static Long toLong(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Long) {
            return (Long) obj;
        }
        try {
            return new Long(obj.toString().trim());
        } catch (Exception e) {
        }
        return null;
    }

    public static Long toLong(Object obj, Long defaultValue) {
        Long value = toLong(obj);
        if (value != null) {
            return value;
        }
        return defaultValue;
    }

    public static BigDecimal toBigDecimal(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof BigDecimal) {
            return (BigDecimal) obj;
        }
        try {
            return new BigDecimal(obj.toString().trim());
        } catch (Exception e) {
        }
        return null;
    }

    public static BigDecimal toBigDecimal(Object obj, BigDecimal defaultValue) {
        BigDecimal value = toBigDecimal(obj);
        if (value != null) {
            return value;
        }
        return defaultValue;
    }

    public static Boolean toBoolean(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Boolean) {
            return (Boolean) obj;
        }
        try {
            return new Boolean(obj.toString().trim());
        } catch (Exception e) {
        }
        return null;
    }

    public static Boolean toBoolean(Object obj, Boolean defaultValue) {
        if (obj == null) {
            return defaultValue;
        }
        if (obj instanceof Boolean) {
            return (Boolean) obj;
        }
        try {
            return new Boolean(obj.toString().trim());
        } catch (Exception e) {
        }
        return defaultValue;
    }

    public static Integer[] string2IntAry(String str, String separatorChars, Integer defaultValue) {
        if (StringUtils.isBlank(str)) {
            return null;
        }
        String[] strAry = StringUtils.split(str, separatorChars);
        Integer[] intAry = new Integer[strAry.length];
        for (int i = 0; i < strAry.length; i++) {
            intAry[i] = toInteger(strAry[i], defaultValue);
        }
        return intAry;
    }

    public static List<Integer> string2IntList(String str, String separatorChars, Integer defaultValue) {
        if (StringUtils.isBlank(str)) {
            return new ArrayList<Integer>(1);
        }
        String[] strAry = StringUtils.split(str, separatorChars);
        List<Integer> intList = new ArrayList<Integer>(strAry.length);
        for (int i = 0; i < strAry.length; i++) {
            intList.add(toInteger(strAry[i], defaultValue));
        }
        return intList;
    }

    public static Long[] string2LongAry(String str, String separatorChars, Long defaultValue) {
        if (StringUtils.isBlank(str)) {
            return null;
        }
        String[] strAry = StringUtils.split(str, separatorChars);
        Long[] longAry = new Long[strAry.length];
        for (int i = 0; i < strAry.length; i++) {
            longAry[i] = toLong(strAry[i], defaultValue);
        }
        return longAry;
    }

    public static List<Long> string2LongList(String str, String separatorChars, Long defaultValue) {
        if (StringUtils.isBlank(str)) {
            return new ArrayList<Long>(1);
        }
        String[] strAry = StringUtils.split(str, separatorChars);
        List<Long> longList = new ArrayList<Long>(strAry.length);
        for (int i = 0; i < strAry.length; i++) {
            longList.add(toLong(strAry[i], defaultValue));
        }
        return longList;
    }

}
