package com.luastar.swift.http.server;


import com.luastar.swift.base.utils.ObjUtils;
import com.luastar.swift.http.constant.HttpConstant;
import com.luastar.swift.http.constant.HttpMediaType;
import com.luastar.swift.http.route.HttpHandlerMapping;
import com.luastar.swift.http.route.HttpRequestHandler;
import com.luastar.swift.http.route.handlermapping.HandlerExecutionChain;
import com.luastar.swift.http.route.handlermapping.HandlerMethod;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class HttpChannelHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static final Logger logger = LoggerFactory.getLogger(HttpChannelHandler.class);

    private final HttpHandlerMapping handlerMapping;

    /**
     * 在 worker-group 线程池中执行
     *
     * @param handlerMapping
     */
    public HttpChannelHandler(HttpHandlerMapping handlerMapping) {
        // 使用自定义线程池异步执行时不能自动释放
        super(false);
        logger.info("初始化HttpChannelHandler");
        if (handlerMapping == null) {
            throw new IllegalArgumentException("handlerMapping can't be null.");
        }
        this.handlerMapping = handlerMapping;
    }

    /**
     * 在  worker-group 或 executor-group 线程池中执行
     *
     * @param ctx
     * @param fullHttpRequest
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest fullHttpRequest) throws Exception {
        // 异步处理业务逻辑
        HttpThreadPoolExecutor.submit(() -> {
            final long startTime = System.currentTimeMillis();
            final String requestId = RandomStringUtils.random(20, true, true);
            HttpRequest httpRequest = null;
            HttpResponse httpResponse = null;
            try {
                MDC.put(HttpConstant.MDC_KEY, requestId);
                logger.info("业务逻辑处理开始......");
                if (HttpUtil.is100ContinueExpected(fullHttpRequest)) {
                    DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE);
                    ctx.write(response);
                    return;
                }
                if (!fullHttpRequest.decoderResult().isSuccess()) {
                    FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST);
                    ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
                    return;
                }
                // 初始化HttpRequest
                httpRequest = new HttpRequest(fullHttpRequest, requestId, getSocketAddressIp(ctx));
                httpRequest.logRequest();
                // 初始化HttpResponse
                httpResponse = new HttpResponse(httpRequest.getRequestId());
                // 查找处理类方法
                HandlerExecutionChain mappedHandler = handlerMapping.getActualHandler(httpRequest);
                if (mappedHandler == null) {
                    DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND);
                    ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
                    return;
                }
                // 拦截器处理前
                if (!mappedHandler.applyPreHandle(httpRequest, httpResponse)) {
                    handleHttpResponse(ctx, httpRequest, httpResponse);
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
                handleHttpResponse(ctx, httpRequest, httpResponse);
            } catch (Throwable e) {
                try {
                    // 自定义异常处理
                    if (e instanceof InvocationTargetException) {
                        // 业务异常
                        handlerMapping.exceptionHandle(httpRequest, httpResponse, ((InvocationTargetException) e).getTargetException());
                    } else {
                        // 其他异常
                        handlerMapping.exceptionHandle(httpRequest, httpResponse, e);
                    }
                    // 处理返回结果
                    handleHttpResponse(ctx, httpRequest, httpResponse);
                } catch (Throwable ex) {
                    // 系统异常处理
                    exceptionCaught(ctx, ex);
                }
            } finally {
                logger.info("业务数据销毁......");
                // 销毁数据
                if (httpRequest != null) {
                    httpRequest.destroy();
                }
                if (httpResponse != null) {
                    httpResponse.destroy();
                }
                logger.info("业务逻辑处理结束，耗时{}毫秒......", System.currentTimeMillis() - startTime);
                MDC.remove(HttpConstant.MDC_KEY);
            }
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        String errMsg = "exceptionCaught: " + ObjUtils.ifNull(cause.getMessage(), "");
        logger.error(errMsg, cause);
        if (ctx.channel().isActive()) {
            ByteBuf buf = Unpooled.copiedBuffer(errMsg, CharsetUtil.UTF_8);
            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR, buf);
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpMediaType.TEXT_PLAIN_UTF_8);
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
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
     * 处理返回结果
     *
     * @param ctx
     */
    protected void handleHttpResponse(ChannelHandlerContext ctx, HttpRequest httpRequest, HttpResponse httpResponse) {
        // 返回值处理
        if (HttpUtil.isKeepAlive(httpRequest.getFullHttpRequest())) {
            httpResponse.setHeader(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
            ctx.writeAndFlush(httpResponse.getFullHttpResponse());
        } else {
            ctx.writeAndFlush(httpResponse.getFullHttpResponse()).addListener(ChannelFutureListener.CLOSE);
        }
        // 请求体日志
        httpResponse.logResponse();
    }

}

