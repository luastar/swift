package com.luastar.swift.http.route;


import com.google.common.collect.Lists;
import com.luastar.swift.base.utils.CollectionUtils;
import com.luastar.swift.http.route.handlermapping.HandlerExecutionChain;
import com.luastar.swift.http.route.handlermapping.HandlerMapping;
import com.luastar.swift.http.route.handlermapping.RequestMappingHandlerMapping;
import com.luastar.swift.http.route.handlermapping.SimpleUrlHandlerMapping;
import com.luastar.swift.http.server.HttpRequest;
import org.springframework.core.OrderComparator;

import java.util.List;

/**
 * 参考 spring mvc 实现的路由处理
 */
public class HttpHandlerMapping extends RequestMappingHandlerMapping {

    private HttpExceptionHandler exceptionHandler;

    private List<SimpleUrlHandlerMapping> simpleUrlHandlerMappingList;

    private List<HandlerMapping> handlerMappingList;

    public void setExceptionHandler(HttpExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    public HttpExceptionHandler getExceptionHandler() {
        return exceptionHandler;
    }

    public void setSimpleUrlHandlerMappingList(List<SimpleUrlHandlerMapping> simpleUrlHandlerMappingList) {
        this.simpleUrlHandlerMappingList = simpleUrlHandlerMappingList;
    }

    public HttpHandlerMapping() {
        setOrder(9999);
    }

    @Override
    public void afterPropertiesSet() {
        super.afterPropertiesSet();
        this.handlerMappingList = Lists.newArrayList();
        this.handlerMappingList.add(this);
        if (CollectionUtils.isNotEmpty(simpleUrlHandlerMappingList)){
            this.handlerMappingList.addAll(simpleUrlHandlerMappingList);
        }
        OrderComparator.sort(this.handlerMappingList);
    }

    public HandlerExecutionChain getActualHandler(HttpRequest request) throws Exception {
        for (HandlerMapping handlerMapping : this.handlerMappingList) {
            HandlerExecutionChain handler = handlerMapping.getHandler(request);
            if (handler != null) {
                return handler;
            }
        }
        return null;
    }

}
