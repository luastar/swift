package com.luastar.swift.rpc.serialize.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.pool.KryoFactory;
import com.esotericsoftware.kryo.pool.KryoPool;
import com.luastar.swift.rpc.serialize.IRpcSerialize;
import com.luastar.swift.rpc.server.RpcRequest;
import com.luastar.swift.rpc.server.RpcResponse;
import org.apache.commons.io.IOUtils;
import org.objenesis.strategy.StdInstantiatorStrategy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Protostuff 序列化
 */
public class KryoSerialize implements IRpcSerialize {

    private static KryoPool pool;

    public KryoSerialize() {
        pool = new KryoPool.Builder(new KryoFactory() {
            public Kryo create() {
                Kryo kryo = new Kryo();
                kryo.setReferences(false);
                kryo.register(RpcRequest.class);
                kryo.register(RpcResponse.class);
                kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
                return kryo;
            }
        }).build();
    }

    public <T> byte[] serialize(T obj) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            Kryo kryo = pool.borrow();
            Output output = new Output(outputStream);
            kryo.writeClassAndObject(output, obj);
            output.close();
            pool.release(kryo);
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(outputStream);
        }
    }

    public <T> T deserialize(byte[] data, Class<T> cls) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
        try {
            Kryo kryo = pool.borrow();
            Input input = new Input(inputStream);
            T result = (T) kryo.readClassAndObject(input);
            input.close();
            pool.release(kryo);
            return result;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

}
