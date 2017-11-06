package com.luastar.swift.http.route.handlermapping;

import com.luastar.swift.http.constant.HttpConstant;
import com.luastar.swift.http.route.interceptor.HandlerInterceptorAdapter;
import com.luastar.swift.http.server.HttpRequest;
import com.luastar.swift.http.server.HttpResponse;
import org.springframework.beans.BeansException;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 *
 */
public class AbstractUrlHandlerMapping extends AbstractHandlerMapping {

    private Object rootHandler;

    private boolean lazyInitHandlers = false;

    private final Map<String, Object> handlerMap = new LinkedHashMap<String, Object>();

    /**
     * Set the root handler for this handler mapping, that is,
     * the handler to be registered for the root path ("/").
     * <p>Default is {@code null}, indicating no root handler.
     */
    public void setRootHandler(Object rootHandler) {
        this.rootHandler = rootHandler;
    }

    /**
     * Return the root handler for this handler mapping (registered for "/"),
     * or {@code null} if none.
     */
    public Object getRootHandler() {
        return this.rootHandler;
    }

    /**
     * Set whether to lazily initialize handlers. Only applicable to
     * singleton handlers, as prototypes are always lazily initialized.
     * Default is "false", as eager initialization allows for more efficiency
     * through referencing the controller objects directly.
     * <p>If you want to allow your controllers to be lazily initialized,
     * make them "lazy-init" and set this flag to true. Just making them
     * "lazy-init" will not work, as they are initialized through the
     * references from the handler mapping in this case.
     */
    public void setLazyInitHandlers(boolean lazyInitHandlers) {
        this.lazyInitHandlers = lazyInitHandlers;
    }

    /**
     * Look up a handler for the URL path of the given request.
     *
     * @param request current HTTP request
     * @return the handler instance, or {@code null} if none found
     */
    @Override
    protected Object getHandlerInternal(HttpRequest request) throws Exception {
        String lookupPath = request.getLookupPath();
        Object handler = lookupHandler(lookupPath, request);
        if (handler == null) {
            // We need to care for the default handler directly, since we need to
            // expose the PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE for it as well.
            Object rawHandler = null;
            if ("/".equals(lookupPath)) {
                rawHandler = getRootHandler();
            }
            if (rawHandler == null) {
                rawHandler = getDefaultHandler();
            }
            if (rawHandler != null) {
                // Bean name or resolved handler?
                if (rawHandler instanceof String) {
                    String handlerName = (String) rawHandler;
                    rawHandler = getApplicationContext().getBean(handlerName);
                }
                validateHandler(rawHandler, request);
                handler = buildPathExposingHandler(rawHandler, lookupPath, lookupPath, null);
            }
        }
        if (handler == null) {
            logger.info("No handler mapping found for [{}]", lookupPath);
        } else {
            logger.info("Mapping [{}] to {}", lookupPath, handler);
        }
        return handler;
    }

    /**
     * Look up a handler instance for the given URL path.
     * <p>Supports direct matches, e.g. a registered "/test" matches "/test",
     * and various Ant-style pattern matches, e.g. a registered "/t*" matches
     * both "/test" and "/team". For details, see the AntPathMatcher class.
     * <p>Looks for the most exact pattern, where most exact is defined as
     * the longest path pattern.
     *
     * @param urlPath URL the bean is mapped to
     * @param request current HTTP request (to expose the path within the mapping to)
     * @return the associated handler instance, or {@code null} if not found
     * @see #exposePathWithinMapping
     * @see org.springframework.util.AntPathMatcher
     */
    protected Object lookupHandler(String urlPath, HttpRequest request) throws Exception {
        // Direct match?
        Object handler = this.handlerMap.get(urlPath);
        if (handler != null) {
            // Bean name or resolved handler?
            if (handler instanceof String) {
                String handlerName = (String) handler;
                handler = getApplicationContext().getBean(handlerName);
            }
            validateHandler(handler, request);
            return buildPathExposingHandler(handler, urlPath, urlPath, null);
        }
        // Pattern match?
        List<String> matchingPatterns = new ArrayList<String>();
        for (String registeredPattern : this.handlerMap.keySet()) {
            if (getPathMatcher().match(registeredPattern, urlPath)) {
                matchingPatterns.add(registeredPattern);
            }
        }
        String bestPatternMatch = null;
        Comparator<String> patternComparator = getPathMatcher().getPatternComparator(urlPath);
        if (!matchingPatterns.isEmpty()) {
            Collections.sort(matchingPatterns, patternComparator);
            logger.debug("Matching patterns for request [{}] are {}", urlPath, matchingPatterns);
            bestPatternMatch = matchingPatterns.get(0);
        }
        if (bestPatternMatch != null) {
            handler = this.handlerMap.get(bestPatternMatch);
            // Bean name or resolved handler?
            if (handler instanceof String) {
                String handlerName = (String) handler;
                handler = getApplicationContext().getBean(handlerName);
            }
            validateHandler(handler, request);
            String pathWithinMapping = getPathMatcher().extractPathWithinPattern(bestPatternMatch, urlPath);

            // There might be multiple 'best patterns', let's make sure we have the correct URI template variables
            // for all of them
            Map<String, String> uriTemplateVariables = new LinkedHashMap<String, String>();
            for (String matchingPattern : matchingPatterns) {
                if (patternComparator.compare(bestPatternMatch, matchingPattern) == 0) {
                    Map<String, String> vars = getPathMatcher().extractUriTemplateVariables(matchingPattern, urlPath);
                    uriTemplateVariables.putAll(vars);
                }
            }
            logger.debug("URI Template variables for request [{}] are {}", urlPath, uriTemplateVariables);
            return buildPathExposingHandler(handler, bestPatternMatch, pathWithinMapping, uriTemplateVariables);
        }
        // No handler found...
        return null;
    }

