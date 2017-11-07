package com.luastar.swift.http.route.handlermapping;

import com.luastar.swift.http.route.resource.CacheControl;
import com.luastar.swift.http.route.resource.ResourceHttpRequestHandler;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class SimpleUrlHandlerMapping extends AbstractUrlHandlerMapping implements InitializingBean {

    private final Map<String, Object> urlMap = new HashMap<String, Object>();

    /**
     * 新增的映射配置
     */
    private String mapping;
    private String locations;
    private CacheControl cacheControl;

    public SimpleUrlHandlerMapping() {
        setOrder(99);
    }

    /**
     * Set a Map with URL paths as keys and handler beans (or handler bean names)
     * as values. Convenient for population with bean references.
     * <p>Supports direct URL matches and Ant-style pattern matches. For syntax
     * details, see the {@link org.springframework.util.AntPathMatcher} javadoc.
     *
     * @param urlMap map with URLs as keys and beans as values
     */
    public void setUrlMap(Map<String, ?> urlMap) {
        this.urlMap.putAll(urlMap);
    }

    /**
     * Allow Map access to the URL path mappings, with the option to add or
     * override specific entries.
     * <p>Useful for specifying entries directly, for example via "urlMap[myKey]".
     * This is particularly useful for adding or overriding entries in child
     * bean definitions.
     */
    public Map<String, ?> getUrlMap() {
        return this.urlMap;
    }

    public void setMapping(String mapping) {
        this.mapping = mapping;
    }

    public void setLocations(String locations) {
        this.locations = locations;
    }

    public void setCacheControl(CacheControl cacheControl) {
        this.cacheControl = cacheControl;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (!StringUtils.isEmpty(this.mapping) && !StringUtils.isEmpty(this.locations)) {
            ResourceHttpRequestHandler handler = new ResourceHttpRequestHandler();
            String[] locationAry = StringUtils.commaDelimitedListToStringArray(this.locations);
            List<Resource> locations = new ArrayList<Resource>();
            for (String location : locationAry) {
                locations.add(getApplicationContext().getResource(location));
            }
            handler.setLocations(locations);
            if (this.cacheControl != null) {
                handler.setCacheControl(this.cacheControl);
            } else {
                handler.setCacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS).cachePrivate());
            }
            urlMap.put(this.mapping, handler);
        }
        registerHandlers(this.urlMap);
    }

    /**
     * Register all handlers specified in the URL map for the corresponding paths.
     *
     * @param urlMap Map with URL paths as keys and handler beans or bean names as values
     * @throws BeansException        if a handler couldn't be registered
     * @throws IllegalStateException if there is a conflicting handler registered
     */
    protected void registerHandlers(Map<String, Object> urlMap) throws BeansException {
        if (urlMap.isEmpty()) {
            logger.warn("Neither 'urlMap' nor 'mappings' set on SimpleUrlHandlerMapping");
            return;
        }
        for (Map.Entry<String, Object> entry : urlMap.entrySet()) {
            String url = entry.getKey();
            Object handler = entry.getValue();
            // Prepend with slash if not already present.
            if (!url.startsWith("/")) {
                url = "/" + url;
            }
            // Remove whitespace from handler bean name.
            if (handler instanceof String) {
                handler = ((String) handler).trim();
            }
            registerHandler(url, handler);
        }
    }

}
