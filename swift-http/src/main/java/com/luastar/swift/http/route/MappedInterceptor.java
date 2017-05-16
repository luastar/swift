package com.luastar.swift.http.route;

import org.springframework.util.PathMatcher;

/**
 * Holds information about a HandlerInterceptor mapped to a path into the application.
 * Provides a method to match a request path to the mapped path patterns.
 *
 * @author Keith Donald
 * @author Rossen Stoyanchev
 * @since 3.0
 */
public final class MappedInterceptor {

    private String[] includePatterns;

    private String[] excludePatterns;

    private HandlerInterceptor interceptor;

    public String[] getIncludePatterns() {
        return includePatterns;
    }

    public void setIncludePatterns(String[] includePatterns) {
        this.includePatterns = includePatterns;
    }

    public String[] getExcludePatterns() {
        return excludePatterns;
    }

    public void setExcludePatterns(String[] excludePatterns) {
        this.excludePatterns = excludePatterns;
    }

    public HandlerInterceptor getInterceptor() {
        return interceptor;
    }

    public void setInterceptor(HandlerInterceptor interceptor) {
        this.interceptor = interceptor;
    }

    /**
     * Returns {@code true} if the interceptor applies to the given request path.
     *
     * @param lookupPath  the current request path
     * @param pathMatcher a path matcher for path pattern matching
     */
    public boolean matches(String lookupPath, PathMatcher pathMatcher) {
        if (this.excludePatterns != null) {
            for (String pattern : this.excludePatterns) {
                if (pathMatcher.match(pattern, lookupPath)) {
                    return false;
                }
            }
        }
        if (this.includePatterns == null) {
            return true;
        } else {
            for (String pattern : this.includePatterns) {
                if (pathMatcher.match(pattern, lookupPath)) {
                    return true;
                }
            }
            return false;
        }
    }

}
