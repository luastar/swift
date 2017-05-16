package com.luastar.swift.rpc.constant;

import com.luastar.swift.rpc.registry.*;

public enum RegistryType {

    SIMPLE("simple", SimpleServiceRegistry.class, SimpleServiceDiscovery.class),
    ZOOKEEPER("zookeeper", ZooKeeperServiceRegistry.class, ZooKeeperServiceDiscovery.class);

    private String key;
    private Class<? extends IServiceRegistry> registryClass;
    private Class<? extends IServiceDiscovery> discoveryClass;

    RegistryType(String key, Class<? extends IServiceRegistry> registryClass, Class<? extends IServiceDiscovery> discoveryClass) {
        this.key = key;
        this.registryClass = registryClass;
        this.discoveryClass = discoveryClass;
    }

    public String getKey() {
        return key;
    }

    public Class<? extends IServiceRegistry> getRegistryClass() {
        return registryClass;
    }

    public Class<? extends IServiceDiscovery> getDiscoveryClass() {
        return discoveryClass;
    }

}
