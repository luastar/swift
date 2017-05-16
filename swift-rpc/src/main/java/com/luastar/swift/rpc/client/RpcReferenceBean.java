package com.luastar.swift.rpc.client;


import com.luastar.swift.base.utils.ClassLoaderUtils;
import com.luastar.swift.rpc.constant.RpcConstant;
import com.luastar.swift.rpc.registry.IServiceDiscovery;
import com.luastar.swift.rpc.serialize.IRpcSerialize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

/**
 * rpc 引用注解实例化处理
 */
public class RpcReferenceBean implements BeanPostProcessor {

    private static final Logger logger = LoggerFactory.getLogger(RpcReferenceBean.class);

    private RpcProxy rpcProxy;

    public RpcReferenceBean() {
        IServiceDiscovery serviceDiscovery = ClassLoaderUtils.getInstance(RpcConstant.getRegistryType().getDiscoveryClass());
        IRpcSerialize rpcSerialize = ClassLoaderUtils.getInstance(RpcConstant.getSerializeType().getSerializeClass());
        rpcProxy = new RpcProxy(serviceDiscovery, rpcSerialize);
    }

    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Field[] fields = bean.getClass().getDeclaredFields();
        for (Field field : fields) {
            try {
                RpcReference reference = field.getAnnotation(RpcReference.class);
                if (reference == null) {
                    continue;
                }
                ReflectionUtils.makeAccessible(field);
                Object value = createProxy(reference, field.getType());
                if (value != null) {
                    field.set(bean, value);
                }
            } catch (Throwable e) {
                logger.error("Failed to init remote service reference at filed " + field.getName() + " in class " + bean.getClass().getName() + ", cause: " + e.getMessage(), e);
            }
        }
        return bean;
    }

    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    /**
     * 生成客户端引用对象
     *
     * @param reference
     * @param referenceClass
     * @return
     */
    private Object createProxy(RpcReference reference, Class<?> referenceClass) {
        if (!referenceClass.isInterface()) {
            throw new IllegalStateException("The @RpcReference undefined interfaceClass, and the property type " + referenceClass.getName() + " is not a interface.");
        }
        logger.info("create reference proxy for reference={}, version={}", referenceClass.getName(), reference.version());
        return rpcProxy.create(referenceClass, reference.version());
    }

}
