package com.luastar.swift.rpc.registry;

import com.luastar.swift.rpc.constant.RpcConstant;
import org.I0Itec.zkclient.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 基于 ZooKeeper 的服务发现接口实现
 */
public class ZooKeeperServiceDiscovery implements IServiceDiscovery {

    private static final Logger logger = LoggerFactory.getLogger(ZooKeeperServiceDiscovery.class);

    private final ZkClient zkClient;

    public ZooKeeperServiceDiscovery() {
        zkClient = new ZkClient(RpcConstant.ZK_ADDRESS, RpcConstant.ZK_SESSION_TIMEOUT, RpcConstant.ZK_CONNECTION_TIMEOUT);
    }

    public String discover(String serviceName) {
        // 获取 service 节点
        String servicePath = RpcConstant.ZK_REGISTRY_PATH + "/" + serviceName;
        if (!zkClient.exists(servicePath)) {
            throw new RuntimeException(String.format("can not find any service node on path: %s", servicePath));
        }
        List<String> addressList = zkClient.getChildren(servicePath);
        if (addressList == null || addressList.isEmpty()) {
            throw new RuntimeException(String.format("can not find any address node on path: %s", servicePath));
        }
        // 获取 address 节点
        String address = null;
        int addressNum = addressList.size();
        if (addressNum == 1) {
            // 若只有一个地址，则获取该地址
            address = addressList.get(0);
            logger.info("get only address node: {}", address);
        } else {
            // 若存在多个地址，则随机获取一个地址
            address = addressList.get(ThreadLocalRandom.current().nextInt(addressNum));
            logger.info("get random address node: {}", address);
        }
        // 返回 address 节点的值
        String addressPath = servicePath + "/" + address;
        return zkClient.readData(addressPath);
    }

}