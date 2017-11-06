package com.luastar.swift.http.route;


import com.luastar.swift.base.utils.CollectionUtils;
import com.luastar.swift.http.route.handlermapping.HandlerExecutionChain;
import com.luastar.swift.http.route.handlermapping.HandlerMapping;
import com.luastar.swift.http.route.handlermapping.RequestMappingHandlerMapping;
import com.luastar.swift.http.route.handlermapping.SimpleUrlHandlerMapping;
import com.luastar.swift.http.server.HttpRequest;

import java.util.List;

/**
 * 参考 spring mvc 实现的路由处理
 */
public class HttpHandlerMapping extends RequestMappingHandlerMapping {

    private HttpExceptionHandler exceptionHandler;

    private List<SimpleUrlHandlerMapping> simpleUrlHandlerMappingList;

    public void setExceptionHandler(HttpExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    public HttpExceptionHandler getExceptionHandler() {
        return exceptionHandler;
    }

    public void setSimpleUrlHandlerMappingList(List<SimpleUrlHandlerMapping> simpleUrlHandlerMappingList) {
        this.simpleUrlHandlerMappingList = simpleUrlHandlerMappingList;
    }

    public HandlerExecutionChain getActualHandler(HttpRequest request) throws Exception {
        // 先使用默认方法匹配
        HandlerExecutionChain handler = getHandler(request);
        if (handler != null) {
            return handler;
        }
        // 再使用静态资源匹配
        if (CollectionUtils.isNotEmpty(simpleUrlHandlerMappingList)) {
            for (HandlerMapping handlerMapping : this.simpleUrlHandlerMappingList) {
                handler = handlerMapping.getHandler(request);
                if (handler != null) {
                    return handler;
                }
            }
        }
        return null;
    }

}
