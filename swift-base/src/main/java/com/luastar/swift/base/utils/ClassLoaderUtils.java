package com.luastar.swift.base.utils;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ClassLoaderUtils {

    private static final Logger logger = LoggerFactory.getLogger(ClassLoaderUtils.class);

    private static final Properties EMPTY_PROPERTIES = new Properties();

    private static ClassLoader classLoader;

    /**
     * 创建指定类的实例
     *
     * @param clazzName 类名
     * @return
     */
    public static Object getInstance(String clazzName) {
        try {
            return loadClass(clazzName).newInstance();
        } catch (InstantiationException e) {
            logger.error(e.getMessage(), e);
        } catch (IllegalAccessException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * 创建指定类的实例
     *
     * @param clazz 类
     * @return
     */
    public static <T>T getInstance(Class<T> clazz) {
        try {
            return clazz.newInstance();
        } catch (InstantiationException e) {
            logger.error(e.getMessage(), e);
        } catch (IllegalAccessException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * 根据指定的类名加载类
     *
     * @param name 类名
     * @return
     */
    public static Class<?> loadClass(String name) {
        try {
            return getClassLoader().loadClass(name);
        } catch (ClassNotFoundException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    public static ClassLoader getClassLoader() {
        if (classLoader != null) {
            return classLoader;
        }
        classLoader = Thread.currentThread().getContextClassLoader();
        return classLoader;
    }

    /**
     * 获得资源真实文件路径
     *
     * @param resource 资源
     * @return
     */
    public static String getPath(String resource) {
        return getClassLoader().getResource(resource).getPath();
    }

    /**
     * 将资源文件转化为Properties对象
     *
     * @param resource 资源文件
     * @return
     */
    public static Properties getProperties(String resource) {
        try {
            Properties properties = new Properties();
            InputStream is = getStream(resource);
            if (is == null) {
                return EMPTY_PROPERTIES;
            }
            properties.load(new BufferedReader(new InputStreamReader(is)));
            return properties;
        } catch (IOException e) {
            return EMPTY_PROPERTIES;
        }
    }

    /**
     * 将资源文件加载到输入流中
     *
     * @param resource 资源文件
     * @return
     */
    public static InputStream getStream(String resource) {
        return getClassLoader().getResourceAsStream(resource);
    }

    /**
     * 将资源文件的内容转化为List实例
     *
     * @param resource 资源文件
     * @return
     */
    public static List<String> getList(String resource) {
        List<String> list = new ArrayList<String>();
        InputStream is = getStream(resource);
        if (is == null) {
            return list;
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        try {
            String line = null;
            while ((line = reader.readLine()) != null) {
                list.add(line);
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(reader);
            IOUtils.closeQuietly(is);
        }
        return list;
    }

}
