package com.luastar.swift.base.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.dozer.DozerBeanMapper;
import org.dozer.Mapper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ObjUtils {

    private static Mapper mapper;

    public static <T> T ifNull(T object, T defaultValue) {
        return object == null ? defaultValue : object;
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
        return ifNull(toString(obj), defaultValue);
    }

    public static String toStringTrim(Object obj) {
        String value = toString(obj);
        if (value == null) {
            return null;
        }
        return value.trim();
    }

    public static String toStringTrim(Object obj, String defaultValue) {
        return ifNull(toStringTrim(obj), defaultValue);
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
        return ifNull(toInteger(obj), defaultValue);
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
        return ifNull(toLong(obj), defaultValue);
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
        return ifNull(toBigDecimal(obj), defaultValue);
    }

    public static Boolean toBoolean(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Boolean) {
            return (Boolean) obj;
        }
        if (obj instanceof Number) {
            return Boolean.valueOf(((Number) obj).intValue() == 1);
        }
        if (obj instanceof String) {
            String strVal = (String) obj;
            if (strVal.length() == 0 || "null".equalsIgnoreCase(strVal)) {
                return null;
            }
            if ("true".equalsIgnoreCase(strVal)
                    || "T".equalsIgnoreCase(strVal)
                    || "Y".equalsIgnoreCase(strVal)
                    || "1".equals(strVal)) {
                return Boolean.TRUE;
            }
            if ("false".equalsIgnoreCase(strVal)
                    || "F".equalsIgnoreCase(strVal)
                    || "N".equalsIgnoreCase(strVal)
                    || "0".equals(strVal)) {
                return Boolean.FALSE;
            }
        }
        try {
            return Boolean.valueOf(obj.toString().trim());
        } catch (Exception e) {
        }
        return null;
    }

    public static Boolean toBoolean(Object obj, Boolean defaultValue) {
        return ifNull(toBoolean(obj), defaultValue);
    }

    public static Mapper getMapper() {
        if (mapper == null) {
            synchronized (ObjUtils.class) {
                if (mapper == null) {
                    mapper = new DozerBeanMapper();
                }
            }
        }
        return mapper;
    }

    public static void map(Object source, Object destination) {
        getMapper().map(source, destination);
    }

    public static <T> T map(Object source, Class<T> destinationClass) {
        return getMapper().map(source, destinationClass);
    }

    public static String prop2String(Object obj) {
        if (obj == null) {
            return null;
        }
        return ToStringBuilder.reflectionToString(obj);
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