    /**
     * Validate the given handler against the current request.
     * <p>The default implementation is empty. Can be overridden in subclasses,
     * for example to enforce specific preconditions expressed in URL mappings.
     *
     * @param handler the handler object to validate
     * @param request current HTTP request
     * @throws Exception if validation failed
     */
    protected void validateHandler(Object handler, HttpRequest request) throws Exception {
    }

    /**
     * Build a handler object for the given raw handler, exposing the actual
     * handler, the {@link HttpConstant#PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE}, as well as
     * the {@link HttpConstant#URI_TEMPLATE_VARIABLES_ATTRIBUTE} before executing the handler.
     * <p>The default implementation builds a {@link HandlerExecutionChain}
     * with a special interceptor that exposes the path attribute and uri template variables
     *
     * @param rawHandler           the raw handler to expose
     * @param pathWithinMapping    the path to expose before executing the handler
     * @param uriTemplateVariables the URI template variables, can be {@code null} if no variables found
     * @return the final handler object
     */
    protected Object buildPathExposingHandler(Object rawHandler,
                                              String bestMatchingPattern,
                                              String pathWithinMapping, Map<String, String> uriTemplateVariables) {

        HandlerExecutionChain chain = new HandlerExecutionChain(rawHandler);
        chain.addInterceptor(new PathExposingHandlerInterceptor(bestMatchingPattern, pathWithinMapping));
        if (!CollectionUtils.isEmpty(uriTemplateVariables)) {
            chain.addInterceptor(new UriTemplateVariablesHandlerInterceptor(uriTemplateVariables));
        }
        return chain;
    }

    /**
     * Expose the path within the current mapping as request attribute.
     *
     * @param pathWithinMapping the path within the current mapping
     * @param request           the request to expose the path to
     * @see HttpConstant#PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE
     */
    protected void exposePathWithinMapping(String bestMatchingPattern, String pathWithinMapping, HttpRequest request) {
        request.setAttribute(HttpConstant.BEST_MATCHING_PATTERN_ATTRIBUTE, bestMatchingPattern);
        request.setAttribute(HttpConstant.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE, pathWithinMapping);
    }

    /**
     * Expose the URI templates variables as request attribute.
     *
     * @param uriTemplateVariables the URI template variables
     * @param request              the request to expose the path to
     * @see HttpConstant#PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE
     */
    protected void exposeUriTemplateVariables(Map<String, String> uriTemplateVariables, HttpRequest request) {
        request.setAttribute(HttpConstant.URI_TEMPLATE_VARIABLES_ATTRIBUTE, uriTemplateVariables);
    }

    /**
     * Register the specified handler for the given URL paths.
     *
     * @param urlPaths the URLs that the bean should be mapped to
     * @param beanName the name of the handler bean
     * @throws BeansException        if the handler couldn't be registered
     * @throws IllegalStateException if there is a conflicting handler registered
     */
    protected void registerHandler(String[] urlPaths, String beanName) throws BeansException, IllegalStateException {
        Assert.notNull(urlPaths, "URL path array must not be null");
        for (String urlPath : urlPaths) {
            registerHandler(urlPath, beanName);
        }
    }

