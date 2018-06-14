package com.luastar.swift.http.server;

import com.alibaba.fastjson.JSON;
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
        StringBuilder info = new StringBuilder()
                .append("当前结果信息：").append("\n")
                .append("============================================================").append("\n")
                .append("== response status: ").append(getStatus()).append("\n")
                .append("== response headers : ").append(JSON.toJSONString(headerMap)).append("\n")
                .append("== response cookie : ").append(JSON.toJSONString(cookieSet)).append("\n");
        String body = getResult();
        if (StringUtils.isEmpty(body)) {
            if (getOutputStream() == null) {
                info.append("== response body is empty.").append("\n");
            } else {
                info.append("== response body is stream.").append("\n");
            }
        } else {
            if (body.length() <= HttpConstant.SWIFT_MAX_LOG_LENGTH) {
                info.append("== response body : ").append(body).append("\n");
            } else {
                info.append("== response body is too long to log out.").append("\n");
            }
        }
        info.append("============================================================").append("\n");
        logger.info(info.toString());
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
