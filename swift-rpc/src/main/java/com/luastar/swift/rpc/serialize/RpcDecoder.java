package com.luastar.swift.rpc.serialize;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class RpcDecoder extends ByteToMessageDecoder {

    protected Class<?> genericClass;

    private IRpcSerialize rpcSerialize;

    public RpcDecoder(Class<?> genericClass, IRpcSerialize rpcSerialize) {
        this.genericClass = genericClass;
        this.rpcSerialize = rpcSerialize;
    }

    @Override
    public final void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() < 4) {
            return;
        }
        in.markReaderIndex();
        int dataLength = in.readInt();
        if (in.readableBytes() < dataLength) {
            in.resetReaderIndex();
            return;
        }
        byte[] data = new byte[dataLength];
        in.readBytes(data);
        Object obj = rpcSerialize.deserialize(data, genericClass);
        out.add(obj);
    }

}
