package com.luastar.swift.rpc.server;

import com.google.common.collect.Maps;
import com.luastar.swift.base.utils.ClassLoaderUtils;
import com.luastar.swift.base.utils.SpringUtils;
import com.luastar.swift.rpc.constant.RpcConstant;
import com.luastar.swift.rpc.registry.IServiceRegistry;
import com.luastar.swift.rpc.serialize.IRpcSerialize;
import com.luastar.swift.rpc.serialize.RpcDecoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import com.luastar.swift.rpc.serialize.RpcEncoder;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Map;

/**
 * RPC 服务器（用于发布 RPC 服务）
 * 参考：
 * 1、http://git.oschina.net/huangyong/rpc
 * 2、https://github.com/luxiaoxun/NettyRpc
 */
public class RpcServer {

    private static final Logger logger = LoggerFactory.getLogger(RpcServer.class);

    private int port = 8090;

    private IServiceRegistry serviceRegistry;

    private IRpcSerialize rpcSerialize;

    private Map<String, Object> handlerMap;

    public RpcServer(int port) {
        this.port = port;
        this.serviceRegistry = ClassLoaderUtils.getInstance(RpcConstant.getRegistryType().getRegistryClass());
        this.rpcSerialize = ClassLoaderUtils.getInstance(RpcConstant.getSerializeType().getSerializeClass());
        initHandlerMap();
    }

    protected void initHandlerMap() {
        this.handlerMap = Maps.newHashMap();
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext(RpcConstant.SWIFT_CONFIG_LOCATION);
        SpringUtils.setApplicationContext(applicationContext);
        Map<String, Object> serviceBeanMap = applicationContext.getBeansWithAnnotation(RpcService.class);
        if (MapUtils.isNotEmpty(serviceBeanMap)) {
            for (Object serviceBean : serviceBeanMap.values()) {
                RpcService rpcService = serviceBean.getClass().getAnnotation(RpcService.class);
                String serviceName = rpcService.value().getName();
                String serviceVersion = rpcService.version();
                if (StringUtils.isNotEmpty(serviceVersion)) {
                    serviceName += "-" + serviceVersion;
                }
                handlerMap.put(serviceName, serviceBean);
            }
        }
    }

    public void start() {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel channel) throws Exception {
                            ChannelPipeline pipeline = channel.pipeline();
                            pipeline.addLast(new LengthFieldBasedFrameDecoder(65536, 0, 4, 0, 0));
                            pipeline.addLast(new RpcDecoder(RpcRequest.class, rpcSerialize));
                            pipeline.addLast(new RpcEncoder(RpcResponse.class, rpcSerialize));
                            pipeline.addLast(new RpcServerChannelHandler(handlerMap));
                        }
                    });
            bootstrap.option(ChannelOption.SO_BACKLOG, 1024);
            bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
            Channel channel = bootstrap.bind(port).sync().channel();
            logger.info("SwiftRpcServer started on port {}", port);
            // 注册 RPC 服务地址
            String serviceAddress = RpcConstant.CURRENT_SERVER_ADDRESS + ":" + port;
            for (String interfaceName : handlerMap.keySet()) {
                serviceRegistry.register(interfaceName, serviceAddress);
            }
            channel.closeFuture().sync();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

}
