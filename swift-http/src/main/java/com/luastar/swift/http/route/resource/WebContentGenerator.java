package com.luastar.swift.http.route.resource;

import com.luastar.swift.base.json.JsonUtils;
import com.luastar.swift.http.route.RequestMethod;
import com.luastar.swift.http.server.HttpRequest;
import com.luastar.swift.http.server.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by zhuminghua on 2017/11/1.
 */

public class WebContentGenerator extends ApplicationObjectSupport {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected static final String HEADER_PRAGMA = "Pragma";

    protected static final String HEADER_EXPIRES = "Expires";

    protected static final String HEADER_CACHE_CONTROL = "Cache-Control";

    protected static final String HEADER_IF_MODIFIED_SINCE = "If-Modified-Since";

    protected static final String HEADER_LAST_MODIFIED = "Last-Modified";

    /**
     * Set of supported HTTP methods
     */
    private Set<String> supportedMethods;

    /**
     * Use HTTP 1.0 expires header?
     */
    private boolean useExpiresHeader = true;

    /**
     * Use HTTP 1.1 cache-control header?
     */
    private boolean useCacheControlHeader = true;

    /**
     * Use HTTP 1.1 cache-control header value "no-store"?
     */
    private boolean useCacheControlNoStore = true;

    private int cacheSeconds = -1;

