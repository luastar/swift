package com.luastar.swift.rpc.serialize.hessian;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.luastar.swift.rpc.serialize.IRpcSerialize;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Protostuff 序列化
 */
public class HessianSerialize implements IRpcSerialize {

    public <T> byte[] serialize(T obj) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            Hessian2Output ho = new Hessian2Output(output);
            ho.startMessage();
            ho.writeObject(obj);
            ho.completeMessage();
            ho.close();
            return output.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(output);
        }
    }

    public <T> T deserialize(byte[] data, Class<T> cls) {
        ByteArrayInputStream input = new ByteArrayInputStream(data);
        try {
            Hessian2Input hi = new Hessian2Input(input);
            hi.startMessage();
            T result = (T) hi.readObject();
            hi.completeMessage();
            hi.close();
            return result;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(input);
        }
    }

}
