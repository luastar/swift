package com.luastar.swift.http.server;


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
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class HttpChannelHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static final Logger logger = LoggerFactory.getLogger(HttpChannelHandler.class);

    private static final String MDC_KEY = "requestId";

    private final HttpHandlerMapping handlerMapping;

    private HttpRequest httpRequest;

    private HttpResponse httpResponse;

    public HttpChannelHandler(HttpHandlerMapping handlerMapping) {
        if (handlerMapping == null) {
            throw new IllegalArgumentException("handlerMapping can't be null.");
        }
        this.handlerMapping = handlerMapping;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest fullHttpRequest) throws Exception {
        // requestId
        String requestId = RandomStringUtils.random(20, true, true);
        MDC.put(MDC_KEY, requestId);
        if (!fullHttpRequest.decoderResult().isSuccess()) {
            ctx.writeAndFlush(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST)).addListener(ChannelFutureListener.CLOSE);
            return;
        }
        if (HttpUtil.is100ContinueExpected(fullHttpRequest)) {
            ctx.write(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE));
            return;
        }
        // 初始化HttpRequest
        httpRequest = new HttpRequest(fullHttpRequest, requestId, getSocketAddressIp(ctx));
        httpRequest.logRequest();
        // 查找处理类方法
        HandlerExecutionChain mappedHandler = handlerMapping.getActualHandler(httpRequest);
        if (mappedHandler == null) {
            logger.warn("handler not find");
            ctx.writeAndFlush(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND)).addListener(ChannelFutureListener.CLOSE);
            return;
        }
        // 初始化HttpResponse
        httpResponse = new HttpResponse(httpRequest.getRequestId());
        // 耗时任务交给EventExecutorGroup线程池处理
        ctx.executor().submit(() -> {
            try {
                // 拦截器处理前
                if (!mappedHandler.applyPreHandle(httpRequest, httpResponse)) {
                    writeHttpResponse(ctx);
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
                // 返回处理结果
                writeHttpResponse(ctx);
            } catch (Exception exception) {
                // 处理异常
                if (handlerMapping.getExceptionHandler() == null) {
                    httpResponse.setResponseContentTypePlain();
                    httpResponse.setResult("Failure: " + exception.getMessage() + "\r\n");
                } else {
                    handlerMapping.getExceptionHandler().exceptionHandle(httpRequest, httpResponse, exception);
                }
                // 返回处理结果
                writeHttpResponse(ctx);
            } finally {
                destroy();
            }
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error(cause.getMessage(), cause);
        if (ctx.channel().isActive()) {
            ByteBuf buf = Unpooled.copiedBuffer("Failure: " + cause.getMessage() + "\r\n", CharsetUtil.UTF_8);
            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR, buf);
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpMediaType.TEXT_PLAIN_UTF_8);
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, buf.readableBytes());
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
    protected void writeHttpResponse(ChannelHandlerContext ctx) {
        // 返回值处理
        ctx.writeAndFlush(httpResponse.getFullHttpResponse()).addListener(ChannelFutureListener.CLOSE);
        // 请求体日志
        httpResponse.logResponse();
    }

    /**
     * 销毁对象
     */
    protected void destroy() {
        if (httpRequest != null) {
            httpRequest.destroy();
            httpRequest = null;
        }
        if (httpResponse != null) {
            IOUtils.closeQuietly(httpResponse.getOutputStream());
            httpResponse = null;
        }
        MDC.remove(MDC_KEY);
    }

}

