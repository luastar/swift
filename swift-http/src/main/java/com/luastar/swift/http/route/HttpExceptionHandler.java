package com.luastar.swift.http.route;

import com.luastar.swift.http.server.HttpRequest;
import com.luastar.swift.http.server.HttpResponse;

public interface HttpExceptionHandler {

    /**
     * service处理异常执行
     * @param request
     * @param response
     * @param exception
     */
    void exceptionHandle(HttpRequest request, HttpResponse response, Throwable exception);

}
