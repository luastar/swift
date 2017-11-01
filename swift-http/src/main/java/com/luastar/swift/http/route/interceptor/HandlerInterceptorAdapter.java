package com.luastar.swift.http.route.interceptor;

import com.luastar.swift.http.server.HttpRequest;
import com.luastar.swift.http.server.HttpResponse;

/**
 *
 */
public class HandlerInterceptorAdapter implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpRequest request, HttpResponse response) throws Exception {
        return true;
    }

    @Override
    public void postHandle(HttpRequest request, HttpResponse response) throws Exception {

    }

}
