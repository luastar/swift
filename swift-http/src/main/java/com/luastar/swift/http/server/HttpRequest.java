package com.luastar.swift.http.server;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.luastar.swift.base.entity.SwiftHashMap;
import com.luastar.swift.base.utils.DateUtils;
import com.luastar.swift.base.utils.ObjUtils;
import com.luastar.swift.base.utils.ValidateUtils;
import com.luastar.swift.http.constant.HttpConstant;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.multipart.*;
import io.netty.util.CharsetUtil;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.validation.DataBinder;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HttpRequest {

    private static final Logger logger = LoggerFactory.getLogger(HttpRequest.class);

    /**
     * 10MB以内文件存在内存中
     */
    private static final HttpDataFactory factory = new DefaultHttpDataFactory(1024 * 1024 * 10);

    static {
        DiskFileUpload.deleteOnExitTemporaryFile = true;
        DiskFileUpload.baseDirectory = null;
        DiskAttribute.deleteOnExitTemporaryFile = true;
        DiskAttribute.baseDirectory = null;
    }

    private String requestId;
    private String socketIp;
    private String ip;

    private FullHttpRequest request;
    private QueryStringDecoder queryStringDecoder;
    private HttpPostRequestDecoder postRequestDecoder;

    private Map<String, String> headerMap = new CaseInsensitiveMap<>();
    private Map<String, Cookie> cookieMap = Maps.newLinkedHashMap();
    private Map<String, String> parameterMap = Maps.newLinkedHashMap();
    private Map<String, List<String>> multParameterMap = Maps.newLinkedHashMap();
    private Map<String, FileUpload> fileMap = Maps.newLinkedHashMap();
    private Map<String, Object> attributeMap = Maps.newLinkedHashMap();

    public HttpRequest(FullHttpRequest request, String requestId, String socketIp) {
        this.request = request;
        this.requestId = requestId;
        this.socketIp = socketIp;
        initRequestHeader();
        initRequestIp();
        decodeQueryString();
        decodeBody();
    }

    protected void initRequestHeader() {
        for (Map.Entry<String, String> entry : request.headers()) {
            headerMap.put(entry.getKey(), entry.getValue());
        }
        String cookieString = request.headers().get(HttpHeaderNames.COOKIE);
        if (StringUtils.isNotEmpty(cookieString)) {
            Set<Cookie> cookieSet = ServerCookieDecoder.STRICT.decode(cookieString);
            for (Cookie cookie : cookieSet) {
                cookieMap.put(cookie.name(), cookie);
            }
        }
    }

    protected void initRequestIp() {
        String clientIP = request.headers().get("X-Forwarded-For");
        if (ObjUtils.isEmpty(clientIP)) {
            clientIP = request.headers().get("X-Real-IP");
        }
        if (ObjUtils.isEmpty(clientIP)) {
            clientIP = this.socketIp;
        }
        if (ObjUtils.isNotEmpty(clientIP) && StringUtils.contains(clientIP, ",")) {
            clientIP = StringUtils.split(clientIP, ",")[0];
        }
        this.ip = clientIP;
    }

    protected void decodeQueryString() {
        queryStringDecoder = new QueryStringDecoder(getUri());
        Map<String, List<String>> uriAttributes = queryStringDecoder.parameters();
        for (Map.Entry<String, List<String>> attr : uriAttributes.entrySet()) {
            for (String attrVal : attr.getValue()) {
                parameterMap.put(attr.getKey(), attrVal);
            }
            if (attr.getValue().size() > 1) {
                multParameterMap.put(attr.getKey(), attr.getValue());
            }
        }
    }

    /**
     * 默认解析 X_WWW_FORM_URLENCODED 和 MULTIPART_FORM_DATA 两种类型的表体
     * 其他支持解析的类型可以手动调用 decodeBodyFormData() 方法
     */
    protected void decodeBody() {
        if (HttpMethod.GET.equals(request.method())) {
            return;
        }
        String contentType = getContentType();
        if (ObjUtils.isEmpty(contentType)) {
            return;
        }
        if (StringUtils.containsIgnoreCase(contentType, HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED)
                || StringUtils.containsIgnoreCase(contentType, HttpHeaderValues.MULTIPART_FORM_DATA)) {
            decodeBodyFormData();
        }
    }

    /**
     * 解析表单内容的表体
     */
    public void decodeBodyFormData() {
        try {
            postRequestDecoder = new HttpPostRequestDecoder(factory, request);
            List<InterfaceHttpData> dataList = postRequestDecoder.getBodyHttpDatas();
            for (InterfaceHttpData data : dataList) {
                if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {
                    Attribute attribute = (Attribute) data;
                    // 如果有重复key，保存到多值 map
                    if (parameterMap.containsKey(attribute.getName())) {
                        List<String> multValueList = multParameterMap.get(attribute.getName());
                        if (multValueList == null) {
                            multValueList = Lists.newArrayList(parameterMap.get(attribute.getName()));
                        }
                        multValueList.add(attribute.getValue());
                        multParameterMap.put(attribute.getName(), multValueList);
                    }
                    // 单值 map 直接覆盖
                    parameterMap.put(attribute.getName(), attribute.getValue());
                } else if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.FileUpload) {
                    FileUpload fileUpload = (FileUpload) data;
                    fileMap.put(fileUpload.getName(), fileUpload);
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void logRequest() {
        String reqHeaderJson = JSON.toJSONString(getHeaderMap());
        MDC.put(HttpConstant.MDC_KEY_REQUEST_IP, getIp());
        MDC.put(HttpConstant.MDC_KEY_REQUEST_URI, getUri());
        MDC.put(HttpConstant.MDC_KEY_REQUEST_METHOD, getMethod());
        MDC.put(HttpConstant.MDC_KEY_REQUEST_HEADER, reqHeaderJson);
        logger.info("===请求信息开始=========================================================");
        logger.info("== request ip : {}, socketIp : {}", getIp(), getSocketIp());
        logger.info("== request method : {}, uri : {}", getMethod(), getUri());
        logger.info("== request headers : {}", reqHeaderJson);
        String body = getBody();
        if (StringUtils.isNotEmpty(body)) {
            if (body.length() <= HttpConstant.SWIFT_MAX_LOG_LENGTH) {
                MDC.put(HttpConstant.MDC_KEY_REQUEST_BODY, body);
                logger.info("== request body : {}", body);
            } else {
                MDC.put(HttpConstant.MDC_KEY_REQUEST_BODY, "request body is too long to log out");
                logger.info("== request body is too long to log out.");
            }
        }
        logger.info("===请求信息结束=========================================================");
    }

    public String getUri() {
        return request.uri();
    }

    public String getMethod() {
        return request.method().toString();
    }

    public String getContentType() {
        return request.headers().get(HttpHeaderNames.CONTENT_TYPE);
    }

    public String getLookupPath() {
        return queryStringDecoder.path();
    }

    public String getRequestId() {
        return requestId;
    }

    public String getIp() {
        return ObjUtils.ifNull(ip, "");
    }

    public String getSocketIp() {
        return socketIp;
    }

    public FullHttpRequest getFullHttpRequest() {
        return request;
    }

    public Map<String, String> getHeaderMap() {
        return headerMap;
    }

    public String getHeader(String key) {
        return headerMap.get(key);
    }

    public void setHeader(String key, String value) {
        headerMap.put(key, value);
    }

    public Map<String, Cookie> getCookieMap() {
        return cookieMap;
    }

    public String getCookie(String key) {
        Cookie cookie = cookieMap.get(key);
        if (cookie == null) {
            return null;
        }
        return cookie.value();
    }

    public Map<String, String> getParameterMap() {
        return parameterMap;
    }

    public Map<String, List<String>> getMultParameterMap() {
        return multParameterMap;
    }

    public String getParameter(String key) {
        return parameterMap.get(key);
    }

    public String getParameter(String key, String defaultValue) {
        String value = getParameter(key);
        if (ObjUtils.isEmpty(value)) {
            return defaultValue;
        }
        return value;
    }

    public List<String> getMultParameter(String key) {
        return multParameterMap.get(key);
    }

    public List<String> getMultParameter(String key, List<String> defaultValue) {
        List<String> valueList = getMultParameter(key);
        if (ObjUtils.isEmpty(valueList)) {
            return defaultValue;
        }
        return valueList;
    }

    public Integer getIntParameter(String key) {
        return ObjUtils.toInteger(getParameter(key));
    }

    public Integer getIntParameter(String key, Integer defaultValue) {
        return ObjUtils.toInteger(getParameter(key), defaultValue);
    }

    public Long getLongParameter(String key) {
        return ObjUtils.toLong(getParameter(key));
    }

    public Long getLongParameter(String key, Long defaultValue) {
        return ObjUtils.toLong(getParameter(key), defaultValue);
    }

    public BigDecimal getBigDecimalParameter(String key) {
        return ObjUtils.toBigDecimal(getParameter(key));
    }

    public BigDecimal getBigDecimalParameter(String key, BigDecimal defaultValue) {
        return ObjUtils.toBigDecimal(getParameter(key), defaultValue);
    }

    public Boolean getBooleanParameter(String key) {
        return ObjUtils.toBoolean(getParameter(key));
    }

    public Boolean getBooleanParameter(String key, Boolean defaultValue) {
        return ObjUtils.toBoolean(getParameter(key), defaultValue);
    }

    public Map<String, FileUpload> getFileMap() {
        return fileMap;
    }

    public FileUpload getFile(String key) {
        return fileMap.get(key);
    }

    public InputStream getFileInputStream(String key) throws IOException {
        return getFileInputStream(getFile(key));
    }

    public InputStream getFileInputStream(FileUpload fileUpload) throws IOException {
        if (fileUpload == null) {
            return null;
        }
        if (fileUpload.isInMemory()) {
            return new ByteBufInputStream(fileUpload.content());
        } else {
            return new FileInputStream(fileUpload.getFile());
        }
    }

    public Map<String, Object> getAttributeMap() {
        return attributeMap;
    }

    public Object getAttribute(String key) {
        return attributeMap.get(key);
    }

    public void setAttribute(String key, Object value) {
        attributeMap.put(key, value);
    }

    public String getPathValue(String key) {
        Map<String, String> uriVariables = (Map<String, String>) attributeMap.get(HttpConstant.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if (uriVariables == null) {
            return null;
        }
        return uriVariables.get(key);
    }

    public Integer getIntPathValue(String key) {
        return ObjUtils.toInteger(getPathValue(key));
    }

    public Long getLongPathValue(String key) {
        return ObjUtils.toLong(getPathValue(key));
    }

    public String getBody() {
        ByteBuf content = request.content();
        if (content != null) {
            return content.toString(CharsetUtil.UTF_8);
        }
        return "";
    }

    public <T> T getBodyObject(Class<T> clazz) {
        String body = getBody();
        if (ObjUtils.isEmpty(body)) {
            return null;
        }
        T obj = null;
        try {
            obj = JSON.parseObject(body, clazz);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return ValidateUtils.validate(obj);
    }

    public <T> List<T> getBodyArray(Class<T> clazz) {
        String body = getBody();
        if (ObjUtils.isEmpty(body)) {
            return null;
        }
        try {
            return JSON.parseArray(body, clazz);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    public SwiftHashMap<String, Object> getBodyMap() {
        return getBodyObject(SwiftHashMap.class);
    }

    public ByteBufInputStream getBodyInputStream() {
        ByteBuf content = request.content();
        if (content != null) {
            return new ByteBufInputStream(content);
        }
        return null;
    }

    public <T> T bindObj(T obj) {
        DataBinder dataBinder = new DataBinder(obj);
        dataBinder.registerCustomEditor(Date.class, new CustomDateEditor(DateUtils.NORMAL_FORMAT, true));
        dataBinder.bind(new MutablePropertyValues(parameterMap));
        return ValidateUtils.validate(obj);
    }

    public void destroy() {
        if (postRequestDecoder != null) {
            postRequestDecoder.destroy();
            postRequestDecoder = null;
        }
        headerMap.clear();
        cookieMap.clear();
        parameterMap.clear();
        multParameterMap.clear();
        fileMap.clear();
        attributeMap.clear();
        request.release();
    }

}
