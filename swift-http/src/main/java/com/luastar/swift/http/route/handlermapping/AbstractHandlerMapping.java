package com.luastar.swift.http.route.handlermapping;

import com.luastar.swift.http.route.interceptor.HandlerInterceptor;
import com.luastar.swift.http.route.interceptor.MappedInterceptor;
import com.luastar.swift.http.server.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.core.Ordered;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.Assert;
import org.springframework.util.PathMatcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 *
 */
public abstract class AbstractHandlerMapping extends ApplicationObjectSupport implements HandlerMapping, Ordered {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private int order = Integer.MAX_VALUE;  // default: same as non-Ordered

    private Object defaultHandler;

    private PathMatcher pathMatcher = new AntPathMatcher();

    private final List<Object> interceptors = new ArrayList<Object>();

    private final List<HandlerInterceptor> adaptedInterceptors = new ArrayList<HandlerInterceptor>();

    private final List<MappedInterceptor> mappedInterceptors = new ArrayList<MappedInterceptor>();

    /**
     * Specify the order value for this HandlerMapping bean.
     * <p>Default value is {@code Integer.MAX_VALUE}, meaning that it's non-ordered.
     *
     * @see org.springframework.core.Ordered#getOrder()
     */
    public final void setOrder(int order) {
        this.order = order;
    }

    @Override
    public final int getOrder() {
        return this.order;
    }

    public Object getDefaultHandler() {
        return this.defaultHandler;
    }

    public void setDefaultHandler(Object defaultHandler) {
        this.defaultHandler = defaultHandler;
    }

    public PathMatcher getPathMatcher() {
        return this.pathMatcher;
    }

    public void setPathMatcher(PathMatcher pathMatcher) {
        Assert.notNull(pathMatcher, "PathMatcher must not be null");
        this.pathMatcher = pathMatcher;
    }

    /**
     * Set the interceptors to apply for all handlers mapped by this handler mapping.
     * <p>Supported interceptor types are HandlerInterceptor, WebRequestInterceptor, and MappedInterceptor.
     * Mapped interceptors apply only to request URLs that match its path patterns.
     * Mapped interceptor beans are also detected by type during initialization.
     *
     * @param interceptors array of handler interceptors, or {@code null} if none
     */
    public void setInterceptors(Object[] interceptors) {
        this.interceptors.addAll(Arrays.asList(interceptors));
    }

    @Override
    protected void initApplicationContext() throws BeansException {
        extendInterceptors(this.interceptors);
        detectMappedInterceptors(this.mappedInterceptors);
        initInterceptors();
    }

    /**
     * Extension hook that subclasses can override to register additional interceptors,
     * given the configured interceptors (see {@link #setInterceptors}).
     * <p>Will be invoked before {@link #initInterceptors()} adapts the specified
     * interceptors into {@link HandlerInterceptor} instances.
     * <p>The default implementation is empty.
     *
     * @param interceptors the configured interceptor List (never {@code null}), allowing
     *                     to add further interceptors before as well as after the existing interceptors
     */
    protected void extendInterceptors(List<Object> interceptors) {
    }

    /**
     * Detect beans of type {@link MappedInterceptor} and add them to the list of mapped interceptors.
     * <p>This is called in addition to any {@link MappedInterceptor}s that may have been provided
     * via {@link #setInterceptors}, by default adding all beans of type {@link MappedInterceptor}
     * from the current context and its ancestors. Subclasses can override and refine this policy.
     *
     * @param mappedInterceptorList an empty list to add {@link MappedInterceptor} instances to
     */
    protected void detectMappedInterceptors(List<MappedInterceptor> mappedInterceptorList) {
        Map<String, MappedInterceptor> mappedInterceptorMap = BeanFactoryUtils.beansOfTypeIncludingAncestors(getApplicationContext(), MappedInterceptor.class, true, false);
        mappedInterceptorList.addAll(mappedInterceptorMap.values());
        for (MappedInterceptor interceptor : mappedInterceptorList) {
            logger.info("detect mapped interceptor {}, includePatterns={}, excludePatterns={}", interceptor.getInterceptor().getClass().getName(), interceptor.getIncludePatterns(), interceptor.getExcludePatterns());
        }
    }

    /**
     * Initialize the specified interceptors, checking for {@link MappedInterceptor}s and
     * adapting {@link HandlerInterceptor}s if necessary.
     *
     * @see #setInterceptors
     */
    protected void initInterceptors() {
        if (this.interceptors.isEmpty()) {
            return;
        }
        for (int i = 0; i < this.interceptors.size(); i++) {
            Object interceptor = this.interceptors.get(i);
            if (interceptor == null) {
                throw new IllegalArgumentException("Entry number " + i + " in interceptors array is null");
            }
            if (interceptor instanceof MappedInterceptor) {
                this.mappedInterceptors.add((MappedInterceptor) interceptor);
            } else if (interceptor instanceof HandlerInterceptor) {
                this.adaptedInterceptors.add((HandlerInterceptor) interceptor);
            } else {
                throw new IllegalArgumentException("Interceptor type not supported: " + interceptor.getClass().getName());
            }
        }
    }

    /**
     * Return the adapted interceptors as {@link HandlerInterceptor} array.
     *
     * @return the array of {@link HandlerInterceptor}s, or {@code null} if none
     */
    protected final HandlerInterceptor[] getAdaptedInterceptors() {
        int count = this.adaptedInterceptors.size();
        return (count > 0 ? this.adaptedInterceptors.toArray(new HandlerInterceptor[count]) : null);
    }

    /**
     * Return all configured {@link MappedInterceptor}s as an array.
     *
     * @return the array of {@link MappedInterceptor}s, or {@code null} if none
     */
    protected final MappedInterceptor[] getMappedInterceptors() {
        int count = this.mappedInterceptors.size();
        return (count > 0 ? this.mappedInterceptors.toArray(new MappedInterceptor[count]) : null);
    }

    /**
     * Look up a handler for the given request, falling back to the default
     * handler if no specific one is found.
     *
     * @param request current HTTP request
     * @return the corresponding handler instance, or the default handler
     * @see #getHandlerInternal
     */
    @Override
    public final HandlerExecutionChain getHandler(HttpRequest request) throws Exception {
        Object handler = getHandlerInternal(request);
        if (handler == null) {
            handler = getDefaultHandler();
        }
        if (handler == null) {
            return null;
        }
        // Bean name or resolved handler?
        if (handler instanceof String) {
            String handlerName = (String) handler;
            handler = getApplicationContext().getBean(handlerName);
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
    protected abstract Object getHandlerInternal(HttpRequest request) throws Exception;

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
     * @see #getAdaptedInterceptors()
     */
    protected HandlerExecutionChain getHandlerExecutionChain(Object handler, HttpRequest request) {
        HandlerExecutionChain chain = (handler instanceof HandlerExecutionChain ? (HandlerExecutionChain) handler : new HandlerExecutionChain(handler));
        chain.addInterceptors(getAdaptedInterceptors());
        String lookupPath = request.getLookupPath();
        for (MappedInterceptor mappedInterceptor : this.mappedInterceptors) {
            if (mappedInterceptor.matches(lookupPath, this.pathMatcher)) {
                chain.addInterceptor(mappedInterceptor.getInterceptor());
            }
        }
        return chain;
    }

}
