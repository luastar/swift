package com.luastar.swift.http.server;

import com.luastar.swift.http.constant.HttpConstant;
import com.luastar.swift.http.constant.HttpMediaType;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import io.netty.util.CharsetUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;

public class HttpResponse {

    private static final Logger logger = LoggerFactory.getLogger(HttpResponse.class);

    private String requestId;

    private FullHttpResponse fullHttpResponse;

    private String result;

    private ByteArrayOutputStream outputStream;

    public HttpResponse(String requestId) {
        this.requestId = requestId;
        this.fullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        setResponseContentTypeJson();
    }

    public void setResponseContentTypePlain() {
        setHeader(HttpHeaderNames.CONTENT_TYPE, HttpMediaType.TEXT_PLAIN_UTF_8);
    }

    public void setResponseContentTypeJson() {
        setHeader(HttpHeaderNames.CONTENT_TYPE, HttpMediaType.JSON_UTF_8);
    }

    public void setResponseContentTypeHtml() {
        setHeader(HttpHeaderNames.CONTENT_TYPE, HttpMediaType.TEXT_HTML_UTF_8);
    }

    public void setResponseContentTypeStream(String fileName) {
        setHeader(HttpHeaderNames.CONTENT_TYPE, HttpMediaType.APPLICATION_OCTET_STREAM_UTF_8);
        setHeader(HttpHeaderNames.CONTENT_DISPOSITION, "attachment;filename=" + fileName);
    }

    public void logResponse() {
        logger.info("===返回信息开始=========================================================");
        logger.info("== response status: {}", getStatus());
        logger.info("== response headers : {}", getHeaders());
        String body = getResult();
        if (StringUtils.isEmpty(body)) {
            if (getOutputStream() == null) {
                logger.info("== response body is empty.");
            } else {
                logger.info("== response body is stream.");
            }
        } else {
            if (body.length() <= HttpConstant.SWIFT_MAX_LOG_LENGTH) {
                logger.info("== response body : {}", body);
            } else {
                logger.info("== response body is too long to log out.");
            }
        }
        logger.info("===返回信息结束=========================================================");
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

    public String getHeader(CharSequence key) {
        return fullHttpResponse.headers().get(key);
    }

    public void setHeader(CharSequence key, Object value) {
        fullHttpResponse.headers().set(key, value);
    }

    public void addHeader(CharSequence key, Object value) {
        fullHttpResponse.headers().add(key, value);
    }

    public void addCookie(Cookie cookie) {
        addHeader(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.STRICT.encode(cookie));
    }

    public void sendRedirect(String uri) {
        setStatus(HttpResponseStatus.FOUND);
        setHeader(HttpHeaderNames.LOCATION, uri);
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

    public FullHttpResponse getFullHttpResponse() {
        long contentLength = 0;
        if (getOutputStream() != null) {
            // 此处使用copiedBuffer会导致excel等文档有问题
            ByteBuf buf = Unpooled.wrappedBuffer(getOutputStream().toByteArray());
            fullHttpResponse = fullHttpResponse.replace(buf);
            contentLength = buf.readableBytes();
        } else if (StringUtils.isNotEmpty(getResult())) {
            ByteBuf buf = Unpooled.copiedBuffer(getResult(), CharsetUtil.UTF_8);
            fullHttpResponse = fullHttpResponse.replace(buf);
            contentLength = buf.readableBytes();
        }
        HttpUtil.setContentLength(fullHttpResponse, contentLength);
        return fullHttpResponse;
    }

    public void destroy() {
        IOUtils.closeQuietly(outputStream);
    }

}
