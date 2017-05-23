package com.luastar.swift.http.constant;


import com.luastar.swift.base.config.PropertyUtils;

public interface HttpConstant {

    /**
     * Name of the attribute that contains the path
     * within the handler mapping, in case of a pattern match, or the full
     * relevant URI (typically within the DispatcherServlet's mapping) else.
     * <p>Note: This attribute is not required to be supported by all
     * HttpConstant implementations. URL-based HttpConstants will
     * typically support it, but handlers should not necessarily expect
     * this request attribute to be present in all scenarios.
     */
    String PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE = HttpConstant.class.getName() + ".pathWithinHttpConstant";

    /**
     * Name of the attribute that contains the
     * best matching pattern within the handler mapping.
     * <p>Note: This attribute is not required to be supported by all
     * HttpConstant implementations. URL-based HttpConstants will
     * typically support it, but handlers should not necessarily expect
     * this request attribute to be present in all scenarios.
     */
    String BEST_MATCHING_PATTERN_ATTRIBUTE = HttpConstant.class.getName() + ".bestMatchingPattern";

    /**
     * Name of the boolean attribute that indicates
     * whether type-level mappings should be inspected.
     * <p>Note: This attribute is not required to be supported by all
     * HttpConstant implementations.
     */
    String INTROSPECT_TYPE_LEVEL_MAPPING = HttpConstant.class.getName() + ".introspectTypeLevelMapping";

    /**
     * Name of the attribute that contains the URI
     * templates map, mapping variable names to values.
     * <p>Note: This attribute is not required to be supported by all
     * HttpConstant implementations. URL-based HttpConstants will
     * typically support it, but handlers should not necessarily expect
     * this request attribute to be present in all scenarios.
     */
    String URI_TEMPLATE_VARIABLES_ATTRIBUTE = HttpConstant.class.getName() + ".uriTemplateVariables";

    /**
     * Name of the attribute that contains a map with
     * URI matrix variables.
     * <p>Note: This attribute is not required to be supported by all
     * HttpConstant implementations and may also not be present depending on
     * whether the HttpConstant is configured to keep matrix variable content
     * in the request URI.
     */
    String MATRIX_VARIABLES_ATTRIBUTE = HttpConstant.class.getName() + ".matrixVariables";

    /**
     * Name of the attribute that contains the set of
     * producible MediaTypes applicable to the mapped handler.
     * <p>Note: This attribute is not required to be supported by all
     * HttpConstant implementations. Handlers should not necessarily expect
     * this request attribute to be present in all scenarios.
     */
    String PRODUCIBLE_MEDIA_TYPES_ATTRIBUTE = HttpConstant.class.getName() + ".producibleMediaTypes";


    String SWIFT_CONFIG_LOCATION = PropertyUtils.getString("swift.config.location", "spring-swift.xml");
    int SWIFT_TIMEOUT = PropertyUtils.getInt("swift.timeout", 10);
    int SWIFT_MAX_CONTENT_LENGTH = PropertyUtils.getInt("swift.maxContentLength", 1024 * 1024);

}