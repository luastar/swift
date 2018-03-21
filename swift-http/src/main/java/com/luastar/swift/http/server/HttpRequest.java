package com.luastar.swift.http.server;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.luastar.swift.base.entity.SwiftHashMap;
import com.luastar.swift.base.utils.DateUtils;
import com.luastar.swift.base.utils.ObjUtils;
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
    private Map<String, FileUpload> fileMap = Maps.newLinkedHashMap();
    private Map<String, Object> attributeMap = Maps.newLinkedHashMap();

    public HttpRequest(FullHttpRequest request, String requestId, String socketIp) {
        this.request = request;
        this.requestId = requestId;
        this.socketIp = socketIp;
        initRequestHeader();
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
        initRequestIp();
    }

    protected void initRequestIp() {
        String clientIP = request.headers().get("X-Forwarded-For");
        if (StringUtils.isBlank(clientIP)) {
            clientIP = request.headers().get("X-Real-IP");
        }
        if (StringUtils.isBlank(clientIP)) {
            clientIP = this.socketIp;
        }
        if (StringUtils.isNotBlank(clientIP) && StringUtils.contains(clientIP, ",")) {
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
        }
    }

    protected void decodeBody() {
        try {
            if (HttpMethod.GET.equals(request.method())) {
                return;
            }
            String contentType = getContentType();
            if (StringUtils.isEmpty(contentType)) {
                return;
            }
            if (StringUtils.containsIgnoreCase(contentType, HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED)
                    || StringUtils.containsIgnoreCase(contentType, HttpHeaderValues.MULTIPART_FORM_DATA)) {
                // 只解析 X_WWW_FORM_URLENCODED 和 MULTIPART_FORM_DATA
                postRequestDecoder = new HttpPostRequestDecoder(factory, request);
                List<InterfaceHttpData> dataList = postRequestDecoder.getBodyHttpDatas();
                for (InterfaceHttpData data : dataList) {
                    if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {
                        Attribute attribute = (Attribute) data;
                        parameterMap.put(attribute.getName(), attribute.getValue());
                    } else if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.FileUpload) {
                        FileUpload fileUpload = (FileUpload) data;
                        fileMap.put(fileUpload.getName(), fileUpload);
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void logRequest() {
        logger.info("request ip : {}, socketIp : {}", getIp(), getSocketIp());
        logger.info("request method : {}, uri : {}", getMethod(), getUri());
        logger.info("request headers : {}", JSON.toJSONString(headerMap));
        logger.info("request attributes : {}", JSON.toJSONString(attributeMap));
        String body = getBody();
        if (StringUtils.isNotEmpty(body)) {
            if (body.length() <= HttpConstant.SWIFT_MAX_LOG_LENGTH) {
                logger.info("request body : {}", getBody());
            } else {
                logger.info("request body is too long to log out");
            }
        }
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

    public String getParameter(String key) {
        return parameterMap.get(key);
    }

    public String getParameter(String key, String defaultValue) {
        String value = getParameter(key);
        if (StringUtils.isEmpty(value)) {
            return defaultValue;
        }
        return value;
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
        if (StringUtils.isEmpty(body)) {
            return null;
        }
        return JSON.parseObject(body, clazz);
    }

    public <T> List<T> getBodyArray(Class<T> clazz) {
        String body = getBody();
        if (StringUtils.isEmpty(body)) {
            return null;
        }
        return JSON.parseArray(body, clazz);
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
        return obj;
    }

    public void destroy() {
        if (postRequestDecoder != null) {
            postRequestDecoder.destroy();
            postRequestDecoder = null;
        }
    }

}
