package com.luastar.swift.http.route.handlermapping;

import com.luastar.swift.http.route.interceptor.HandlerInterceptor;
import com.luastar.swift.http.server.HttpRequest;
import com.luastar.swift.http.server.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Handler execution chain, consisting of handler object and any handler interceptors.
 * Returned by HandlerMapping's {@link HandlerMapping#getHandler} method.
 */
public class HandlerExecutionChain {

    private static final Logger logger = LoggerFactory.getLogger(HandlerExecutionChain.class);

    private final Object handler;

    private List<HandlerInterceptor> interceptorList;

    /**
     * Create a new HandlerExecutionChain.
     *
     * @param handler the handler object to execute
     */
    public HandlerExecutionChain(Object handler) {
        this(handler, null);
    }

    /**
     * Create a new HandlerExecutionChain.
     *
     * @param handler         the handler object to execute
     * @param interceptorList the arraylist of interceptors to apply
     *                        (in the given order) before the handler itself executes
     */
    public HandlerExecutionChain(Object handler, List<HandlerInterceptor> interceptorList) {
        this.handler = handler;
        this.interceptorList = interceptorList;
    }

    /**
     * Return the handler object to execute.
     *
     * @return the handler object
     */
    public Object getHandler() {
        return this.handler;
    }

    public void addInterceptor(HandlerInterceptor interceptor) {
        initInterceptorList();
        this.interceptorList.add(interceptor);
    }

    public void addInterceptorList(List<HandlerInterceptor> interceptorList) {
        if (interceptorList != null) {
            initInterceptorList();
            this.interceptorList.addAll(interceptorList);
        }
    }

    private void initInterceptorList() {
        if (this.interceptorList == null) {
            this.interceptorList = new ArrayList<HandlerInterceptor>();
        }
    }

    /**
     * Return the array of interceptors to apply (in the given order).
     *
     * @return the array of HandlerInterceptors instances (may be {@code null})
     */
    public List<HandlerInterceptor> getInterceptorList() {
        return this.interceptorList;
    }

    /**
     * Apply preHandle methods of registered interceptors.
     *
     * @return {@code true} if the execution chain should proceed with the
     * next interceptor or the handler itself. Else, DispatcherServlet assumes
     * that this interceptor has already dealt with the response itself.
     */
    public boolean applyPreHandle(HttpRequest request, HttpResponse response) throws Exception {
        if (CollectionUtils.isEmpty(this.interceptorList)) {
            return true;
        }
        int size = this.interceptorList.size();
        for (int i = 0; i < size; i++) {
            HandlerInterceptor interceptor = this.interceptorList.get(i);
            if (!interceptor.preHandle(request, response)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Apply postHandle methods of registered interceptors.
     */
    public void applyPostHandle(HttpRequest request, HttpResponse response) throws Exception {
        if (CollectionUtils.isEmpty(this.interceptorList)) {
            return;
        }
        int size = this.interceptorList.size();
        for (int i = size - 1; i >= 0; i--) {
            HandlerInterceptor interceptor = this.interceptorList.get(i);
            interceptor.postHandle(request, response);
        }
    }

    /**
     * Delegates to the handler's {@code toString()}.
     */
    @Override
    public String toString() {
        if (this.handler == null) {
            return "HandlerExecutionChain with no handler";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("HandlerExecutionChain with handler [").append(this.handler).append("]");
        if (!CollectionUtils.isEmpty(this.interceptorList)) {
            sb.append(" and ").append(this.interceptorList.size()).append(" interceptor");
            if (this.interceptorList.size() > 1) {
                sb.append("s");
            }
        }
        return sb.toString();
    }

}
