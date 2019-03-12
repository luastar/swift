package com.luastar.swift.http.route;


import com.luastar.swift.http.constant.HttpConstant;
import com.luastar.swift.http.server.HttpRequest;
import com.luastar.swift.http.server.HttpResponse;
import com.luastar.swift.http.server.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.*;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;

/**
 * 参考 spring mvc 实现的路由处理
 */
public class HttpHandlerMapping implements ApplicationContextAware, InitializingBean, HandlerMapping {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private ApplicationContext applicationContext;

    private HttpRequestHandler defaultHandler;

    private HttpExceptionHandler exceptionHandler;

    private PathMatcher pathMatcher = new AntPathMatcher();

    private final Map<RequestMappingInfo, HandlerMethod> handlerMethods = new LinkedHashMap<>();

    private final MultiValueMap<String, RequestMappingInfo> urlMap = new LinkedMultiValueMap<>();

    private final List<MappedInterceptor> mappedInterceptorList = new ArrayList<>();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public void setDefaultHandler(HttpRequestHandler defaultHandler) {
        this.defaultHandler = defaultHandler;
    }

    public void setExceptionHandler(HttpExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initInterceptors();
        initHandlerMethods();
    }

    /**
     * Initialize the specified interceptors, checking for {@link MappedInterceptor}s if necessary.
     */
    protected void initInterceptors() {
        Map<String, MappedInterceptor> mappedInterceptorMap = BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext, MappedInterceptor.class, true, false);
        mappedInterceptorList.addAll(mappedInterceptorMap.values());
        for (MappedInterceptor interceptor : mappedInterceptorList) {
            logger.info("init interceptor {}, includePatterns={}, excludePatterns={}", interceptor.getInterceptor().getClass().getName(), interceptor.getIncludePatterns(), interceptor.getExcludePatterns());
        }
    }

    /**
     * Scan beans in the ApplicationContext, detect and register handler methods.
     * @see #getMappingForMethod(Method, Class)
     */
    protected void initHandlerMethods() {
        Map<String, Object> beanMap = applicationContext.getBeansWithAnnotation(HttpService.class);
        if (beanMap != null) {
            for (Map.Entry<String, Object> bean : beanMap.entrySet()) {
                detectHandlerMethods(bean.getValue());
            }
        }
    }

    /**
     * Look for handler methods in a handler.
     *
     * @param handler the bean name of a handler or a handler instance
     */
    protected void detectHandlerMethods(final Object handler) {
        Class<?> handlerType = (handler instanceof String ? applicationContext.getType((String) handler) : handler.getClass());
        // Avoid repeated calls to getMappingForMethod which would rebuild RequestMatchingInfo instances
        final Map<Method, RequestMappingInfo> mappings = new IdentityHashMap<>();
        final Class<?> userType = ClassUtils.getUserClass(handlerType);
        Set<Method> methods = selectMethods(userType, new ReflectionUtils.MethodFilter() {
            @Override
            public boolean matches(Method method) {
                RequestMappingInfo mapping = getMappingForMethod(method, userType);
                if (mapping == null) {
                    return false;
                }
                Class<?>[] methodParameterTypes = method.getParameterTypes();
                if (methodParameterTypes == null
                        || methodParameterTypes.length != 2
                        || !methodParameterTypes[0].equals(HttpRequest.class)
                        || !methodParameterTypes[1].equals(HttpResponse.class)) {
                    logger.warn("警告：{}[{}]参数不合法，忽略。", userType.getName(), method.getName());
                    return false;
                }
                mappings.put(method, mapping);
                return true;
            }
        });
        for (Method method : methods) {
            registerHandlerMethod(handler, method, mappings.get(method));
        }
    }

    /**
     * Select handler methods for the given handler type.
     * <p>Callers define handler methods of interest through the {@link ReflectionUtils.MethodFilter} parameter.
     *
     * @param handlerType         the handler type to search handler methods on
     * @param handlerMethodFilter a {@link ReflectionUtils.MethodFilter} to help recognize handler methods of interest
     * @return the selected methods, or an empty set
     */
    public static Set<Method> selectMethods(final Class<?> handlerType, final ReflectionUtils.MethodFilter handlerMethodFilter) {
        final Set<Method> handlerMethods = new LinkedHashSet<Method>();
        Set<Class<?>> handlerTypes = new LinkedHashSet<Class<?>>();
        Class<?> specificHandlerType = null;
        if (!Proxy.isProxyClass(handlerType)) {
            handlerTypes.add(handlerType);
            specificHandlerType = handlerType;
        }
        handlerTypes.addAll(Arrays.asList(handlerType.getInterfaces()));
        for (Class<?> currentHandlerType : handlerTypes) {
            final Class<?> targetClass = (specificHandlerType != null ? specificHandlerType : currentHandlerType);
            ReflectionUtils.doWithMethods(currentHandlerType, new ReflectionUtils.MethodCallback() {
                @Override
                public void doWith(Method method) {
                    Method specificMethod = ClassUtils.getMostSpecificMethod(method, targetClass);
                    Method bridgedMethod = BridgeMethodResolver.findBridgedMethod(specificMethod);
                    if (handlerMethodFilter.matches(specificMethod) &&
                            (bridgedMethod == specificMethod || !handlerMethodFilter.matches(bridgedMethod))) {
                        handlerMethods.add(specificMethod);
                    }
                }
            }, ReflectionUtils.USER_DECLARED_METHODS);
        }
        return handlerMethods;
    }

    /**
     * Provide the mapping for a handler method. A method for which no
     * mapping can be provided is not a handler method.
     *
     * @param method      the method to provide a mapping for
     * @param handlerType the handler type, possibly a sub-type of the method's
     *                    declaring class
     * @return the mapping, or {@code null} if the method is not mapped
     */
    protected RequestMappingInfo getMappingForMethod(Method method, Class<?> handlerType) {
        RequestMappingInfo info = null;
        HttpService methodAnnotation = AnnotationUtils.findAnnotation(method, HttpService.class);
        if (methodAnnotation != null) {
            info = createRequestMappingInfo(methodAnnotation);
            HttpService typeAnnotation = AnnotationUtils.findAnnotation(handlerType, HttpService.class);
            if (typeAnnotation != null) {
                info = createRequestMappingInfo(typeAnnotation).combine(info);
            }
        }
        return info;
    }

    /**
     * Created a RequestMappingInfo from a RequestMapping annotation.
     */
    protected RequestMappingInfo createRequestMappingInfo(HttpService annotation) {
        return new RequestMappingInfo(new PatternsRequestCondition(annotation.value(), pathMatcher, true, true), new RequestMethodsRequestCondition(annotation.method()));
    }

    /**
     * Register a handler method and its unique mapping.
     *
     * @param handler the bean name of the handler or the handler instance
     * @param method  the method to register
     * @param mapping the mapping conditions associated with the handler method
     * @throws IllegalStateException if another method was already registered
     *                               under the same mapping
     */
    protected void registerHandlerMethod(Object handler, Method method, RequestMappingInfo mapping) {
        HandlerMethod newHandlerMethod = createHandlerMethod(handler, method);
        HandlerMethod oldHandlerMethod = this.handlerMethods.get(mapping);
        if (oldHandlerMethod != null && !oldHandlerMethod.equals(newHandlerMethod)) {
            StringBuilder msg = new StringBuilder();
            msg.append("Ambiguous(不明确的) mapping found. Cannot map '")
                    .append(newHandlerMethod.getBean())
                    .append("' bean method \n")
                    .append(newHandlerMethod)
                    .append("\nto ")
                    .append(mapping)
                    .append(": There is already '")
                    .append(oldHandlerMethod.getBean())
                    .append("' bean method\n")
                    .append(oldHandlerMethod)
                    .append(" mapped.");
            throw new IllegalStateException(msg.toString());
        }
        this.handlerMethods.put(mapping, newHandlerMethod);
        Set<String> patterns = mapping.getPatternsCondition().getPatterns();
        for (String pattern : patterns) {
            if (!pathMatcher.isPattern(pattern)) {
                logger.info("url pattern={}, mapping={}", pattern, mapping.toString());
                this.urlMap.add(pattern, mapping);
            }
        }
    }

    /**
     * Create the HandlerMethod instance.
     *
     * @param handler either a bean name or an actual handler instance
     * @param method  the target method
     * @return the created HandlerMethod
     */
    protected HandlerMethod createHandlerMethod(Object handler, Method method) {
        HandlerMethod handlerMethod;
        if (handler instanceof String) {
            String beanName = (String) handler;
            handlerMethod = new HandlerMethod(beanName, applicationContext, method);
        } else {
            handlerMethod = new HandlerMethod(handler, method);
        }
        return handlerMethod;
    }

    @Override
    public HandlerExecutionChain getHandler(HttpRequest request) throws Exception {
        Object handler = getHandlerInternal(request);
        if (handler == null) {
            handler = defaultHandler;
        }
        if (handler == null){
            return null;
        }
        return getHandlerExecutionChain(handler, request);
    }

    /**
     * Look up a handler for the given request, returning {@code null} if no
     * specific one is found. This method is called by {@link #getHandler};
     * a {@code null} return value will lead to the default handler, if one is set.
     * <p>Note: This method may also return a pre-built {@link HandlerExecutionChain},
     * combining a handler object with dynamically determined interceptors.
     * Statically specified interceptors will get merged into such an existing chain.
     *
     * @param request current HTTP request
     * @return the corresponding handler instance, or {@code null} if none found
     * @throws Exception if there is an internal error
     */
    protected HandlerMethod getHandlerInternal(HttpRequest request) throws Exception {
        String lookupPath = request.getLookupPath();
        HandlerMethod handlerMethod = lookupHandlerMethod(lookupPath, request);
        if (handlerMethod != null) {
            logger.info("Returning handler method [{}]", handlerMethod);
        } else {
            logger.info("Did not find handler method for [{}][{}]", lookupPath, request.getMethod());
        }
        return (handlerMethod != null ? handlerMethod.createWithResolvedBean() : null);
    }

    /**
     * Look up the best-matching handler method for the current request.
     * If multiple matches are found, the best match is selected.
     *
     * @param lookupPath mapping lookup path within the current servlet mapping
     * @param request    the current request
     * @return the best-matching handler method, or {@code null} if no match
     * @see #handleMatch(RequestMappingInfo, String, HttpRequest)
     * @see #handleNoMatch(Set, String, HttpRequest)
     */
    protected HandlerMethod lookupHandlerMethod(String lookupPath, HttpRequest request) throws Exception {
        List<Match> matches = new ArrayList<Match>();
        List<RequestMappingInfo> directPathMatches = this.urlMap.get(lookupPath);
        if (directPathMatches != null) {
            addMatchingMappings(directPathMatches, matches, request);
        }
        if (matches.isEmpty()) {
            // No choice but to go through all mappings...
            addMatchingMappings(this.handlerMethods.keySet(), matches, request);
        }
        if (!matches.isEmpty()) {
            Comparator<Match> comparator = new MatchComparator(getMappingComparator(request));
            Collections.sort(matches, comparator);
            logger.info("Found {} matching mapping(s) for [{}] : {}", matches.size(), lookupPath, matches);
            Match bestMatch = matches.get(0);
            if (matches.size() > 1) {
                Match secondBestMatch = matches.get(1);
                if (comparator.compare(bestMatch, secondBestMatch) == 0) {
                    Method m1 = bestMatch.handlerMethod.getMethod();
                    Method m2 = secondBestMatch.handlerMethod.getMethod();
                    StringBuilder msg = new StringBuilder();
                    msg.append("Ambiguous handler methods mapped for HTTP path '")
                            .append(request.getUri())
                            .append("': {")
                            .append(m1).append(", ").append(m2)
                            .append("}");
                    throw new IllegalStateException(msg.toString());
                }
            }
            handleMatch(bestMatch.mapping, lookupPath, request);
            return bestMatch.handlerMethod;
        } else {
            return handleNoMatch(handlerMethods.keySet(), lookupPath, request);
        }
    }

    private void addMatchingMappings(Collection<RequestMappingInfo> mappings, List<Match> matches, HttpRequest request) {
        for (RequestMappingInfo mapping : mappings) {
            RequestMappingInfo match = mapping.getMatchingCondition(request);
            if (match != null) {
                matches.add(new Match(match, this.handlerMethods.get(mapping)));
            }
        }
    }

    /**
     * Return a comparator for sorting matching mappings.
     * The returned comparator should sort 'better' matches higher.
     *
     * @param request the current request
     * @return the comparator, never {@code null}
     */
    protected Comparator<RequestMappingInfo> getMappingComparator(final HttpRequest request) {
        return new Comparator<RequestMappingInfo>() {
            @Override
            public int compare(RequestMappingInfo info1, RequestMappingInfo info2) {
                return info1.compareTo(info2, request);
            }
        };
    }

    /**
     * Invoked when a matching mapping is found.
     *
     * @param mapping    the matching mapping
     * @param lookupPath mapping lookup path within the current servlet mapping
     * @param request    the current request
     */
    protected void handleMatch(RequestMappingInfo mapping, String lookupPath, HttpRequest request) {
        request.setAttribute(HttpConstant.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE, lookupPath);
        String bestPattern;
        Map<String, String> uriVariables;
        Set<String> patterns = mapping.getPatternsCondition().getPatterns();
        if (patterns.isEmpty()) {
            bestPattern = lookupPath;
            uriVariables = Collections.emptyMap();
        } else {
            bestPattern = patterns.iterator().next();
            uriVariables = pathMatcher.extractUriTemplateVariables(bestPattern, lookupPath);
        }
        request.setAttribute(HttpConstant.BEST_MATCHING_PATTERN_ATTRIBUTE, bestPattern);
        request.setAttribute(HttpConstant.URI_TEMPLATE_VARIABLES_ATTRIBUTE, uriVariables);
    }

    /**
     * Invoked when no matching mapping is not found.
     *
     * @param mappings   all registered mappings
     * @param lookupPath mapping lookup path within the current servlet mapping
     * @param request    the current request
     */
    protected HandlerMethod handleNoMatch(Set<RequestMappingInfo> mappings, String lookupPath, HttpRequest request) throws Exception {
        return null;
    }

    /**
     * Build a {@link HandlerExecutionChain} for the given handler, including
     * applicable interceptors.
     * <p>The default implementation builds a standard {@link HandlerExecutionChain}
     * with the given handler, the handler mapping's common interceptors, and any
     * {@link MappedInterceptor}s matching to the current request URL. Subclasses
     * may override this in order to extend/rearrange the list of interceptors.
     * <p><b>NOTE:</b> The passed-in handler object may be a raw handler or a
     * pre-built {@link HandlerExecutionChain}. This method should handle those
     * two cases explicitly, either building a new {@link HandlerExecutionChain}
     * or extending the existing chain.
     * <p>For simply adding an interceptor in a custom subclass, consider calling
     * {@code super.getHandlerExecutionChain(handler, request)} and invoking
     * {@link HandlerExecutionChain#addInterceptor} on the returned chain object.
     *
     * @param handler the resolved handler instance (never {@code null})
     * @param request current HTTP request
     * @return the HandlerExecutionChain (never {@code null})
     */
    protected HandlerExecutionChain getHandlerExecutionChain(Object handler, HttpRequest request) {
        HandlerExecutionChain chain = new HandlerExecutionChain(handler);
        String lookupPath = request.getLookupPath();
        for (MappedInterceptor mappedInterceptor : this.mappedInterceptorList) {
            if (mappedInterceptor.matches(lookupPath, this.pathMatcher)) {
                chain.addInterceptor(mappedInterceptor.getInterceptor());
            }
        }
        return chain;
    }

    /**
     * 异常业务处理
     *
     * @param request
     * @param response
     * @param exception
     */
    public void exceptionHandler(HttpRequest request, HttpResponse response, Throwable exception) throws Throwable {
        if (exceptionHandler == null) {
            throw exception;
        }
        exceptionHandler.exceptionHandle(request, response, exception);
    }

    /**
     * A thin wrapper around a matched HandlerMethod and its mapping, for the purpose of
     * comparing the best match with a comparator in the context of the current request.
     */
    private class Match {

        private final RequestMappingInfo mapping;

        private final HandlerMethod handlerMethod;

        public Match(RequestMappingInfo mapping, HandlerMethod handlerMethod) {
            this.mapping = mapping;
            this.handlerMethod = handlerMethod;
        }

        @Override
        public String toString() {
            return this.mapping.toString();
        }
    }


    private class MatchComparator implements Comparator<Match> {

        private final Comparator<RequestMappingInfo> comparator;

        public MatchComparator(Comparator<RequestMappingInfo> comparator) {
            this.comparator = comparator;
        }

        @Override
        public int compare(Match match1, Match match2) {
            return this.comparator.compare(match1.mapping, match2.mapping);
        }
    }

}
