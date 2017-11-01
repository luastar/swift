package com.luastar.swift.http.route;


import com.luastar.swift.http.route.handlermapping.HandlerExecutionChain;
import com.luastar.swift.http.route.handlermapping.HandlerMapping;
import com.luastar.swift.http.route.handlermapping.RequestMappingHandlerMapping;
import com.luastar.swift.http.server.HttpRequest;
import com.luastar.swift.http.server.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.OrderComparator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 参考 spring mvc 实现的路由处理
 */
public class HttpHandlerMapping implements ApplicationContextAware {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private List<HandlerMapping> handlerMappings;

    private HttpExceptionHandler exceptionHandler;

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        initHandlerMappings(context);
    }

    public void setExceptionHandler(HttpExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    /**
     * Initialize the HandlerMappings used by this class.
     * <p>If no HandlerMapping beans are defined in the BeanFactory for this namespace,
     * we default to BeanNameUrlHandlerMapping.
     */
    protected void initHandlerMappings(ApplicationContext context) {
        this.handlerMappings = null;
        // Find all HandlerMappings in the ApplicationContext, including ancestor contexts.
        Map<String, HandlerMapping> matchingBeans = BeanFactoryUtils.beansOfTypeIncludingAncestors(context, HandlerMapping.class, true, false);
        if (!matchingBeans.isEmpty()) {
            this.handlerMappings = new ArrayList<HandlerMapping>(matchingBeans.values());
            // We keep HandlerMappings in sorted order.
            OrderComparator.sort(this.handlerMappings);
        }
        // Ensure we have at least one HandlerMapping, by registering
        // a default HandlerMapping if no other mappings are found.
        if (this.handlerMappings == null) {
            List<HandlerMapping> defaultHandlerMappings = new ArrayList<HandlerMapping>();
            HandlerMapping mapping = context.getAutowireCapableBeanFactory().createBean(RequestMappingHandlerMapping.class);
            defaultHandlerMappings.add(mapping);
            this.handlerMappings = defaultHandlerMappings;
            logger.info("No HandlerMappings found, using default");
        }
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

    /**
     * 异常业务处理
     *
     * @param request
     * @param response
     * @param exception
     */
    public void exceptionHandler(HttpRequest request, HttpResponse response, Exception exception) throws Exception {
        if (exceptionHandler == null) {
            throw exception;
        }
        exceptionHandler.exceptionHandle(request, response, exception);
    }

}
