package com.luastar.swift.http.server;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface HttpService {

    /**
     * The primary mapping expressed by this annotation.
     * <p>In a Servlet environment: the path mapping URIs (e.g. "/myPath.do").
     * Ant-style path patterns are also supported (e.g. "/myPath/*.do").
     * At the method level, relative paths (e.g. "edit.do") are supported
     * within the primary mapping expressed at the type level.
     * Path mapping URIs may contain placeholders (e.g. "/${connect}")
     * <p>In a Portlet environment: the mapped portlet modes
     * (i.e. "EDIT", "VIEW", "HELP" or any custom modes).
     * <p><b>Supported at the type level as well as at the method level!</b>
     * When used at the type level, all method-level mappings inherit
     * this primary mapping, narrowing it for a specific handler method.
     */
    String[] value() default {};

}
