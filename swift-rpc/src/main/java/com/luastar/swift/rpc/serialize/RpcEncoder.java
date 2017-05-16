package com.luastar.swift.rpc.serialize;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class RpcEncoder extends MessageToByteEncoder {

    protected Class<?> genericClass;

    private IRpcSerialize rpcSerialize;

    public RpcEncoder(Class<?> genericClass, IRpcSerialize rpcSerialize) {
        this.genericClass = genericClass;
        this.rpcSerialize = rpcSerialize;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        if (genericClass.isInstance(msg)) {
            byte[] data = rpcSerialize.serialize(msg);
            out.writeInt(data.length);
            out.writeBytes(data);
        }
    }

}
