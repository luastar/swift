package com.luastar.swift.http.server;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.luastar.swift.base.utils.SpringUtils;
import com.luastar.swift.http.constant.HttpConstant;
import com.luastar.swift.http.route.HttpHandlerMapping;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class HttpServer {

    private static final Logger logger = LoggerFactory.getLogger(HttpServer.class);

    private int port = 8080;

    private boolean ssl = false;

    private HttpHandlerMapping handlerMapping;

    public HttpServer(int port) {
        this(port, false);
    }

    public HttpServer(int port, boolean ssl) {
        this.port = port;
        this.ssl = ssl;
        initHandlerMapping();
    }

    /**
     * 初始化handlerMapping
     */
    protected void initHandlerMapping() {
        logger.info("启动端口号:{}", port);
        logger.info("spring配置文件路径:{}", HttpConstant.SWIFT_CONFIG_LOCATION);
        logger.info("业务执行超时时间:{}秒", HttpConstant.SWIFT_EXECUTE_TIMEOUT);
        logger.info("最大包大小:{}KB, 输出日志大小:{}KB", HttpConstant.SWIFT_MAX_CONTENT_LENGTH / 1024, HttpConstant.SWIFT_MAX_LOG_LENGTH / 1024);
        logger.info("boss线程数:{}，worker线程数:{}, business线程数:{}", HttpConstant.SWIFT_BOSS_THREADS, HttpConstant.SWIFT_WORKER_THREADS, HttpConstant.SWIFT_BUSINESS_THREADS);
        logger.info("返回结果压缩级别:{}", HttpConstant.SWIFT_COMPRESSION_LEVEL);
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext(HttpConstant.SWIFT_CONFIG_LOCATION);
        SpringUtils.setApplicationContext(applicationContext);
        this.handlerMapping = applicationContext.getBean(HttpHandlerMapping.class);
        if (handlerMapping == null) {
            throw new RuntimeException("handlerMapping is null!");
        }
    }

    public void start() {
        // 设置线程名称
        ThreadFactoryBuilder threadFactoryBuilder = new ThreadFactoryBuilder();
        EventLoopGroup bossGroup = new NioEventLoopGroup(HttpConstant.SWIFT_BOSS_THREADS, threadFactoryBuilder.setNameFormat("boss-group-%d").build());
        EventLoopGroup workerGroup = new NioEventLoopGroup(HttpConstant.SWIFT_WORKER_THREADS, threadFactoryBuilder.setNameFormat("worker-group-%d").build());
        /**
         *  备注：
         *  netty推荐耗时业务handler放到单独的线程池
         *  但该线程池是顺序执行的，如果当前线程被阻塞，则本次请求会被放进当前线程队列，而不是找空闲的线程执行
         *  因此，使用自己的线程池会更加灵活
         */
        // EventExecutorGroup executorGroup = new DefaultEventExecutorGroup(HttpConstant.SWIFT_BUSINESS_THREADS, threadFactoryBuilder.setNameFormat("executor-group-%d").build());
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            if (ssl) {
                                SelfSignedCertificate ssc = new SelfSignedCertificate();
                                SslContext sslContext = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
                                pipeline.addLast(sslContext.newHandler(ch.alloc()));
                            }
                            // http request decode and response encode
                            pipeline.addLast(new HttpServerCodec());
                            // 将消息头和体聚合成FullHttpRequest和FullHttpResponse
                            pipeline.addLast(new HttpObjectAggregator(HttpConstant.SWIFT_MAX_CONTENT_LENGTH));
                            // 压缩处理
                            pipeline.addLast(new HttpContentCompressor(HttpConstant.SWIFT_COMPRESSION_LEVEL));
                            // 自定义http服务
                            // pipeline.addLast(executorGroup, "http-handler", new HttpChannelHandler(handlerMapping));
                            pipeline.addLast(new HttpChannelHandler(handlerMapping));
                        }
                    });
            Channel channel = bootstrap.bind(port).sync().channel();
            channel.closeFuture().sync();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            //executorGroup.shutdownGracefully();
            HttpThreadPoolExecutor.shutdownGracefully();
        }
    }

}
