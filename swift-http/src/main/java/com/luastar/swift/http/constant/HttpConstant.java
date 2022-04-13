package com.luastar.swift.http.constant;


import com.luastar.swift.base.config.PropertyUtils;
import io.netty.util.NettyRuntime;

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

    /**
     * 日志标记 请求id
     */
    String MDC_KEY_REQUESTID = "requestId";
    /**
     * 日志标记 请求id
     */
    String MDC_KEY_REQUEST_ID = "request_id";
    /**
     * 日志标记 请求ip
     */
    String MDC_KEY_REQUEST_IP = "request_ip";
    /**
     * 日志标记 请求路由
     */
    String MDC_KEY_REQUEST_URI = "request_uri";
    /**
     * 日志标记 请求方式
     */
    String MDC_KEY_REQUEST_METHOD = "request_method";
    /**
     * 日志标记 请求头
     */
    String MDC_KEY_REQUEST_HEADER = "request_header";
    /**
     * 日志标记 请求体
     */
    String MDC_KEY_REQUEST_BODY = "request_body";
    /**
     * 日志标记 返回状态
     */
    String MDC_KEY_RESPONSE_STATUS = "response_status";
    /**
     * 日志标记 返回头
     */
    String MDC_KEY_RESPONSE_HEADER = "response_header";
    /**
     * 日志标记 返回体
     */
    String MDC_KEY_RESPONSE_BODY = "response_body";
    /**
     * 日志标记 耗时
     */
    String MDC_KEY_REQUEST_COST = "request_cost";

    /**
     * 系统自带spring配置文件地址
     */
    String SWIFT_BASE_CONFIG_LOCATION = "classpath*:spring-swift-base.xml";

    /**
     * 项目spring配置文件地址
     */
    String SWIFT_CONFIG_LOCATION = PropertyUtils.getString("swift.config.location", "classpath:spring-swift.xml");
    /**
     * 任务执行超时时间（秒），默认值 86400（24小时）
     */
    int SWIFT_EXECUTE_TIMEOUT = PropertyUtils.getInt("swift.executeTimeout", 86400);
    /**
     * 请求行最大长度
     */
    int SWIFT_MAX_INITIAL_LINE_LENGTH = PropertyUtils.getInt("swift.maxInitialLineLength", 4096);
    /**
     * 请求头最大值
     */
    int SWIFT_MAX_HEADER_SIZE = PropertyUtils.getInt("swift.maxHeaderSize", 8192*4);
    /**
     * 请求块最大值
     */
    int SWIFT_MAX_CHUNK_SIZE = PropertyUtils.getInt("swift.maxChunkSize", 8192);
    /**
     * 请求体最大值
     */
    int SWIFT_MAX_CONTENT_LENGTH = PropertyUtils.getInt("swift.maxContentLength", 1024 * 1024 * 10);
    /**
     * 输出日志最大值
     */
    int SWIFT_MAX_LOG_LENGTH = PropertyUtils.getInt("swift.maxLogLength", 1024 * 16);
    /**
     * 分发线程数，默认值 1（为0表示cpu个数 * 2）
     */
    int SWIFT_BOSS_THREADS = PropertyUtils.getInt("swift.bossThreads", 1);
    /**
     * 工作线程数，默认值 0（为0表示cpu个数 * 2）
     */
    int SWIFT_WORKER_THREADS = PropertyUtils.getInt("swift.workerThreads", 0);
    /**
     * 业务线程数，默认值 max(16, cpu * 2)
     */
    int SWIFT_BUSINESS_THREADS = PropertyUtils.getInt("swift.businessThreads", Math.max(16, NettyRuntime.availableProcessors() * 2));
    /**
     * 返回结果压缩级别，0~9，默认6
     */
    int SWIFT_COMPRESSION_LEVEL = PropertyUtils.getInt("swift.compressionLevel", 6);

}
