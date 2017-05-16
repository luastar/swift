package com.luastar.swift.rpc.constant;

import com.luastar.swift.base.config.PropertyUtils;
import org.apache.commons.lang3.StringUtils;

public class RpcConstant {

    public static final String SWIFT_CONFIG_LOCATION = PropertyUtils.getString("swift.config.location", "spring-swift.xml");

    public static final String CURRENT_SERVER_ADDRESS = PropertyUtils.getString("swift.server.address", "127.0.0.1");

    public static final String REGISTRY_TYPE = PropertyUtils.getString("swift.server.registry", RegistryType.SIMPLE.getKey());
    public static final String SERIALIZE_TYPE = PropertyUtils.getString("swift.server.serialize", SerializeType.HESSIAN.getKey());

    public static final String REMOTE_ADDRESS = PropertyUtils.getString("swift.remote.address");

    public static final String ZK_ADDRESS = PropertyUtils.getString("swift.zookeeper.address", "127.0.0.1:2181");
    public static final String ZK_REGISTRY_PATH = "/netty-swift";
    public static final int ZK_SESSION_TIMEOUT = PropertyUtils.getInt("swift.zookeeper.session.timeout", 5000);
    public static final int ZK_CONNECTION_TIMEOUT = PropertyUtils.getInt("swift.zookeeper.connection.timeout", 1000);

    public static RegistryType getRegistryType() {
        if (StringUtils.isEmpty(REGISTRY_TYPE)) {
            return RegistryType.SIMPLE;
        }
        if (REGISTRY_TYPE.equalsIgnoreCase(RegistryType.ZOOKEEPER.getKey())) {
            return RegistryType.ZOOKEEPER;
        } else {
            return RegistryType.SIMPLE;
        }
    }

    public static SerializeType getSerializeType() {
        if (StringUtils.isEmpty(SERIALIZE_TYPE)) {
            return SerializeType.HESSIAN;
        }
        if (SERIALIZE_TYPE.equalsIgnoreCase(SerializeType.KRYO.getKey())) {
            return SerializeType.KRYO;
        } else if (SERIALIZE_TYPE.equalsIgnoreCase(SerializeType.PROTOSTUFF.getKey())) {
            return SerializeType.PROTOSTUFF;
        } else {
            return SerializeType.HESSIAN;
        }
    }

}