    private boolean alwaysMustRevalidate = false;

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
     * @param restrictDefaultSupportedMethods {@code true} if this generator should support HTTP methods GET, HEAD and POST by default, or {@code false} if it should be unrestricted
     */
    public WebContentGenerator(boolean restrictDefaultSupportedMethods) {
        if (restrictDefaultSupportedMethods) {
            this.supportedMethods = new HashSet<String>(4);
            this.supportedMethods.add(RequestMethod.GET.name());
            this.supportedMethods.add(RequestMethod.HEAD.name());
            this.supportedMethods.add(RequestMethod.POST.name());
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
        if (methods != null) {
            this.supportedMethods = new HashSet<String>(Arrays.asList(methods));
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
     * Set whether to use the HTTP 1.0 expires header. Default is "true".
     * <p>Note: Cache headers will only get applied if caching is enabled
     * (or explicitly prevented) for the current request.
     */
    public final void setUseExpiresHeader(boolean useExpiresHeader) {
        this.useExpiresHeader = useExpiresHeader;
    }

    /**
     * Return whether the HTTP 1.0 expires header is used.
     */
    public final boolean isUseExpiresHeader() {
        return this.useExpiresHeader;
    }

    /**
     * Set whether to use the HTTP 1.1 cache-control header. Default is "true".
     * <p>Note: Cache headers will only get applied if caching is enabled
     * (or explicitly prevented) for the current request.
     */
    public final void setUseCacheControlHeader(boolean useCacheControlHeader) {
        this.useCacheControlHeader = useCacheControlHeader;
    }

    /**
     * Return whether the HTTP 1.1 cache-control header is used.
     */
    public final boolean isUseCacheControlHeader() {
        return this.useCacheControlHeader;
    }

    /**
     * Set whether to use the HTTP 1.1 cache-control header value "no-store"
     * when preventing caching. Default is "true".
     */
    public final void setUseCacheControlNoStore(boolean useCacheControlNoStore) {
        this.useCacheControlNoStore = useCacheControlNoStore;
    }

    /**
     * Return whether the HTTP 1.1 cache-control header value "no-store" is used.
     */
    public final boolean isUseCacheControlNoStore() {
        return this.useCacheControlNoStore;
    }

    /**
     * An option to add 'must-revalidate' to every Cache-Control header. This
     * may be useful with annotated controller methods, which can
     * programmatically do a lastModified calculation as described
     */
    public void setAlwaysMustRevalidate(boolean mustRevalidate) {
        this.alwaysMustRevalidate = mustRevalidate;
    }

    /**
     * Return whether 'must-revalidate' is added to every Cache-Control header.
     */
    public boolean isAlwaysMustRevalidate() {
        return alwaysMustRevalidate;
    }

    /**
     * Cache content for the given number of seconds. Default is -1,
     * indicating no generation of cache-related headers.
     * <p>Only if this is set to 0 (no cache) or a positive value (cache for
     * this many seconds) will this class generate cache headers.
     * <p>The headers can be overwritten by subclasses, before content is generated.
     */
    public final void setCacheSeconds(int seconds) {
        this.cacheSeconds = seconds;
    }

    /**
     * Return the number of seconds that content is cached.
     */
    public final int getCacheSeconds() {
        return this.cacheSeconds;
    }


    /**
     * Check and prepare the given request and response according to the settings
     * of this generator. Checks for supported methods and a required session,
     * and applies the number of cache seconds specified for this generator.
     *
     * @param request      current HTTP request
     * @param response     current HTTP response
     * @param lastModified if the mapped handler provides Last-Modified support
     */
    protected final void checkAndPrepare(HttpRequest request, HttpResponse response, boolean lastModified) {
        checkAndPrepare(request, response, this.cacheSeconds, lastModified);
    }

    /**
     * Check and prepare the given request and response according to the settings
     * of this generator. Checks for supported methods and a required session,
     * and applies the given number of cache seconds.
     *
     * @param request      current HTTP request
     * @param response     current HTTP response
     * @param cacheSeconds positive number of seconds into the future that the
     *                     response should be cacheable for, 0 to prevent caching
     * @param lastModified if the mapped handler provides Last-Modified support
     */
    protected final void checkAndPrepare(HttpRequest request, HttpResponse response, int cacheSeconds, boolean lastModified) {
        // Check whether we should support the request method.
        String method = request.getMethod();
        if (this.supportedMethods != null && !this.supportedMethods.contains(method)) {
            logger.error("Http Request Method Not Supported : method={}, supportedMethods={}", method, JsonUtils.toJson(this.supportedMethods));
            return;
        }
        // Do declarative cache control.
        // Revalidate if the controller supports last-modified.
        applyCacheSeconds(response, cacheSeconds, lastModified);
    }

    /**
     * Prevent the response from being cached.
     * See {@code http://www.mnot.net/cache_docs}.
     */
    protected final void preventCaching(HttpResponse response) {
        response.setHeader(HEADER_PRAGMA, "no-cache");
        if (this.useExpiresHeader) {
            // HTTP 1.0 header
            response.setHeader(HEADER_EXPIRES, 1L);
        }
        if (this.useCacheControlHeader) {
            // HTTP 1.1 header: "no-cache" is the standard value,
            // "no-store" is necessary to prevent caching on FireFox.
            response.setHeader(HEADER_CACHE_CONTROL, "no-cache");
            if (this.useCacheControlNoStore) {
                response.addHeader(HEADER_CACHE_CONTROL, "no-store");
            }
        }
    }

    /**
     * Set HTTP headers to allow caching for the given number of seconds.
     * Does not tell the browser to revalidate the resource.
     *
     * @param response current HTTP response
     * @param seconds  number of seconds into the future that the response
     *                 should be cacheable for
     */
    protected final void cacheForSeconds(HttpResponse response, int seconds) {
        cacheForSeconds(response, seconds, false);
    }

    /**
     * Set HTTP headers to allow caching for the given number of seconds.
     * Tells the browser to revalidate the resource if mustRevalidate is
     * {@code true}.
     *
     * @param response       the current HTTP response
     * @param seconds        number of seconds into the future that the response
     *                       should be cacheable for
     * @param mustRevalidate whether the client should revalidate the resource
     *                       (typically only necessary for controllers with last-modified support)
     */
    protected final void cacheForSeconds(HttpResponse response, int seconds, boolean mustRevalidate) {
        if (this.useExpiresHeader) {
            // HTTP 1.0 header
            response.setHeader(HEADER_EXPIRES, System.currentTimeMillis() + seconds * 1000L);
        }
        if (this.useCacheControlHeader) {
            // HTTP 1.1 header
            String headerValue = "max-age=" + seconds;
            if (mustRevalidate || this.alwaysMustRevalidate) {
                headerValue += ", must-revalidate";
            }
            response.setHeader(HEADER_CACHE_CONTROL, headerValue);
        }
    }

    /**
     * Apply the given cache seconds and generate corresponding HTTP headers,
     * i.e. allow caching for the given number of seconds in case of a positive
     * value, prevent caching if given a 0 value, do nothing else.
     * Does not tell the browser to revalidate the resource.
     *
     * @param response current HTTP response
     * @param seconds  positive number of seconds into the future that the
     *                 response should be cacheable for, 0 to prevent caching
     */
    protected final void applyCacheSeconds(HttpResponse response, int seconds) {
        applyCacheSeconds(response, seconds, false);
    }

    /**
     * Apply the given cache seconds and generate respective HTTP headers.
     * <p>That is, allow caching for the given number of seconds in the
     * case of a positive value, prevent caching if given a 0 value, else
     * do nothing (i.e. leave caching to the client).
     *
     * @param response       the current HTTP response
     * @param seconds        the (positive) number of seconds into the future that
     *                       the response should be cacheable for; 0 to prevent caching; and
     *                       a negative value to leave caching to the client.
     * @param mustRevalidate whether the client should revalidate the resource
     *                       (typically only necessary for controllers with last-modified support)
     */
    protected final void applyCacheSeconds(HttpResponse response, int seconds, boolean mustRevalidate) {
        if (seconds > 0) {
            cacheForSeconds(response, seconds, mustRevalidate);
        } else if (seconds == 0) {
            preventCaching(response);
        }
        // Leave caching to the client otherwise.
    }

}
