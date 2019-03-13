package com.luastar.swift.http.route;


import com.google.common.collect.Lists;
import com.luastar.swift.base.utils.CollectionUtils;
import com.luastar.swift.http.route.handlermapping.HandlerExecutionChain;
import com.luastar.swift.http.route.handlermapping.HandlerMapping;
import com.luastar.swift.http.route.handlermapping.RequestMappingHandlerMapping;
import com.luastar.swift.http.route.handlermapping.SimpleUrlHandlerMapping;
import com.luastar.swift.http.server.HttpRequest;
import com.luastar.swift.http.server.HttpResponse;
import org.springframework.core.OrderComparator;

import java.util.List;

/**
 * 参考 spring mvc 实现的路由处理
 */
public class HttpHandlerMapping extends RequestMappingHandlerMapping {

    private HttpExceptionHandler exceptionHandler;

    private List<SimpleUrlHandlerMapping> simpleUrlHandlerMappingList;

    private List<HandlerMapping> handlerMappingList;

    public HttpHandlerMapping() {
        this.handlerMappingList = Lists.newArrayList();
        setOrder(9999);
    }

    public void setExceptionHandler(HttpExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    public void setSimpleUrlHandlerMappingList(List<SimpleUrlHandlerMapping> simpleUrlHandlerMappingList) {
        this.simpleUrlHandlerMappingList = simpleUrlHandlerMappingList;
    }

    @Override
    public void afterPropertiesSet() {
        super.afterPropertiesSet();
        this.handlerMappingList.add(this);
        if (CollectionUtils.isNotEmpty(simpleUrlHandlerMappingList)) {
            this.handlerMappingList.addAll(simpleUrlHandlerMappingList);
        }
        OrderComparator.sort(this.handlerMappingList);
    }

    /**
     * 获取实际处理器
     *
     * @param request
     * @return
     * @throws Exception
     */
    public HandlerExecutionChain getActualHandler(HttpRequest request) throws Exception {
        for (HandlerMapping handlerMapping : this.handlerMappingList) {
            HandlerExecutionChain handler = handlerMapping.getHandler(request);
            if (handler != null) {
                return handler;
            }
        }
        return null;
    }

    /**
     * 异常处理
     * @param request
     * @param httpResponse
     * @param e
     * @throws Throwable
     */
    public void exceptionHandle(HttpRequest request, HttpResponse httpResponse, Throwable e) throws Throwable {
        if (exceptionHandler == null) {
            throw e;
        }
        exceptionHandler.exceptionHandle(request, httpResponse, e);
    }

}
