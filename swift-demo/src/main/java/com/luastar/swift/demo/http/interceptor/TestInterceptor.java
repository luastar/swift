package com.luastar.swift.demo.http.interceptor;

import com.luastar.swift.http.route.HandlerInterceptor;
import com.luastar.swift.http.server.HttpRequest;
import com.luastar.swift.http.server.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by zhuminghua on 2016/10/10.
 */
public class TestInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(TestInterceptor.class);

    public boolean preHandle(HttpRequest request, HttpResponse response) throws Exception {
        logger.info("TestInterceptor[preHandle]");
        return true;
    }

    public void postHandle(HttpRequest request, HttpResponse response) throws Exception {
        logger.info("TestInterceptor[postHandle]");
    }

}
