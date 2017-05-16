package com.luastar.swift.rpc.registry;

import com.luastar.swift.rpc.constant.RpcConstant;
import org.I0Itec.zkclient.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 基于 ZooKeeper 的服务注册接口实现
 */
public class ZooKeeperServiceRegistry implements IServiceRegistry {

    private static final Logger logger = LoggerFactory.getLogger(ZooKeeperServiceRegistry.class);

    private final ZkClient zkClient;

    public ZooKeeperServiceRegistry() {
        zkClient = new ZkClient(RpcConstant.ZK_ADDRESS, RpcConstant.ZK_SESSION_TIMEOUT, RpcConstant.ZK_CONNECTION_TIMEOUT);
    }

    public void register(String serviceName, String serviceAddress) {
        // 创建 registry 根节点（持久）
        if (!zkClient.exists(RpcConstant.ZK_REGISTRY_PATH)) {
            zkClient.createPersistent(RpcConstant.ZK_REGISTRY_PATH);
            logger.info("create registry node: {}", RpcConstant.ZK_REGISTRY_PATH);
        }
        // 创建 service 节点（持久）
        String servicePath = RpcConstant.ZK_REGISTRY_PATH + "/" + serviceName;
        if (!zkClient.exists(servicePath)) {
            zkClient.createPersistent(servicePath);
            logger.info("create service node: {}", servicePath);
        }
        // 创建 address 节点（临时）
        String addressPath = servicePath + "/address-";
        String addressNode = zkClient.createEphemeralSequential(addressPath, serviceAddress);
        logger.info("create address node: {}", addressNode);
    }

}