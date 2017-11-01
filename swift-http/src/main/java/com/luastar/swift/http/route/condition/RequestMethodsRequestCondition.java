package com.luastar.swift.http.route.condition;

import com.google.common.collect.Lists;
import com.luastar.swift.http.route.RequestMethod;
import com.luastar.swift.http.server.HttpRequest;

import java.util.*;

/**
 *
 */
public class RequestMethodsRequestCondition extends AbstractRequestCondition<RequestMethodsRequestCondition> {

    private final Set<RequestMethod> methods;

    public RequestMethodsRequestCondition(RequestMethod... requestMethods) {
        this(asList(requestMethods));
    }

    private RequestMethodsRequestCondition(Collection<RequestMethod> requestMethods) {
        this.methods = Collections.unmodifiableSet(new LinkedHashSet(requestMethods));
    }

    private static List<RequestMethod> asList(RequestMethod... requestMethods) {
        if (requestMethods == null) {
            return Lists.newArrayList();
        }
        return Arrays.asList(requestMethods);
    }

    public Set<RequestMethod> getMethods() {
        return this.methods;
    }

    protected Collection<RequestMethod> getContent() {
        return this.methods;
    }

    protected String getToStringInfix() {
        return " || ";
    }

    public RequestMethodsRequestCondition combine(RequestMethodsRequestCondition other) {
        Set<RequestMethod> set = new LinkedHashSet(this.methods);
        set.addAll(other.methods);
        return new RequestMethodsRequestCondition(set);
    }

    public RequestMethodsRequestCondition getMatchingCondition(HttpRequest request) {
        if (this.methods.isEmpty()) {
            return this;
        } else {
            RequestMethod incomingRequestMethod = this.getRequestMethod(request);
            if (incomingRequestMethod != null) {
                Iterator iterator = this.methods.iterator();
                while (iterator.hasNext()) {
                    RequestMethod method = (RequestMethod) iterator.next();
                    if (method.equals(incomingRequestMethod)) {
                        return new RequestMethodsRequestCondition(new RequestMethod[]{method});
                    }
                }
            }
            return null;
        }
    }

    private RequestMethod getRequestMethod(HttpRequest request) {
        try {
            return RequestMethod.valueOf(request.getMethod());
        } catch (IllegalArgumentException var3) {
            return null;
        }
    }

    public int compareTo(RequestMethodsRequestCondition other, HttpRequest request) {
        return other.methods.size() - this.methods.size();
    }

}
