package com.luastar.swift.rpc.constant;

import com.luastar.swift.rpc.serialize.IRpcSerialize;
import com.luastar.swift.rpc.serialize.hessian.HessianSerialize;
import com.luastar.swift.rpc.serialize.kryo.KryoSerialize;
import com.luastar.swift.rpc.serialize.protostuff.ProtostuffSerialize;

public enum SerializeType {

    HESSIAN("hessian", HessianSerialize.class),
    KRYO("kryo", KryoSerialize.class),
    PROTOSTUFF("protostuff", ProtostuffSerialize.class);

    private String key;
    private Class<? extends IRpcSerialize> serializeClass;

    SerializeType(String key, Class<? extends IRpcSerialize> serializeClass) {
        this.key = key;
        this.serializeClass = serializeClass;
    }

    public String getKey() {
        return key;
    }

    public Class<? extends IRpcSerialize> getSerializeClass() {
        return serializeClass;
    }
    
}