    /**
     * Register the specified handler for the given URL path.
     *
     * @param urlPath the URL the bean should be mapped to
     * @param handler the handler instance or handler bean name String
     *                (a bean name will automatically be resolved into the corresponding handler bean)
     * @throws BeansException        if the handler couldn't be registered
     * @throws IllegalStateException if there is a conflicting handler registered
     */
    protected void registerHandler(String urlPath, Object handler) throws BeansException, IllegalStateException {
        Assert.notNull(urlPath, "URL path must not be null");
        Assert.notNull(handler, "Handler object must not be null");
        Object resolvedHandler = handler;
        // Eagerly resolve handler if referencing singleton via name.
        if (!this.lazyInitHandlers && handler instanceof String) {
            String handlerName = (String) handler;
            if (getApplicationContext().isSingleton(handlerName)) {
                resolvedHandler = getApplicationContext().getBean(handlerName);
            }
        }
        Object mappedHandler = this.handlerMap.get(urlPath);
        if (mappedHandler != null) {
            if (mappedHandler != resolvedHandler) {
                StringBuffer msg = new StringBuffer("Cannot map ")
                        .append(getHandlerDescription(handler))
                        .append(" to URL path [")
                        .append(urlPath)
                        .append("]: There is already ")
                        .append(getHandlerDescription(mappedHandler))
                        .append(" mapped.");
                throw new IllegalStateException(msg.toString());
            }
        } else {
            if (urlPath.equals("/")) {
                logger.info("Root mapping to " + getHandlerDescription(handler));
                setRootHandler(resolvedHandler);
            } else if (urlPath.equals("/*")) {
                logger.info("Default mapping to " + getHandlerDescription(handler));
                setDefaultHandler(resolvedHandler);
            } else {
                this.handlerMap.put(urlPath, resolvedHandler);
                logger.info("Mapped URL path [{}] onto {}", urlPath, getHandlerDescription(handler));
            }
        }
    }

    private String getHandlerDescription(Object handler) {
        StringBuffer desc = new StringBuffer("handler ");
        if (handler instanceof String) {
            desc.append("'").append(handler).append("'");
        } else {
            desc.append("of type [").append(handler.getClass()).append("]");
        }
        return desc.toString();
    }


    /**
     * Return the registered handlers as an unmodifiable Map, with the registered path
     * as key and the handler object (or handler bean name in case of a lazy-init handler)
     * as value.
     *
     * @see #getDefaultHandler()
     */
    public final Map<String, Object> getHandlerMap() {
        return Collections.unmodifiableMap(this.handlerMap);
    }

    /**
     * Indicates whether this handler mapping support type-level mappings. Default to {@code false}.
     */
    protected boolean supportsTypeLevelMappings() {
        return false;
    }


    /**
     * Special interceptor for exposing the
     * {@link HttpConstant#PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE} attribute.
     *
     * @see AbstractUrlHandlerMapping#exposePathWithinMapping
     */
    private class PathExposingHandlerInterceptor extends HandlerInterceptorAdapter {

        private final String bestMatchingPattern;

        private final String pathWithinMapping;

        public PathExposingHandlerInterceptor(String bestMatchingPattern, String pathWithinMapping) {
            this.bestMatchingPattern = bestMatchingPattern;
            this.pathWithinMapping = pathWithinMapping;
        }

        @Override
        public boolean preHandle(HttpRequest request, HttpResponse response) {
            exposePathWithinMapping(this.bestMatchingPattern, this.pathWithinMapping, request);
            request.setAttribute(HttpConstant.INTROSPECT_TYPE_LEVEL_MAPPING, supportsTypeLevelMappings());
            return true;
        }

    }

    /**
     * Special interceptor for exposing the
     * {@link HttpConstant#URI_TEMPLATE_VARIABLES_ATTRIBUTE} attribute.
     *
     * @see AbstractUrlHandlerMapping#exposePathWithinMapping
     */
    private class UriTemplateVariablesHandlerInterceptor extends HandlerInterceptorAdapter {

        private final Map<String, String> uriTemplateVariables;

        public UriTemplateVariablesHandlerInterceptor(Map<String, String> uriTemplateVariables) {
            this.uriTemplateVariables = uriTemplateVariables;
        }

        @Override
        public boolean preHandle(HttpRequest request, HttpResponse response) {
            exposeUriTemplateVariables(this.uriTemplateVariables, request);
            return true;
        }

    }

}
