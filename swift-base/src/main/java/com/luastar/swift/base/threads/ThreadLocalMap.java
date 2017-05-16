package com.luastar.swift.base.threads;


import java.util.HashMap;
import java.util.Map;

public class ThreadLocalMap {

    private static final ThreadLocal THREAD_LOCAL_MAP = new ThreadLocal();

    /**
     * 获得线程中保存的属性.
     *
     * @param attribute 属性名称
     * @return 属性值
     */
    public static Object get(String attribute) {
        Map map = (Map) THREAD_LOCAL_MAP.get();
        return map.get(attribute);
    }

    /**
     * 获得线程中保存的属性，使用指定类型进行转型.
     *
     * @param attribute 属性名称
     * @param clazz     类型
     * @param <T>       自动转型
     * @return 属性值
     */
    public static <T> T get(String attribute, Class<T> clazz) {
        return (T) get(attribute);
    }

    /**
     * 设置制定属性名的值.
     *
     * @param attribute 属性名称
     * @param value     属性值
     */
    public static void set(String attribute, Object value) {
        Map map = (Map) THREAD_LOCAL_MAP.get();
        if (map == null) {
            map = new HashMap();
            THREAD_LOCAL_MAP.set(map);
        }
        map.put(attribute, value);
    }

}
