package com.luastar.swift.base.json;

import com.fasterxml.jackson.databind.JavaType;

import java.util.Collection;
import java.util.Map;

public abstract class JsonUtils {

    protected static JsonMapper jsonMapper = new JsonMapper();

    public static JsonMapper getJsonMapper() {
        return jsonMapper;
    }

    public static String toJson(Object obj) {
        return jsonMapper.toJson(obj);
    }

    public static <T> T toObj(String json, Class<T> clazz) {
        return jsonMapper.toObj(json, clazz);
    }

    public static <T> T toObj(String json, Class<? extends Collection> collectionClass, Class<?> elementClass) {
        JavaType javaType = jsonMapper.contructCollectionType(collectionClass, elementClass);
        return jsonMapper.toObj(json, javaType);
    }

    public static <T> T toObj(String json, Class<? extends Map> mapClass, Class<?> keyClass, Class<?> valueClass) {
        JavaType javaType = jsonMapper.contructMapType(mapClass, keyClass, valueClass);
        return jsonMapper.toObj(json, javaType);
    }

    public static String formatJson(String json) {
        return jsonMapper.formatJson(json);
    }

}
