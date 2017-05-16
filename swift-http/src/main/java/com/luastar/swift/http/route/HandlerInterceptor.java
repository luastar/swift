package com.luastar.swift.http.route;

import com.luastar.swift.http.server.HttpRequest;
import com.luastar.swift.http.server.HttpResponse;

/**
 * 类似于spring mvc的拦截器
 */
public interface HandlerInterceptor {

    /**
     * handler处理前执行
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    boolean preHandle(HttpRequest request, HttpResponse response) throws Exception;

    /**
     * handler处理后执行
     * @param request
     * @param response
     * @throws Exception
     */
    void postHandle(HttpRequest request, HttpResponse response) throws Exception;

}
