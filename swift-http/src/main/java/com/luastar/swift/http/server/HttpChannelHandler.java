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
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
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
        try {
            if (!fullHttpRequest.decoderResult().isSuccess()) {
                return;
            }
            // requestId
            String requestId = RandomStringUtils.random(20, true, true);
            MDC.put(MDC_KEY, requestId);
            if (HttpUtil.is100ContinueExpected(fullHttpRequest)) {
                ctx.write(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE));
                return;
            }
            // 初始化HttpRequest
            httpRequest = new HttpRequest(fullHttpRequest, requestId, getSocketAddressIp(ctx));
            httpRequest.logRequest();
            // 查找处理类方法
            HandlerExecutionChain mappedHandler = handlerMapping.getHandler(httpRequest);
            if (mappedHandler == null) {
                logger.warn("handler not find.");
                FullHttpResponse fullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND);
                ctx.writeAndFlush(fullHttpResponse).addListener(ChannelFutureListener.CLOSE);
                return;
            }
            // 初始化HttpResponse
            httpResponse = new HttpResponse(httpRequest.getRequestId());
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
            // 返回处理结果
            handleHttpResponse(ctx);
        } catch (Exception exception) {
            // 处理异常
            if (handlerMapping.getExceptionHandler() != null) {
                handlerMapping.getExceptionHandler().exceptionHandle(httpRequest, httpResponse, exception);
            }
            // 返回处理结果
            handleHttpResponse(ctx);
        } finally {
            destroy();
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
    protected void handleHttpResponse(ChannelHandlerContext ctx) {
        // 请求体日志
        httpResponse.logResponse();
        // 返回值处理
        FullHttpResponse fullHttpResponse = httpResponse.getFullHttpResponse();
        // 输出返回结果
        /*
        boolean keepAlive = HttpUtil.isKeepAlive(httpRequest.getFullHttpRequest());
        if (keepAlive) {
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
            ctx.writeAndFlush(response);
        } else {
            HttpUtil.setContentLength(response, contentLength);
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        }
        */
        // 不处理keepAlive，直接返回结果
        HttpUtil.setContentLength(fullHttpResponse, httpResponse.getContentLength());
        ctx.writeAndFlush(fullHttpResponse).addListener(ChannelFutureListener.CLOSE);
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

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error(cause.getMessage(), cause);
        if (ctx.channel().isActive()) {
            ByteBuf buf = Unpooled.copiedBuffer("Failure: " + cause.getMessage() + "\r\n", CharsetUtil.UTF_8);
            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR, buf);
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpMediaType.TEXT_PLAIN_UTF_8);
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (ctx.channel().isActive() && IdleState.ALL_IDLE.equals(event.state())) {
                logger.info("close timeout channel {}", ctx.channel().id());
                ctx.channel().close();
            }
        }
    }

}

