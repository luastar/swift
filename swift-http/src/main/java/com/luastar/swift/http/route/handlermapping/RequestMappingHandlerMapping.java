package com.luastar.swift.http.route.handlermapping;

import com.luastar.swift.base.json.JsonUtils;
import com.luastar.swift.http.constant.HttpConstant;
import com.luastar.swift.http.route.RequestMethod;
import com.luastar.swift.http.route.condition.PatternsRequestCondition;
import com.luastar.swift.http.route.condition.RequestMappingInfo;
import com.luastar.swift.http.route.condition.RequestMethodsRequestCondition;
import com.luastar.swift.http.server.HttpRequest;
import com.luastar.swift.http.server.HttpService;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Controller;

import java.lang.reflect.Method;
import java.util.*;

/**
 *
 */
public class RequestMappingHandlerMapping extends AbstractHandlerMethodMapping<RequestMappingInfo> {

    /**
     * Get the URL path patterns associated with this {@link RequestMappingInfo}.
     */
    @Override
    protected Set<String> getMappingPathPatterns(RequestMappingInfo info) {
        return info.getPatternsCondition().getPatterns();
    }

    /**
     * Check if the given RequestMappingInfo matches the current request and
     * return a (potentially new) instance with conditions that match the
     * current request -- for example with a subset of URL patterns.
     *
     * @return an info in case of a match; or {@code null} otherwise.
     */
    @Override
    protected RequestMappingInfo getMatchingMapping(RequestMappingInfo info, HttpRequest request) {
        return info.getMatchingCondition(request);
    }

    /**
     * Provide a Comparator to sort RequestMappingInfos matched to a request.
     */
    @Override
    protected Comparator<RequestMappingInfo> getMappingComparator(final HttpRequest request) {
        return new Comparator<RequestMappingInfo>() {
            @Override
            public int compare(RequestMappingInfo info1, RequestMappingInfo info2) {
                return info1.compareTo(info2, request);
            }
        };
    }

    /**
     * Expose URI template variables, matrix variables, and producible media types in the request.
     *
     * @see HttpConstant#URI_TEMPLATE_VARIABLES_ATTRIBUTE
     * @see HttpConstant#MATRIX_VARIABLES_ATTRIBUTE
     * @see HttpConstant#PRODUCIBLE_MEDIA_TYPES_ATTRIBUTE
     */
    @Override
    protected void handleMatch(RequestMappingInfo info, String lookupPath, HttpRequest request) {
        super.handleMatch(info, lookupPath, request);
        String bestPattern;
        Map<String, String> uriVariables;
        Set<String> patterns = info.getPatternsCondition().getPatterns();
        if (patterns.isEmpty()) {
            bestPattern = lookupPath;
            uriVariables = Collections.emptyMap();
        } else {
            bestPattern = patterns.iterator().next();
            uriVariables = getPathMatcher().extractUriTemplateVariables(bestPattern, lookupPath);
        }
        request.setAttribute(HttpConstant.BEST_MATCHING_PATTERN_ATTRIBUTE, bestPattern);
        request.setAttribute(HttpConstant.URI_TEMPLATE_VARIABLES_ATTRIBUTE, uriVariables);
    }

    /**
     * Iterate all RequestMappingInfos once again, look if any match by URL at
     * least and raise exceptions accordingly.
     */
    @Override
    protected HandlerMethod handleNoMatch(Set<RequestMappingInfo> requestMappingInfos, String lookupPath, HttpRequest request) {
        Set<String> allowedMethods = new LinkedHashSet<String>(4);
        Set<RequestMappingInfo> patternMatches = new HashSet<RequestMappingInfo>();
        Set<RequestMappingInfo> patternAndMethodMatches = new HashSet<RequestMappingInfo>();
        for (RequestMappingInfo info : requestMappingInfos) {
            if (info.getPatternsCondition().getMatchingCondition(request) != null) {
                patternMatches.add(info);
                if (info.getMethodsCondition().getMatchingCondition(request) != null) {
                    patternAndMethodMatches.add(info);
                } else {
                    for (RequestMethod method : info.getMethodsCondition().getMethods()) {
                        allowedMethods.add(method.name());
                    }
                }
            }
        }
        if (patternMatches.isEmpty()) {
            return null;
        } else if (patternAndMethodMatches.isEmpty() && !allowedMethods.isEmpty()) {
            logger.warn("Http Request Method Not Supported : method={}, allowedMethods={}", request.getMethod(), JsonUtils.toJson(allowedMethods));
        }
        return null;
    }

    /**
     * {@inheritDoc}
     * Expects a handler to have a type-level @{@link Controller} annotation.
     */
    @Override
    protected boolean isHandler(Class<?> beanType) {
        return AnnotationUtils.findAnnotation(beanType, HttpService.class) != null;
    }

    /**
     * Uses method and type-level @{@link HttpService} annotations to create
     * the RequestMappingInfo.
     *
     * @return the created RequestMappingInfo, or {@code null} if the method
     * does not have a {@code @RequestMapping} annotation.
     */
    @Override
    protected RequestMappingInfo getMappingForMethod(Method method, Class<?> handlerType) {
        RequestMappingInfo info = null;
        HttpService methodAnnotation = AnnotationUtils.findAnnotation(method, HttpService.class);
        if (methodAnnotation != null) {
            // 方法上的注解
            info = createRequestMappingInfo(methodAnnotation);
            HttpService typeAnnotation = AnnotationUtils.findAnnotation(handlerType, HttpService.class);
            if (typeAnnotation != null) {
                // 类上的注解
                info = createRequestMappingInfo(typeAnnotation).combine(info);
            }
        }
        return info;
    }

    /**
     * Created a RequestMappingInfo from a RequestMapping annotation.
     */
    protected RequestMappingInfo createRequestMappingInfo(HttpService annotation) {
        String[] patterns = annotation.value();
        // 暂时只实现路径和方法条件
        return new RequestMappingInfo(
                new PatternsRequestCondition(patterns, getPathMatcher(), true, true),
                new RequestMethodsRequestCondition(annotation.method())
        );
    }

}
