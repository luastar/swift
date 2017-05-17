package com.luastar.swift.base.json;

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

    public static String formatJson(String json) {
        return jsonMapper.formatJson(json);
    }

}
