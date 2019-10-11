package com.luastar.swift.base.utils;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class ClassLoaderUtils {

    private static final Logger logger = LoggerFactory.getLogger(ClassLoaderUtils.class);

    private static ClassLoader classLoader;

    private static ResourcePatternResolver resourcePatternResolver;

    public static ClassLoader getClassLoader() {
        if (classLoader == null) {
            classLoader = Thread.currentThread().getContextClassLoader();
        }
        return classLoader;
    }

    public static ResourcePatternResolver getResourcePatternResolver() {
        if (resourcePatternResolver == null) {
            resourcePatternResolver = new PathMatchingResourcePatternResolver();
        }
        return resourcePatternResolver;
    }

    /**
     * 创建指定类的实例
     *
     * @param clazzName 类名
     * @return
     */
    public static Object getInstance(String clazzName) {
        try {
            Class clazz = loadClass(clazzName);
            if (clazz != null) {
                return clazz.newInstance();
            }
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
    public static <T> T getInstance(Class<T> clazz) {
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

    /**
     * 获取资源流
     *
     * @param resource
     * @return
     */
    public static InputStream getInputStream(String resource) {
        try {
            Resource[] resourceArray = getResourcePatternResolver().getResources(resource);
            if (ArrayUtils.isNotEmpty(resourceArray)) {
                return resourceArray[0].getInputStream();
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * 将资源文件转化为Properties对象
     * 基于classloader实现
     *
     * @param resource 资源文件
     * @return
     */
    /*
    public static Properties getProperties(String resource) {
        try {
            Properties properties = new Properties();
            InputStream is = getClassLoader().getResourceAsStream(resource);
            if (is != null) {
                properties.load(new BufferedReader(new InputStreamReader(is)));
            }
            return properties;
        } catch (IOException e) {
            return new Properties();
        }
    }
    */

    /**
     * 将资源文件转化为Properties对象
     * 基于spring实现
     *
     * @param resource 资源文件
     * @return
     */
    public static Properties getProperties(String... resource) {
        Properties properties = new Properties();
        if (resource == null || resource.length == 0) {
            return properties;
        }
        YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
        for (String resPath : resource) {
            try {
                Resource[] resourceArray = getResourcePatternResolver().getResources(resPath);
                if (resourceArray != null) {
                    for (Resource res : resourceArray) {
                        if (FilenameUtils.isExtension(resPath, "yml")) {
                            logger.info("加载配置文件：{}", res.getDescription());
                            yaml.setResources(res);
                            properties.putAll(yaml.getObject());
                        } else if (FilenameUtils.isExtension(resPath, "properties")) {
                            logger.info("加载配置文件：{}", res.getDescription());
                            PropertiesLoaderUtils.fillProperties(properties, new EncodedResource(res, "UTF-8"));
                        } else {
                            logger.warn("忽略不识别的文件类型，目前仅支持properties和yml文件：{}", res.getDescription());
                        }
                    }
                }
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            logger.info("配置文件变量：{} = {}", entry.getKey(), entry.getValue());
        }
        return properties;
    }

}
