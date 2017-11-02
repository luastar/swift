package com.luastar.swift.http.route;


import com.google.common.collect.Lists;
import com.luastar.swift.http.route.handlermapping.HandlerExecutionChain;
import com.luastar.swift.http.route.handlermapping.HandlerMapping;
import com.luastar.swift.http.route.handlermapping.RequestMappingHandlerMapping;
import com.luastar.swift.http.route.handlermapping.SimpleUrlHandlerMapping;
import com.luastar.swift.http.server.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 参考 spring mvc 实现的路由处理
 */
public class HttpHandlerMapping {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private List<HandlerMapping> handlerMappings;

    private HttpExceptionHandler exceptionHandler;

    public HttpHandlerMapping() {
        handlerMappings = Lists.newArrayList();
        handlerMappings.add(new RequestMappingHandlerMapping());
        handlerMappings.add(new SimpleUrlHandlerMapping());
    }

    public HttpExceptionHandler getExceptionHandler() {
        return exceptionHandler;
    }

    public void setExceptionHandler(HttpExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    /**
     * @param request
     * @return
     */
    public HandlerExecutionChain getHandler(HttpRequest request) throws Exception {
        for (HandlerMapping hm : this.handlerMappings) {
            HandlerExecutionChain handler = hm.getHandler(request);
            if (handler != null) {
                return handler;
            }
        }
        return null;
    }

}
