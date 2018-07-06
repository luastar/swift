package com.luastar.swift.demo.http;

import com.luastar.swift.base.utils.ObjUtils;
import com.luastar.swift.http.server.HttpServer;

public class HttpBootstrap {

    public static void main(String[] args) {
        int port = 8081;
        if (args != null && args.length > 0) {
            port = ObjUtils.toInteger(args[0], port);
        } else {
            System.setProperty("port", String.valueOf(port));
        }
        new HttpServer(port).start();
    }

}
