package com.luastar.swift.demo.http.handler;

import com.luastar.swift.demo.http.utils.ResponseResultUtils;
import com.luastar.swift.http.route.HttpExceptionHandler;
import com.luastar.swift.http.server.HttpRequest;
import com.luastar.swift.http.server.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultHttpExceptionHandler implements HttpExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(DefaultHttpExceptionHandler.class);

    @Override
    public void exceptionHandle(HttpRequest request, HttpResponse response, Throwable exception) {
        logger.error(exception.getMessage(), exception);
        ResponseResultUtils.exception(response, exception);
    }

}
