package com.luastar.swift.http.server;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
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
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.validation.DataBinder;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HttpRequest {

    private static final Logger logger = LoggerFactory.getLogger(HttpRequest.class);

    private static final String MDC_KEY = "requestId";

    private static final long LOG_MAX_BODY_LENGTH = 1024 * 1024;

    private static final HttpDataFactory factory = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE);

    static {
        DiskFileUpload.deleteOnExitTemporaryFile = true;
        DiskFileUpload.baseDirectory = null;
        DiskAttribute.deleteOnExitTemporaryFile = true;
        DiskAttribute.baseDirectory = null;
    }

    private String requestId;
    private String ip;

    private FullHttpRequest request;
    private QueryStringDecoder queryStringDecoder;
    private HttpPostRequestDecoder postRequestDecoder;

    private Map<String, String> headerMap = new CaseInsensitiveMap<String, String>();
    private Map<String, Cookie> cookieMap = Maps.newLinkedHashMap();
    private Map<String, String> parameterMap = Maps.newLinkedHashMap();
    private Map<String, FileUpload> fileMap = Maps.newLinkedHashMap();
    private Map<String, Object> attributeMap = Maps.newLinkedHashMap();

    public HttpRequest(FullHttpRequest request) {
        this.requestId = RandomStringUtils.random(20, true, true);
        // 日志上下文中加入requestId
        MDC.put(MDC_KEY, requestId);
        this.request = request;
        initRequestHeader();
        decodeQueryString();
        decodeBody();
        logRequestInfo();
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
        if (HttpMethod.GET.equals(request.method())) {
            return;
        }
        String contentType = getContentType();
        if (StringUtils.isEmpty(contentType)) {
            return;
        }
        try {
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
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void logRequestInfo() {
        logger.info("request uri : {}", getUri());
        logger.info("request headers : {}", JSON.toJSONString(getHeaderMap()));
        String body = getBody();
        if (StringUtils.isNotEmpty(body)) {
            if (body.length() <= LOG_MAX_BODY_LENGTH) {
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

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
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

    public ByteBufInputStream getBodyInputStream() {
        ByteBuf content = request.content();
        if (content != null) {
            return new ByteBufInputStream(content);
        }
        return null;
    }

    public <T> T bindObj(T obj) {
        /*
        try {
            BeanUtils.populate(obj, parameterMap);
        } catch (IllegalAccessException e) {
            logger.error(e.getMessage(),e);
        } catch (InvocationTargetException e) {
            logger.error(e.getMessage(),e);
        }
        */
        DataBinder dataBinder = new DataBinder(obj);
        dataBinder.registerCustomEditor(Date.class, new CustomDateEditor(DateUtils.NORMAL_FORMAT, true));
        dataBinder.bind(new MutablePropertyValues(parameterMap));
        return obj;
    }

    public void cleanFiles() {
        if (postRequestDecoder != null) {
            postRequestDecoder.cleanFiles();
        }
    }

    public void destroy() {
        MDC.remove(MDC_KEY);
        if (postRequestDecoder != null) {
            postRequestDecoder.destroy();
            postRequestDecoder = null;
        }
    }

}
