package com.luastar.swift.http.server;

import com.luastar.swift.http.constant.HttpConstant;
import com.luastar.swift.http.constant.HttpMediaType;
import com.luastar.swift.http.route.HandlerExecutionChain;
import com.luastar.swift.http.route.HandlerMethod;
import com.luastar.swift.http.route.HttpHandlerMapping;
import com.luastar.swift.http.route.HttpRequestHandler;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import io.netty.util.CharsetUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Map;

public class HttpChannelHandler extends SimpleChannelInboundHandler<HttpObject> {

    private static final Logger logger = LoggerFactory.getLogger(HttpChannelHandler.class);

    private static final String URI_FAVICON_ICO = "/favicon.ico";

    private final String requestId;
    private final HttpHandlerMapping handlerMapping;
    private HttpRequest httpRequest;
    private HttpResponse httpResponse;

    public HttpChannelHandler(HttpHandlerMapping handlerMapping) {
        // 使用自定义线程池异步执行时不能自动释放
        super(false);
        if (handlerMapping == null) {
            throw new IllegalArgumentException("handlerMapping不能为空！");
        }
        this.requestId = RandomStringUtils.random(20, true, true);
        this.handlerMapping = handlerMapping;
        // 在 worker-group 线程池中执行
        MDC.put(HttpConstant.MDC_KEY, requestId);
        logger.info("初始化HttpChannelHandler");
        MDC.remove(HttpConstant.MDC_KEY);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
        try {
            if (!(msg instanceof FullHttpRequest)) {
                return;
            }
            // 在  worker-group 或 executor-group 线程池中执行
            MDC.put(HttpConstant.MDC_KEY, requestId);
            FullHttpRequest fullHttpRequest = (FullHttpRequest) msg;
            if (HttpUtil.is100ContinueExpected(fullHttpRequest)) {
                ctx.write(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE));
                return;
            }
            if (URI_FAVICON_ICO.equals(fullHttpRequest.uri())) {
                ctx.writeAndFlush(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND)).addListener(ChannelFutureListener.CLOSE);
                return;
            }
            // 初始化HttpRequest
            httpRequest = new HttpRequest(fullHttpRequest, requestId, getSocketAddressIp(ctx));
            httpRequest.logRequest();
            // 初始化HttpResponse
            httpResponse = new HttpResponse(httpRequest.getRequestId());
            // 异步处理业务逻辑
            HttpThreadPoolExecutor.submit(requestId, () -> {
                // 在自定义线程池中执行
                MDC.put(HttpConstant.MDC_KEY, requestId);
                handleBusinessLogic(ctx);
                MDC.remove(HttpConstant.MDC_KEY);
            });
        } catch (Exception exception) {
            try {
                // 处理异常
                handlerMapping.exceptionHandler(httpRequest, httpResponse, exception);
                // 处理返回结果
                handleHttpResponse(ctx);
            } finally {
                // 销毁数据
                destroy();
            }
        } finally {
            MDC.remove(HttpConstant.MDC_KEY);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("业务逻辑处理异常......" + cause.getMessage(), cause);
        if (ctx.channel().isActive()) {
            ByteBuf buf = Unpooled.copiedBuffer("Failure: " + cause.getMessage() + "\r\n", CharsetUtil.UTF_8);
            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR, buf);
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpMediaType.TEXT_PLAIN_UTF_8);
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, buf.readableBytes());
            HttpUtil.setContentLength(response, buf.readableBytes());
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        }
    }

    /**
     * 获取通讯ip
     *
     * @param ctx
     * @return
     */
    protected String getSocketAddressIp(ChannelHandlerContext ctx) {
        SocketAddress socketAddress = ctx.channel().remoteAddress();
        if (socketAddress != null && socketAddress instanceof InetSocketAddress) {
            return ((InetSocketAddress) socketAddress).getAddress().getHostAddress();
        }
        return null;
    }

    /**
     * 处理业务逻辑
     *
     * @param ctx
     */
    protected void handleBusinessLogic(ChannelHandlerContext ctx) {
        try {
            logger.info("业务逻辑处理开始......");
            // 查找处理类方法
            HandlerExecutionChain mappedHandler = handlerMapping.getHandler(httpRequest);
            if (mappedHandler == null) {
                ctx.writeAndFlush(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND)).addListener(ChannelFutureListener.CLOSE);
                return;
            }
            // 拦截器处理前
            if (!mappedHandler.applyPreHandle(httpRequest, httpResponse)) {
                handleHttpResponse(ctx);
                return;
            }
            // 执行方法
            Object handler = mappedHandler.getHandler();
            if (handler instanceof HttpRequestHandler) {
                HttpRequestHandler requestHandler = (HttpRequestHandler) handler;
                requestHandler.handleRequest(httpRequest, httpResponse);
            } else if (handler instanceof HandlerMethod) {
                HandlerMethod handlerMethod = (HandlerMethod) handler;
                Method method = handlerMethod.getMethod();
                Object[] args = new Object[]{httpRequest, httpResponse};
                ReflectionUtils.makeAccessible(method);
                method.invoke(handlerMethod.getBean(), args);
            } else {
                logger.warn("not support handler : {}", handler);
            }
            // 拦截器处理后
            mappedHandler.applyPostHandle(httpRequest, httpResponse);
            // 处理返回结果
            handleHttpResponse(ctx);
        } catch (Exception exception) {
            try {
                // 处理异常
                handlerMapping.exceptionHandler(httpRequest, httpResponse, exception);
                // 处理返回结果
                handleHttpResponse(ctx);
            } catch (Exception e) {
                exceptionCaught(ctx, e);
            }
        } finally {
            logger.info("业务逻辑处理结束......");
            // 销毁数据
            destroy();
        }
    }

    /**
     * 处理返回结果
     *
     * @param ctx
     */
    protected void handleHttpResponse(ChannelHandlerContext ctx) {
        // 请求体日志
        httpResponse.logResponse();
        // 处理返回值
        FullHttpResponse response;
        int contentLength = 0;
        if (StringUtils.isEmpty(httpResponse.getResult())) {
            // 输出流
            if (httpResponse.getOutputStream() == null) {
                response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, httpResponse.getStatus());
            } else {
                // 使用copiedBuffer会导致excel等文档打不开
                ByteBuf buf = Unpooled.wrappedBuffer(httpResponse.getOutputStream().toByteArray());
                response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, httpResponse.getStatus(), buf);
                contentLength = buf.readableBytes();
            }
        } else {
            // 输出结果
            ByteBuf buf = Unpooled.copiedBuffer(httpResponse.getResult(), CharsetUtil.UTF_8);
            response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, httpResponse.getStatus(), buf);
            contentLength = buf.readableBytes();
        }
        // 处理返回头信息
        for (Map.Entry<String, String> resHeader : httpResponse.getHeaderMap().entrySet()) {
            response.headers().set(resHeader.getKey(), resHeader.getValue());
        }
        // 处理cookie
        if (CollectionUtils.isNotEmpty(httpResponse.getCookieSet())) {
            for (Cookie cookie : httpResponse.getCookieSet()) {
                response.headers().add(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.STRICT.encode(cookie));
            }
        }
        // 输出返回结果
        HttpUtil.setContentLength(response, contentLength);
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    /**
     * 销毁数据
     */
    protected void destroy() {
        logger.info("业务数据销毁......");
        // 销毁数据
        if (httpRequest != null) {
            httpRequest.destroy();
            httpRequest = null;
        }
        if (httpResponse != null) {
            httpResponse.destroy();
            httpResponse = null;
        }
    }

}

