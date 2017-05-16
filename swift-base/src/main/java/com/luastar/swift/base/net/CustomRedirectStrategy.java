package com.luastar.swift.base.net;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.*;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.protocol.HttpContext;

public class CustomRedirectStrategy extends DefaultRedirectStrategy {

    @Override
    public boolean isRedirected(HttpRequest request, HttpResponse response, HttpContext context) throws ProtocolException {
        return false;
    }

    public boolean isRedirectedDefault(HttpRequest request, HttpResponse response, HttpContext context) throws ProtocolException {
        return super.isRedirected(request, response, context);
    }

    public String getRedirectLocation(HttpRequest request, HttpResponse response, HttpContext context) throws ProtocolException {

        final HttpClientContext clientContext = HttpClientContext.adapt(context);
        final Header locationHeader = response.getFirstHeader("location");
        if (locationHeader == null) {
            throw new ProtocolException("Received redirect response "
                    + response.getStatusLine()
                    + " but no location header");
        }
        final String location = locationHeader.getValue();
        // 绝对路径直接返回
        if (StringUtils.contains(location, "://")) {
            return location;
        }
        // 处理相对路径
        HttpHost target = clientContext.getTargetHost();
        StringBuilder sb = new StringBuilder();
        sb.append(target.getSchemeName())
                .append("://")
                .append(target.getHostName());
        if (target.getPort() > 0) {
            sb.append(":").append(target.getPort());
        }
        if (!location.startsWith("/")) {
            sb.append("/");
        }
        sb.append(location);
        return sb.toString();
    }

}
