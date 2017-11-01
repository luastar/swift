package com.luastar.swift.http.route.handlermapping;

import com.luastar.swift.http.route.interceptor.MappedInterceptor;
import com.luastar.swift.http.server.HttpRequest;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.Assert;
import org.springframework.util.PathMatcher;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public abstract class AbstractHandlerMapping implements ApplicationContextAware, HandlerMapping {

    private ApplicationContext applicationContext;

    private Object defaultHandler;

    private PathMatcher pathMatcher = new AntPathMatcher();

    private final List<MappedInterceptor> mappedInterceptorList = new ArrayList<MappedInterceptor>();

    public final ApplicationContext getApplicationContext() throws IllegalStateException {
        return this.applicationContext;
    }

    @Override
    public final void setApplicationContext(ApplicationContext context) throws BeansException {
        this.applicationContext = context;
        initApplicationContext();
    }

    protected void initApplicationContext() throws BeansException {
    }

    public Object getDefaultHandler() {
        return this.defaultHandler;
    }

    public void setDefaultHandler(Object defaultHandler) {
        this.defaultHandler = defaultHandler;
    }

    public PathMatcher getPathMatcher() {
        return this.pathMatcher;
    }

    public void setPathMatcher(PathMatcher pathMatcher) {
        Assert.notNull(pathMatcher, "PathMatcher must not be null");
        this.pathMatcher = pathMatcher;
    }

    @Override
    public final HandlerExecutionChain getHandler(HttpRequest request) throws Exception {
        Object handler = getHandlerInternal(request);
        if (handler == null) {
            handler = getDefaultHandler();
        }
        if (handler == null) {
            return null;
        }
        // Bean name or resolved handler?
        if (handler instanceof String) {
            String handlerName = (String) handler;
            handler = getApplicationContext().getBean(handlerName);
        }
        return getHandlerExecutionChain(handler, request);
    }

    protected abstract Object getHandlerInternal(HttpRequest request) throws Exception;

    protected HandlerExecutionChain getHandlerExecutionChain(Object handler, HttpRequest request) {
        HandlerExecutionChain chain = (handler instanceof HandlerExecutionChain ? (HandlerExecutionChain) handler : new HandlerExecutionChain(handler));
        String lookupPath = request.getLookupPath();
        for (MappedInterceptor mappedInterceptor : this.mappedInterceptorList) {
            if (mappedInterceptor.matches(lookupPath, this.pathMatcher)) {
                chain.addInterceptor(mappedInterceptor.getInterceptor());
            }
        }
        return chain;
    }

}
