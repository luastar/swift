package com.luastar.swift.http.server;

import com.google.common.collect.Maps;
import com.luastar.swift.http.constant.HttpConstant;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.multipart.*;
import io.netty.util.CharsetUtil;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.validation.DataBinder;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HttpRequest {

    private static final Logger logger = LoggerFactory.getLogger(HttpRequest.class);

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

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
        this.request = request;
        logger.info("request uri : {}", getUri());
        initRequestHeader();
        decodeQueryString();
        decodePost();
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

    protected void decodePost() {
        if (!HttpMethod.POST.equals(request.method())) {
            return;
        }
        try {
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
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public String getUri() {
        return request.uri();
    }

    public String getMethod() {
        return request.method().toString();
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

    public void setParameter(String key, String value) {
        parameterMap.put(key, value);
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

    public String getBody() {
        ByteBuf content = request.content();
        if (content != null) {
            return content.toString(CharsetUtil.UTF_8);
        }
        return "";
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
        dataBinder.registerCustomEditor(Date.class, new CustomDateEditor(new SimpleDateFormat(DATE_FORMAT), true));
        dataBinder.bind(new MutablePropertyValues(parameterMap));
        return obj;
    }

    public void cleanFiles() {
        if (postRequestDecoder != null) {
            postRequestDecoder.cleanFiles();
        }
    }

    public void destroy() {
        if (postRequestDecoder != null) {
            postRequestDecoder.destroy();
            postRequestDecoder = null;
        }
    }

}
