package com.luastar.swift.http.server;

import com.alibaba.fastjson.JSON;
import com.luastar.swift.http.constant.HttpConstant;
import com.luastar.swift.http.constant.HttpMediaType;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import io.netty.util.CharsetUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import java.io.ByteArrayOutputStream;

public class HttpResponse {

    private static final Logger logger = LoggerFactory.getLogger(HttpResponse.class);

    private String requestId;

    private FullHttpResponse fullHttpResponse;

    private String result;

    private ByteArrayOutputStream outputStream;

    private Resource resource;

    private long contentLength = 0;

    public HttpResponse(String requestId) {
        this.requestId = requestId;
        this.fullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
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
        logger.info("response status: {}", getStatus());
        logger.info("response headers : {}", JSON.toJSONString(getHeaders()));
        String body = getResult();
        if (StringUtils.isEmpty(body)) {
            if (getOutputStream() == null) {
                logger.info("response body is empty");
            } else {
                logger.info("response body is stream");
            }
        } else {
            if (body.length() <= HttpConstant.SWIFT_MAX_LOG_LENGTH) {
                logger.info("response body : {}", body);
            } else {
                logger.info("response body is too long to log out");
            }
        }
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public HttpResponseStatus getStatus() {
        return fullHttpResponse.status();
    }

    public void setStatus(HttpResponseStatus status) {
        fullHttpResponse.setStatus(status);
    }

    public HttpHeaders getHeaders() {
        return fullHttpResponse.headers();
    }

    public String getHeader(String key) {
        return fullHttpResponse.headers().get(key);
    }

    public void setHeader(String key, Object value) {
        fullHttpResponse.headers().set(key, value);
    }

    public void addHeader(String key, Object value) {
        fullHttpResponse.headers().add(key, value);
    }

    public void addCookie(Cookie cookie) {
        fullHttpResponse.headers().add(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.STRICT.encode(cookie));
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

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public long getContentLength() {
        return contentLength;
    }

    public FullHttpResponse getFullHttpResponse() {
        if (getOutputStream() != null) {
            // 此处使用copiedBuffer会导致excel等文档有问题
            ByteBuf buf = Unpooled.wrappedBuffer(getOutputStream().toByteArray());
            fullHttpResponse = fullHttpResponse.replace(buf);
            this.contentLength = buf.readableBytes();
        } else if (StringUtils.isNotEmpty(getResult())) {
            ByteBuf buf = Unpooled.copiedBuffer(getResult(), CharsetUtil.UTF_8);
            fullHttpResponse = fullHttpResponse.replace(buf);
            this.contentLength = buf.readableBytes();
        }
        return fullHttpResponse;
    }

}
