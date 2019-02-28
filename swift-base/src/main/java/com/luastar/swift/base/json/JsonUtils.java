package com.luastar.swift.base.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Map;
import java.util.TimeZone;

public class JsonUtils {

    private static Logger logger = LoggerFactory.getLogger(JsonUtils.class);

    private static ObjectMapper defaultMapper;
    private static ObjectMapper snakeMapper;

    static {
        // 时间格式化设置
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        // 默认mapper
        defaultMapper = new ObjectMapper();
        defaultMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        defaultMapper.setDateFormat(sdf);
        defaultMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        // 下画线mapper
        snakeMapper = new ObjectMapper();
        snakeMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        snakeMapper.setDateFormat(sdf);
        snakeMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        snakeMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
    }

    public static String toJson(Object obj) {
        try {
            if (obj == null) {
                return null;
            }
            return defaultMapper.writeValueAsString(obj);
        } catch (IOException e) {
            logger.error("[writeValueAsString]：" + e.getMessage(), e);
        }
        return null;
    }

    public static String toJsonSnake(Object obj) {
        try {
            if (obj == null) {
                return null;
            }
            return snakeMapper.writeValueAsString(obj);
        } catch (IOException e) {
            logger.error("[writeValueAsString]：" + e.getMessage(), e);
        }
        return null;
    }

    public static <T> T toObj(String json, Class<T> clazz) {
        try {
            if (StringUtils.isEmpty(json)) {
                return null;
            }
            return defaultMapper.readValue(json, clazz);
        } catch (IOException e) {
            logger.error("[readValue]：" + e.getMessage(), e);
        }
        return null;
    }

    public static <T> T toObjSnake(String json, Class<T> clazz) {
        try {
            if (StringUtils.isEmpty(json)) {
                return null;
            }
            return snakeMapper.readValue(json, clazz);
        } catch (IOException e) {
            logger.error("[readValue]：" + e.getMessage(), e);
        }
        return null;
    }

    public static <T> T toObj(String json, Class<? extends Collection> collectionClass, Class<?> elementClass) {
        try {
            if (StringUtils.isEmpty(json)) {
                return null;
            }
            JavaType javaType = defaultMapper.getTypeFactory().constructCollectionType(collectionClass, elementClass);
            return (T) defaultMapper.readValue(json, javaType);
        } catch (IOException e) {
            logger.error("[toObj]" + e.getMessage(), e);
        }
        return null;
    }

    public static <T> T toObj(String json, Class<? extends Map> mapClass, Class<?> keyClass, Class<?> valueClass) {
        try {
            if (StringUtils.isEmpty(json)) {
                return null;
            }
            JavaType javaType = defaultMapper.getTypeFactory().constructMapType(mapClass, keyClass, valueClass);
            return (T) defaultMapper.readValue(json, javaType);
        } catch (IOException e) {
            logger.error("[toObj]" + e.getMessage(), e);
        }
        return null;
    }

    public static String formatJson(String json) {
        try {
            if (json == null) {
                return null;
            }
            Object obj = defaultMapper.readValue(json, Object.class);
            return defaultMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (Exception e) {
            logger.error("[formatJson]：" + e.getMessage(), e);
        }
        return json;
    }

}
