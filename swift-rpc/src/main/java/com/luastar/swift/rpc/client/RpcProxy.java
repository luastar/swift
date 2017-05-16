package com.luastar.swift.rpc.client;

import com.luastar.swift.rpc.registry.IServiceDiscovery;
import com.luastar.swift.rpc.serialize.IRpcSerialize;
import com.luastar.swift.rpc.server.RpcRequest;
import com.luastar.swift.rpc.server.RpcResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;

/**
 * RPC 代理（用于创建 RPC 服务代理）
 */
public class RpcProxy {

    private static final Logger logger = LoggerFactory.getLogger(RpcProxy.class);

    private IServiceDiscovery serviceDiscovery;

    private IRpcSerialize rpcSerialize;

    public RpcProxy(IServiceDiscovery serviceDiscovery, IRpcSerialize rpcSerialize) {
        this.serviceDiscovery = serviceDiscovery;
        this.rpcSerialize = rpcSerialize;
        if (serviceDiscovery == null) {
            throw new RuntimeException("serviceDiscovery is null!");
        }
        if (rpcSerialize == null) {
            throw new RuntimeException("serviceDiscovery is null!");
        }
    }

    public <T> T create(final Class<?> interfaceClass) {
        return create(interfaceClass, "");
    }

    public <T> T create(final Class<?> interfaceClass, final String serviceVersion) {
        // 创建动态代理对象
        return (T) Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass},
                new RpcProxyHandler(interfaceClass, serviceVersion));
    }

    private class RpcProxyHandler implements InvocationHandler {

        private Class<?> interfaceClass;
        private String serviceVersion;

        public RpcProxyHandler(Class<?> interfaceClass, String serviceVersion) {
            this.interfaceClass = interfaceClass;
            this.serviceVersion = serviceVersion;
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            // 创建 RPC 请求对象并设置请求属性
            RpcRequest request = new RpcRequest();
            request.setRequestId(UUID.randomUUID().toString());
            request.setInterfaceName(method.getDeclaringClass().getName());
            request.setServiceVersion(serviceVersion);
            request.setMethodName(method.getName());
            request.setParameterTypes(method.getParameterTypes());
            request.setParameters(args);
            // 获取 RPC 服务地址
            String serviceAddress = null;
            String serviceName = interfaceClass.getName();
            if (StringUtils.isNotEmpty(serviceVersion)) {
                serviceName += "-" + serviceVersion;
            }
            serviceAddress = serviceDiscovery.discover(serviceName);
            if (StringUtils.isEmpty(serviceAddress)) {
                throw new RuntimeException("server address is empty.");
            }
            // 从 RPC 服务地址中解析主机名与端口号
            String[] addressAry = StringUtils.split(serviceAddress, ":");
            // 创建 RPC 客户端对象并发送 RPC 请求
            RpcClient client = new RpcClient(addressAry[0], Integer.parseInt(addressAry[1]), rpcSerialize);
            long startTime = System.currentTimeMillis();
            RpcResponse response = client.send(request);
            logger.info("rpc cost: {}ms", System.currentTimeMillis() - startTime);
            if (response == null) {
                throw new RuntimeException("response is null.");
            }
            // 返回 RPC 响应结果
            if (response.getException() != null) {
                throw response.getException();
            }
            return response.getResult();
        }

    }

}
