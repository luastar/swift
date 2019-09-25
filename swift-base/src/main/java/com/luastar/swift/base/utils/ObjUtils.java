package com.luastar.swift.base.utils;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.dozer.DozerBeanMapper;
import org.dozer.Mapper;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ObjUtils {

    private static final String NULL_VALUE = "NULL";
    private static final String EMPTY_VALUE = "";
    private static final String COMMA = ",";
    private static final String BOOLEAN_TRUE = "TRUE";
    private static final String BOOLEAN_FALSE = "FALSE";
    private static final String BOOLEAN_T = "T";
    private static final String BOOLEAN_F = "F";
    private static final String BOOLEAN_Y = "Y";
    private static final String BOOLEAN_N = "N";
    private static final String BOOLEAN_1 = "1";
    private static final String BOOLEAN_0 = "0";

    private static Mapper mapper;

    public static <T> T ifNull(T object, T defaultValue) {
        return object == null ? defaultValue : object;
    }

    public static <T> T ifEmpty(T object, T defaultValue) {
        return isEmpty(object) ? defaultValue : object;
    }

    public static boolean isNull(Object value) {
        if (value == null) {
            return true;
        } else if (value instanceof String && NULL_VALUE.equalsIgnoreCase((String) value)) {
            return true;
        }
        return false;
    }

    public static boolean isEmpty(Object value) {
        if (value == null) {
            return true;
        } else if (value instanceof String) {
            if (StringUtils.isEmpty((String) value) || NULL_VALUE.equalsIgnoreCase((String) value)) {
                return true;
            }
        } else if (value instanceof Collection && CollectionUtils.isEmpty((Collection) value)) {
            return true;
        } else if (value instanceof Map && MapUtils.isEmpty((Map) value)) {
            return true;
        }
        return false;
    }

    public static boolean isNotEmpty(Object value) {
        return !isEmpty(value);
    }

    public static <T> boolean isEmpty(final T[] array) {
        return ArrayUtils.isEmpty(array);
    }

    public static <T> boolean isNotEmpty(final T[] array) {
        return ArrayUtils.isNotEmpty(array);
    }

    public static boolean isBlank(Object value) {
        if (value == null) {
            return true;
        } else if (value instanceof String) {
            if (StringUtils.isBlank((String) value) || NULL_VALUE.equalsIgnoreCase((String) value)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isNotBlank(Object value) {
        return !isBlank(value);
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
        if (isEmpty(obj)) {
            return null;
        }
        if (obj instanceof Integer) {
            return (Integer) obj;
        }
        if (obj instanceof Number) {
            return ((Number) obj).intValue();
        }
        // 处理字符串值
        String strValue = obj.toString().trim();
        try {
            Number number = NumberFormat.getNumberInstance().parse(strValue);
            return number.intValue();
        } catch (Exception e) {
        }
        return null;
    }

    public static Integer toInteger(Object obj, Integer defaultValue) {
        return ifNull(toInteger(obj), defaultValue);
    }

    public static Long toLong(Object obj) {
        if (isEmpty(obj)) {
            return null;
        }
        if (obj instanceof Long) {
            return (Long) obj;
        }
        if (obj instanceof Number) {
            return ((Number) obj).longValue();
        }
        // 处理字符串值
        String strValue = obj.toString().trim();
        try {
            Number number = NumberFormat.getNumberInstance().parse(strValue);
            return number.longValue();
        } catch (Exception e) {
        }
        return null;
    }

    public static Long toLong(Object obj, Long defaultValue) {
        return ifNull(toLong(obj), defaultValue);
    }

    public static BigDecimal toBigDecimal(Object obj) {
        if (isEmpty(obj)) {
            return null;
        }
        if (obj instanceof BigDecimal) {
            return (BigDecimal) obj;
        }
        // 处理字符串值
        String strValue = obj.toString().trim();
        try {
            Number number = NumberFormat.getNumberInstance().parse(strValue);
            return new BigDecimal(number.doubleValue());
        } catch (Exception e) {
        }
        return null;
    }

    public static BigDecimal toBigDecimal(Object obj, BigDecimal defaultValue) {
        return ifNull(toBigDecimal(obj), defaultValue);
    }

    public static Boolean toBoolean(Object obj) {
        if (isEmpty(obj)) {
            return null;
        }
        if (obj instanceof Boolean) {
            return (Boolean) obj;
        }
        if (obj instanceof Number) {
            return Boolean.valueOf(((Number) obj).intValue() == 1);
        }
        // 处理字符串值
        String strValue = obj.toString().trim();
        if (BOOLEAN_TRUE.equalsIgnoreCase(strValue)
                || BOOLEAN_T.equalsIgnoreCase(strValue)
                || BOOLEAN_Y.equalsIgnoreCase(strValue)
                || BOOLEAN_1.equals(strValue)) {
            return Boolean.TRUE;
        }
        if (BOOLEAN_FALSE.equalsIgnoreCase(strValue)
                || BOOLEAN_F.equalsIgnoreCase(strValue)
                || BOOLEAN_N.equalsIgnoreCase(strValue)
                || BOOLEAN_0.equals(strValue)) {
            return Boolean.FALSE;
        }
        return null;
    }

    public static Boolean toBoolean(Object obj, Boolean defaultValue) {
        return ifNull(toBoolean(obj), defaultValue);
    }

    public static boolean toboolean(Object obj) {
        return toBoolean(obj, false);
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
        if (isBlank(str)) {
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
        if (isBlank(str)) {
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
        if (isBlank(str)) {
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
        if (isBlank(str)) {
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
