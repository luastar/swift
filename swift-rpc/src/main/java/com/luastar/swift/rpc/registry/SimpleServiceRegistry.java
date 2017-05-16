package com.luastar.swift.rpc.registry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 简单的服务注册接口实现
 */
public class SimpleServiceRegistry implements IServiceRegistry {

    private static final Logger logger = LoggerFactory.getLogger(SimpleServiceRegistry.class);

    public SimpleServiceRegistry() {
    }

    public void register(String serviceName, String serviceAddress) {
        logger.info("注册服务：serviceName={}, serviceAddress={}", serviceName, serviceAddress);
    }

}