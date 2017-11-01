package com.luastar.swift.http.route.handlermapping;

import com.luastar.swift.http.constant.HttpConstant;
import com.luastar.swift.http.server.HttpRequest;
import com.luastar.swift.http.server.HttpResponse;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.ClassUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.*;

/**
 *
 */
public abstract class AbstractHandlerMethodMapping<T> extends AbstractHandlerMapping implements InitializingBean {

    private final Map<T, HandlerMethod> handlerMethods = new LinkedHashMap<T, HandlerMethod>();

    private final MultiValueMap<String, T> urlMap = new LinkedMultiValueMap<String, T>();

    /**
     * Return a map with all handler methods and their mappings.
     */
    public Map<T, HandlerMethod> getHandlerMethods() {
        return Collections.unmodifiableMap(this.handlerMethods);
    }

    /**
     * Detects handler methods at initialization.
     */
    @Override
    public void afterPropertiesSet() {
        initHandlerMethods();
    }

    /**
     * Scan beans in the ApplicationContext, detect and register handler methods.
     *
     * @see #isHandler(Class)
     * @see #getMappingForMethod(Method, Class)
     * @see #handlerMethodsInitialized(Map)
     */
    protected void initHandlerMethods() {
        logger.info("Looking for request mappings in application context: " + getApplicationContext());
        String[] beanNames = getApplicationContext().getBeanNamesForType(Object.class);
        for (String beanName : beanNames) {
            if (isHandler(getApplicationContext().getType(beanName))) {
                detectHandlerMethods(beanName);
            }
        }
        handlerMethodsInitialized(getHandlerMethods());
    }

    /**
     * Whether the given type is a handler with handler methods.
     *
     * @param beanType the type of the bean being checked
     * @return "true" if this a handler type, "false" otherwise.
     */
    protected abstract boolean isHandler(Class<?> beanType);

