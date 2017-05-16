package com.luastar.swift.rpc.registry;

import com.luastar.swift.rpc.constant.RpcConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 简单的服务发现接口实现
 */
public class SimpleServiceDiscovery implements IServiceDiscovery {

    private static final Logger logger = LoggerFactory.getLogger(SimpleServiceDiscovery.class);

    /**
     * 服务地址map
     */
    private String serviceAddress = RpcConstant.REMOTE_ADDRESS;

    public SimpleServiceDiscovery() {
    }

    public String getServiceAddress() {
        return serviceAddress;
    }

    public void setServiceAddress(String serviceAddress) {
        this.serviceAddress = serviceAddress;
    }

    public String discover(String serviceName) {
        logger.info("发现服务：serviceName={}, serviceAddress={}", serviceName, serviceAddress);
        return serviceAddress;
    }

}