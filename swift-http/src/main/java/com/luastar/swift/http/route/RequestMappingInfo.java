package com.luastar.swift.http.route;

import com.luastar.swift.http.server.HttpRequest;

public final class RequestMappingInfo implements RequestCondition<RequestMappingInfo> {

    private final PatternsRequestCondition patternsCondition;

    public RequestMappingInfo(PatternsRequestCondition patterns) {
        this.patternsCondition = (patterns != null ? patterns : new PatternsRequestCondition());
    }

    public PatternsRequestCondition getPatternsCondition() {
        return this.patternsCondition;
    }

    public RequestMappingInfo combine(RequestMappingInfo other) {
        PatternsRequestCondition patterns = this.patternsCondition.combine(other.patternsCondition);
        return new RequestMappingInfo(patterns);
    }

    public RequestMappingInfo getMatchingCondition(HttpRequest request) {
        PatternsRequestCondition patterns = this.patternsCondition.getMatchingCondition(request);
        if (patterns == null) {
            return null;
        }
        return new RequestMappingInfo(patterns);
    }

    public int compareTo(RequestMappingInfo other, HttpRequest request) {
        int result = this.patternsCondition.compareTo(other.getPatternsCondition(), request);
        if (result != 0) {
            return result;
        }
        return 0;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("{");
        builder.append(this.patternsCondition);
        builder.append('}');
        return builder.toString();
    }

}
