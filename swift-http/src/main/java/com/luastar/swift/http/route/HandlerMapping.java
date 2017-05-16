package com.luastar.swift.http.route;

import com.luastar.swift.http.server.HttpRequest;

public interface HandlerMapping {

    /**
     * Return a handler and any interceptors for this request. The choice may be made
     * on request URL, session state, or any factor the implementing class chooses.
     * <p>The returned HandlerExecutionChain contains a handler Object, rather than
     * even a tag interface, so that handlers are not constrained in any way.
     * For example, a HandlerAdapter could be written to allow another framework's
     * handler objects to be used.
     * <p>Returns {@code null} if no match was found. This is not an error.
     * The DispatcherServlet will query all registered HandlerMapping beans to find
     * a match, and only decide there is an error if none can find a handler.
     * @param request current HTTP request
     * @return a HandlerExecutionChain instance containing handler object and
     * any interceptors, or {@code null} if no mapping found
     * @throws Exception if there is an internal error
     */
    HandlerExecutionChain getHandler(HttpRequest request) throws Exception;
    
}
