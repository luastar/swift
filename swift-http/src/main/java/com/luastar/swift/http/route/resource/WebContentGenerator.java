package com.luastar.swift.http.route.resource;

import com.luastar.swift.base.utils.StrUtils;
import com.luastar.swift.http.server.HttpRequest;
import com.luastar.swift.http.server.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 *
 */

public class WebContentGenerator extends ApplicationObjectSupport {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    public static final String METHOD_GET = "GET";
    public static final String METHOD_HEAD = "HEAD";
    public static final String METHOD_POST = "POST";

    public static final String HEADER_PRAGMA = "Pragma";
    public static final String HEADER_EXPIRES = "Expires";
    public static final String HEADER_CACHE_CONTROL = "Cache-Control";

    public static final String HEADER_IF_MODIFIED_SINCE = "If-Modified-Since";
    public static final String HEADER_LAST_MODIFIED = "Last-Modified";

    /**
     * Set of supported HTTP methods
     */
    private Set<String> supportedMethods;

    private CacheControl cacheControl;

    /**
     * Create a new WebContentGenerator which supports
     * HTTP methods GET, HEAD and POST by default.
     */
    public WebContentGenerator() {
        this(true);
    }

    /**
     * Create a new WebContentGenerator.
     *
     * @param restrictDefaultSupportedMethods {@code true} if this
     *                                        generator should support HTTP methods GET, HEAD and POST by default,
     *                                        or {@code false} if it should be unrestricted
     */
    public WebContentGenerator(boolean restrictDefaultSupportedMethods) {
        if (restrictDefaultSupportedMethods) {
            this.supportedMethods = new HashSet<String>(4);
            this.supportedMethods.add(METHOD_GET);
            this.supportedMethods.add(METHOD_HEAD);
            this.supportedMethods.add(METHOD_POST);
        }
    }

    /**
     * Create a new WebContentGenerator.
     *
     * @param supportedMethods the supported HTTP methods for this content generator
     */
    public WebContentGenerator(String... supportedMethods) {
        this.supportedMethods = new HashSet<String>(Arrays.asList(supportedMethods));
    }


    /**
     * Set the HTTP methods that this content generator should support.
     * <p>Default is GET, HEAD and POST for simple form controller types;
     * unrestricted for general controllers and interceptors.
     */
    public final void setSupportedMethods(String... methods) {
        if (!ObjectUtils.isEmpty(methods)) {
            this.supportedMethods = new LinkedHashSet<String>(Arrays.asList(methods));
        } else {
            this.supportedMethods = null;
        }
    }

    /**
     * Return the HTTP methods that this content generator supports.
     */
    public final String[] getSupportedMethods() {
        return StringUtils.toStringArray(this.supportedMethods);
    }

    /**
     * Set the {@link CacheControl} instance to build
     * the Cache-Control HTTP response header.
     *
     * @since 4.2
     */
    public final void setCacheControl(CacheControl cacheControl) {
        this.cacheControl = cacheControl;
    }

    /**
     * Get the {@link CacheControl} instance
     * that builds the Cache-Control HTTP response header.
     *
     * @since 4.2
     */
    public final CacheControl getCacheControl() {
        return this.cacheControl;
    }

    /**
     * Check the given request for supported methods and a required session, if any.
     *
     * @param request current HTTP request
     * @throws Exception if the request cannot be handled because a check failed
     * @since 4.2
     */
    protected final void checkRequest(HttpRequest request) throws Exception {
        // Check whether we should support the request method.
        String method = request.getMethod();
        if (this.supportedMethods != null && !this.supportedMethods.contains(method)) {
            String msg = StrUtils.formatString("HttpRequestMethodNotSupportedException method={0},supportedMethods={1}", method, this.supportedMethods);
            throw new Exception(msg);
        }
    }

    /**
     * Prepare the given response according to the settings of this generator.
     * Applies the number of cache seconds specified for this generator.
     *
     * @param response current HTTP response
     * @since 4.2
     */
    protected final void prepareResponse(HttpResponse response) {
        if (this.cacheControl != null) {
            applyCacheControl(response, this.cacheControl);
        }
    }

    /**
     * Set the HTTP Cache-Control header according to the given settings.
     *
     * @param response     current HTTP response
     * @param cacheControl the pre-configured cache control settings
     * @since 4.2
     */
    protected final void applyCacheControl(HttpResponse response, CacheControl cacheControl) {
        String ccValue = cacheControl.getHeaderValue();
        if (ccValue != null) {
            // Set computed HTTP 1.1 Cache-Control header
            response.setHeader(HEADER_CACHE_CONTROL, ccValue);
            if (response.getHeaders().contains(HEADER_PRAGMA)) {
                // Reset HTTP 1.0 Pragma header if present
                response.setHeader(HEADER_PRAGMA, "");
            }
            if (response.getHeaders().contains(HEADER_EXPIRES)) {
                // Reset HTTP 1.0 Expires header if present
                response.setHeader(HEADER_EXPIRES, "");
            }
        }
    }

}
