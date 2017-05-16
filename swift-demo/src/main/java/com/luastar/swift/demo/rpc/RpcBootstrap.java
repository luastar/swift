package com.luastar.swift.demo.rpc;


import com.luastar.swift.base.utils.ObjUtils;
import com.luastar.swift.rpc.server.RpcServer;

public class RpcBootstrap {

    public static void main(String[] args) {
        int port = 8091;
        if (args != null && args.length > 0) {
            port = ObjUtils.toInteger(args[0], port);
        }
        new RpcServer(port).start();
    }

}
