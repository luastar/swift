package com.luastar.swift.http.processor;

import com.luastar.swift.base.config.ConfigFactory;
import com.luastar.swift.base.utils.ObjUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.Environment;

import java.util.Map;

/**
 * @author : liuwei
 * @date : 2022/4/12
 * @desc :
 */
public class ConfigBeanPostProcessor implements BeanPostProcessor , ApplicationContextAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigBeanPostProcessor.class);

    private ApplicationContext applicationContext;

    private volatile boolean isLoading;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public Object postProcessBeforeInitialization(Object o, String s) throws BeansException {
        if (isLoading) {
            return o;
        }
        isLoading = true;
        Environment environment = applicationContext.getEnvironment();
        if (environment instanceof AbstractEnvironment) {
            Map<String, Object> systemProperties = ((AbstractEnvironment) environment).getSystemProperties();
            if (ObjUtils.isNotEmpty(systemProperties)) {
                ConfigFactory.getConfig().putAll(systemProperties);
            }
        } else {
            try {
                ConfigFactory.getConfig().putAll(System.getProperties());
            } catch (Exception e) {
                LOGGER.error("SWIFT-CONFIG-PROCESSOR loading system properties exception：", e);
            }
        }
        return o;
    }

    @Override
    public Object postProcessAfterInitialization(Object o, String s) throws BeansException {
        return o;
    }


}
