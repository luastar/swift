package com.luastar.swift.demo.http.handler;

import com.luastar.swift.http.route.HandlerInterceptor;
import com.luastar.swift.http.server.HttpRequest;
import com.luastar.swift.http.server.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by zhuminghua on 2016/10/10.
 */
public class HelloInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(HelloInterceptor.class);

    public boolean preHandle(HttpRequest request, HttpResponse response) throws Exception {
        logger.info("HelloInterceptor[preHandle]");
        return true;
    }

    public void postHandle(HttpRequest request, HttpResponse response) throws Exception {
        logger.info("HelloInterceptor[postHandle]");
    }

}
