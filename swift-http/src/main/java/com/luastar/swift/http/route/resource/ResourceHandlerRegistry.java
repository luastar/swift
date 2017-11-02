package com.luastar.swift.http.route.resource;

import com.luastar.swift.http.route.HttpRequestHandler;
import com.luastar.swift.http.route.handlermapping.AbstractHandlerMapping;
import com.luastar.swift.http.route.handlermapping.SimpleUrlHandlerMapping;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;

import java.util.*;

/**
 *
 */
public class ResourceHandlerRegistry {

    private final ApplicationContext applicationContext;

    private final List<ResourceHandlerRegistration> registrations = new ArrayList<ResourceHandlerRegistration>();

    private int order = Integer.MAX_VALUE - 1;

    /**
     * Create a new resource handler registry for the given application context.
     *
     * @param applicationContext the Spring application context
     */
    public ResourceHandlerRegistry(ApplicationContext applicationContext) {
        Assert.notNull(applicationContext, "ApplicationContext is required");
        this.applicationContext = applicationContext;
    }

    /**
     * Add a resource handler for serving static resources based on the specified URL path
     * patterns. The handler will be invoked for every incoming request that matches to
     * one of the specified path patterns.
     *
     * @return A {@link ResourceHandlerRegistration} to use to further configure the
     * registered resource handler
     */
    public ResourceHandlerRegistration addResourceHandler(String... pathPatterns) {
        ResourceHandlerRegistration registration = new ResourceHandlerRegistration(this.applicationContext, pathPatterns);
        this.registrations.add(registration);
        return registration;
    }

    /**
     * Whether a resource handler has already been registered for the given path pattern.
     */
    public boolean hasMappingForPattern(String pathPattern) {
        for (ResourceHandlerRegistration registration : this.registrations) {
            if (Arrays.asList(registration.getPathPatterns()).contains(pathPattern)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Specify the order to use for resource handling relative to others
     * configured in the Spring MVC application context.
     * <p>The default value used is {@code Integer.MAX_VALUE-1}.
     */
    public ResourceHandlerRegistry setOrder(int order) {
        this.order = order;
        return this;
    }

    /**
     * Return a handler mapping with the mapped resource handlers; or {@code null} in case
     * of no registrations.
     */
    public AbstractHandlerMapping getHandlerMapping() {
        if (this.registrations.isEmpty()) {
            return null;
        }
        Map<String, HttpRequestHandler> urlMap = new LinkedHashMap<String, HttpRequestHandler>();
        for (ResourceHandlerRegistration registration : this.registrations) {
            for (String pathPattern : registration.getPathPatterns()) {
                ResourceHttpRequestHandler handler = registration.getRequestHandler();
                handler.setApplicationContext(this.applicationContext);
                try {
                    handler.afterPropertiesSet();
                } catch (Exception ex) {
                    throw new BeanInitializationException("Failed to init ResourceHttpRequestHandler", ex);
                }
                urlMap.put(pathPattern, handler);
            }
        }
        SimpleUrlHandlerMapping handlerMapping = new SimpleUrlHandlerMapping();
        handlerMapping.setOrder(order);
        handlerMapping.setUrlMap(urlMap);
        return handlerMapping;
    }

}
