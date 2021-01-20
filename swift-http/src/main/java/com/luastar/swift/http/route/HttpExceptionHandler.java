package com.luastar.swift.http.route;

import com.luastar.swift.http.server.HttpRequest;
import com.luastar.swift.http.server.HttpResponse;

public interface HttpExceptionHandler {

    /**
     * 业务异常处理
     *
     * @param request
     * @param response
     * @param exception
     */
    void businessExceptionHandle(HttpRequest request, HttpResponse response, Throwable exception);

    /**
     * 系统异常处理
     *
     * @param exception
     */
    void systemExceptionHandle(Throwable exception);

}
