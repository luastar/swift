package com.luastar.swift.http.route;

import com.luastar.swift.http.server.HttpRequest;

public final class RequestMappingInfo implements RequestCondition<RequestMappingInfo> {

    private final PatternsRequestCondition patternsCondition;
    private final RequestMethodsRequestCondition methodsCondition;

    public RequestMappingInfo(PatternsRequestCondition patterns, RequestMethodsRequestCondition methods) {
        this.patternsCondition = patterns != null ? patterns : new PatternsRequestCondition();
        this.methodsCondition = methods != null ? methods : new RequestMethodsRequestCondition(new RequestMethod[0]);
    }

    public PatternsRequestCondition getPatternsCondition() {
        return this.patternsCondition;
    }

    public RequestMethodsRequestCondition getMethodsCondition() {
        return this.methodsCondition;
    }

    public RequestMappingInfo combine(RequestMappingInfo other) {
        PatternsRequestCondition patterns = this.patternsCondition.combine(other.patternsCondition);
        RequestMethodsRequestCondition methods = this.methodsCondition.combine(other.methodsCondition);
        return new RequestMappingInfo(patterns, methods);
    }

    public RequestMappingInfo getMatchingCondition(HttpRequest request) {
        RequestMethodsRequestCondition methods = this.methodsCondition.getMatchingCondition(request);
        if (methods != null) {
            PatternsRequestCondition patterns = this.patternsCondition.getMatchingCondition(request);
            if (patterns == null) {
                return null;
            } else {
                return new RequestMappingInfo(patterns, methods);
            }
        } else {
            return null;
        }
    }

    public int compareTo(RequestMappingInfo other, HttpRequest request) {
        int result = this.patternsCondition.compareTo(other.getPatternsCondition(), request);
        if (result != 0) {
            return result;
        } else {
            result = this.methodsCondition.compareTo(other.getMethodsCondition(), request);
            if (result != 0) {
                return result;
            } else {
                return 0;
            }
        }
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj != null && obj instanceof RequestMappingInfo) {
            RequestMappingInfo other = (RequestMappingInfo) obj;
            return this.patternsCondition.equals(other.patternsCondition) && this.methodsCondition.equals(other.methodsCondition);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return this.patternsCondition.hashCode() * 31 + this.methodsCondition.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("{");
        builder.append(this.patternsCondition);
        builder.append(",methods=").append(this.methodsCondition);
        builder.append('}');
        return builder.toString();
    }

}