    /**
     * Look for handler methods in a handler.
     *
     * @param handler the bean name of a handler or a handler instance
     */
    protected void detectHandlerMethods(final Object handler) {
        Class<?> handlerType = (handler instanceof String ? getApplicationContext().getType((String) handler) : handler.getClass());
        // Avoid repeated calls to getMappingForMethod which would rebuild RequestMatchingInfo instances
        final Map<Method, T> mappings = new IdentityHashMap<Method, T>();
        final Class<?> userType = ClassUtils.getUserClass(handlerType);
        Set<Method> methods = HandlerMethodSelector.selectMethods(userType, new ReflectionUtils.MethodFilter() {
            @Override
            public boolean matches(Method method) {
                T mapping = getMappingForMethod(method, userType);
                if (mapping == null) {
                    return false;
                }
                // 增加方法的参数判断，不满足的忽略掉
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
     * Provide the mapping for a handler method. A method for which no
     * mapping can be provided is not a handler method.
     *
     * @param method      the method to provide a mapping for
     * @param handlerType the handler type, possibly a sub-type of the method's declaring class
     * @return the mapping, or {@code null} if the method is not mapped
     */
    protected abstract T getMappingForMethod(Method method, Class<?> handlerType);

    /**
     * Register a handler method and its unique mapping.
     *
     * @param handler the bean name of the handler or the handler instance
     * @param method  the method to register
     * @param mapping the mapping conditions associated with the handler method
     * @throws IllegalStateException if another method was already registered under the same mapping
     */
    protected void registerHandlerMethod(Object handler, Method method, T mapping) {
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
        logger.info("Mapped [" + mapping + "] onto " + newHandlerMethod);
        Set<String> patterns = getMappingPathPatterns(mapping);
        for (String pattern : patterns) {
            if (!getPathMatcher().isPattern(pattern)) {
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
            handlerMethod = new HandlerMethod(beanName, getApplicationContext(), method);
        } else {
            handlerMethod = new HandlerMethod(handler, method);
        }
        return handlerMethod;
    }

    /**
     * Extract and return the URL paths contained in a mapping.
     */
    protected abstract Set<String> getMappingPathPatterns(T mapping);

    /**
     * Invoked after all handler methods have been detected.
     *
     * @param handlerMethods a read-only map with handler methods and mappings.
     */
    protected void handlerMethodsInitialized(Map<T, HandlerMethod> handlerMethods) {
    }

    /**
     * Look up a handler method for the given request.
     */
    @Override
    protected HandlerMethod getHandlerInternal(HttpRequest request) throws Exception {
        String lookupPath = request.getLookupPath();
        HandlerMethod handlerMethod = lookupHandlerMethod(lookupPath, request);
        logger.info("Looking up handler method for path {}", lookupPath);
        if (handlerMethod != null) {
            logger.info("Returning handler method [{}]", handlerMethod);
        } else {
            logger.info("Did not find handler method for [{}]-[{}]", request.getMethod(), lookupPath);
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
     * @see #handleMatch(Object, String, HttpRequest)
     * @see #handleNoMatch(Set, String, HttpRequest)
     */
    protected HandlerMethod lookupHandlerMethod(String lookupPath, HttpRequest request) throws Exception {
        List<Match> matches = new ArrayList<Match>();
        List<T> directPathMatches = this.urlMap.get(lookupPath);
        if (directPathMatches != null) {
            addMatchingMappings(directPathMatches, matches, request);
        }
        if (matches.isEmpty()) {
            // No choice but to go through all mappings...
            addMatchingMappings(this.handlerMethods.keySet(), matches, request);
        }
        if (matches.isEmpty()) {
            return handleNoMatch(handlerMethods.keySet(), lookupPath, request);
        } else {
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
        }
    }

    private void addMatchingMappings(Collection<T> mappings, List<Match> matches, HttpRequest request) {
        for (T mapping : mappings) {
            T match = getMatchingMapping(mapping, request);
            if (match != null) {
                matches.add(new Match(match, this.handlerMethods.get(mapping)));
            }
        }
    }

    /**
     * Check if a mapping matches the current request and return a (potentially
     * new) mapping with conditions relevant to the current request.
     *
     * @param mapping the mapping to get a match for
     * @param request the current HTTP servlet request
     * @return the match, or {@code null} if the mapping doesn't match
     */
    protected abstract T getMatchingMapping(T mapping, HttpRequest request);

    /**
     * Return a comparator for sorting matching mappings.
     * The returned comparator should sort 'better' matches higher.
     *
     * @param request the current request
     * @return the comparator, never {@code null}
     */
    protected abstract Comparator<T> getMappingComparator(HttpRequest request);

    /**
     * Invoked when a matching mapping is found.
     *
     * @param mapping    the matching mapping
     * @param lookupPath mapping lookup path within the current servlet mapping
     * @param request    the current request
     */
    protected void handleMatch(T mapping, String lookupPath, HttpRequest request) {
        request.setAttribute(HttpConstant.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE, lookupPath);
    }

    /**
     * Invoked when no matching mapping is not found.
     *
     * @param mappings   all registered mappings
     * @param lookupPath mapping lookup path within the current servlet mapping
     * @param request    the current request
     */
    protected HandlerMethod handleNoMatch(Set<T> mappings, String lookupPath, HttpRequest request) throws Exception {

        return null;
    }


    /**
     * A thin wrapper around a matched HandlerMethod and its mapping, for the purpose of
     * comparing the best match with a comparator in the context of the current request.
     */
    private class Match {

        private final T mapping;

        private final HandlerMethod handlerMethod;

        public Match(T mapping, HandlerMethod handlerMethod) {
            this.mapping = mapping;
            this.handlerMethod = handlerMethod;
        }

        @Override
        public String toString() {
            return this.mapping.toString();
        }
    }


    private class MatchComparator implements Comparator<Match> {

        private final Comparator<T> comparator;

        public MatchComparator(Comparator<T> comparator) {
            this.comparator = comparator;
        }

        @Override
        public int compare(Match match1, Match match2) {
            return this.comparator.compare(match1.mapping, match2.mapping);
        }
    }

}
