package com.luastar.swift.base.json;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.PropertyNamingStrategy;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.fasterxml.jackson.databind.JavaType;

import java.util.Collection;
import java.util.Map;

public abstract class JsonUtils {

    protected static JsonMapper jsonMapper = new JsonMapper();

    private static SerializeConfig serializeConfig;
    private static ParserConfig parserConfig;

    static {
        serializeConfig = new SerializeConfig();
        serializeConfig.propertyNamingStrategy = PropertyNamingStrategy.SnakeCase;
        parserConfig = new ParserConfig();
        parserConfig.propertyNamingStrategy = PropertyNamingStrategy.SnakeCase;
    }

    public static JsonMapper getJsonMapper() {
        return jsonMapper;
    }

    public static String toJson(Object obj) {
        return jsonMapper.toJson(obj);
    }

    public static String toJsonSnake(Object obj) {
        return JSON.toJSONString(obj, serializeConfig);
    }

    public static <T> T toObj(String json, Class<T> clazz) {
        return jsonMapper.toObj(json, clazz);
    }

    public static <T> T toObjSnake(String json, Class<T> clazz) {
        return JSON.parseObject(json, clazz, parserConfig);
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
