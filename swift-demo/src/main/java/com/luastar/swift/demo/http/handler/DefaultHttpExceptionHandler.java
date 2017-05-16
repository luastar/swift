package com.luastar.swift.demo.http.handler;

import com.google.common.collect.Maps;
import com.luastar.swift.base.json.JsonUtils;
import com.luastar.swift.base.utils.ExceptionUtils;
import com.luastar.swift.http.route.HttpExceptionHandler;
import com.luastar.swift.http.server.HttpRequest;
import com.luastar.swift.http.server.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class DefaultHttpExceptionHandler implements HttpExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(DefaultHttpExceptionHandler.class);

    public void exceptionHandle(HttpRequest request, HttpResponse response, Exception exception) {
        //logger.error(exception.getMessage(), exception);
        Map<String, Object> headMap = Maps.newLinkedHashMap();
        headMap.put("requestId", request.getRequestId());
        headMap.put("status", 3);
        headMap.put("msg", "系统异常：" + ExceptionUtils.getErrorMessageWithNestedException(exception));
        Map<String, Object> resultMap = Maps.newLinkedHashMap();
        resultMap.put("head", headMap);
        String result = JsonUtils.toJson(resultMap);
        logger.info("result:{}", result);
        response.setResult(result);
    }

}
