package com.luastar.swift.http.server;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.luastar.swift.http.constant.HttpConstant;
import com.luastar.swift.http.constant.HttpMediaType;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.cookie.Cookie;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.util.Set;

public class HttpResponse {

    private static final Logger logger = LoggerFactory.getLogger(HttpResponse.class);

    private String requestId;

    private String result;

    private ByteArrayOutputStream outputStream;

    private Map<String, String> headerMap;

    private Set<Cookie> cookieSet;

    private HttpResponseStatus status;

    public HttpResponse(String requestId) {
        this.requestId = requestId;
        this.status = HttpResponseStatus.OK;
        this.headerMap = Maps.newLinkedHashMap();
        this.cookieSet = Sets.newLinkedHashSet();
        setResponseContentTypeJson();
    }

    public void setResponseContentTypePlain() {
        setHeader(HttpHeaderNames.CONTENT_TYPE.toString(), HttpMediaType.TEXT_PLAIN_UTF_8);
    }

    public void setResponseContentTypeJson() {
        setHeader(HttpHeaderNames.CONTENT_TYPE.toString(), HttpMediaType.JSON_UTF_8);
    }

    public void setResponseContentTypeHtml() {
        setHeader(HttpHeaderNames.CONTENT_TYPE.toString(), HttpMediaType.TEXT_HTML_UTF_8);
    }

    public void setResponseContentTypeStream(String fileName) {
        setHeader(HttpHeaderNames.CONTENT_TYPE.toString(), HttpMediaType.APPLICATION_OCTET_STREAM_UTF_8);
        setHeader(HttpHeaderNames.CONTENT_DISPOSITION.toString(), "attachment;filename=" + fileName);
    }

    public void logResponse() {
        logger.info("---返回信息开始---------------------------------------------------------");
        logger.info("-- response status: {}", getStatus());
        logger.info("-- response headers : {}", getHeaderMap());
        logger.info("-- response cookie : {}", getCookieSet());
        String body = getResult();
        if (StringUtils.isEmpty(body)) {
            if (getOutputStream() == null) {
                logger.info("-- response body is empty.");
            } else {
                logger.info("-- response body is stream.");
            }
        } else {
            if (body.length() <= HttpConstant.SWIFT_MAX_LOG_LENGTH) {
                logger.info("-- response body : {}", body);
            } else {
                logger.info("-- response body is too long to log out.");
            }
        }
        logger.info("---返回信息结束---------------------------------------------------------");
    }

    public String getHeader(String key) {
        return headerMap.get(key);
    }

    public void setHeader(String key, String value) {
        headerMap.put(key, value);
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public ByteArrayOutputStream getOutputStream() {
        return outputStream;
    }

    public void setOutputStream(ByteArrayOutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public Map<String, String> getHeaderMap() {
        return headerMap;
    }

    public void setHeaderMap(Map<String, String> headerMap) {
        this.headerMap = headerMap;
    }

    public Set<Cookie> getCookieSet() {
        return cookieSet;
    }

    public void setCookieSet(Set<Cookie> cookieSet) {
        this.cookieSet = cookieSet;
    }

    public void addCookie(Cookie cookie) {
        this.cookieSet.add(cookie);
    }

    public HttpResponseStatus getStatus() {
        return status;
    }

    public void setStatus(HttpResponseStatus status) {
        this.status = status;
    }

    public void destroy() {
        IOUtils.closeQuietly(outputStream);
    }

}
