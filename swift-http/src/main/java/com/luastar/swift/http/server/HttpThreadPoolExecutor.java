package com.luastar.swift.http.server;

import com.luastar.swift.base.thread.QueuableCachedThreadPool;
import com.luastar.swift.base.thread.ThreadPoolBuilder;
import com.luastar.swift.base.thread.ThreadPoolUtils;
import com.luastar.swift.http.constant.HttpConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * http server 线程池
 * 这里使用tomcat的线程池，更适用于业务处理
 * java 线程池 先创建核心线程，满了后放入队列，队列满了后创建临时线程，超过最大线程数后执行拒绝策略
 * tomcat 线程池 先创建核心线程，满了后创建临时线程，超过最大线程数后加入到队列
 */
public class HttpThreadPoolExecutor {

    private static final Logger logger = LoggerFactory.getLogger(HttpThreadPoolExecutor.class);

    private static QueuableCachedThreadPool threadPoolExecutor;

    /**
     * 获取主线程池
     *
     * @return
     */
    private static QueuableCachedThreadPool getThreadPoolExecutor() {
        if (threadPoolExecutor == null) {
            synchronized (HttpThreadPoolExecutor.class) {
                if (threadPoolExecutor == null) {
                    threadPoolExecutor = ThreadPoolBuilder.queuableCachedPool()
                            .setThreadNamePrefix("business-group")
                            .setDaemon(true)
                            .setMinSize(HttpConstant.SWIFT_BUSINESS_THREADS)
                            .setMaxSize(Math.min(HttpConstant.SWIFT_BUSINESS_THREADS * 4, 512))
                            .setKeepAliveSecs(60)
                            .setQueueSize(1024)
                            .build();
                }
            }
        }
        return threadPoolExecutor;
    }

    /**
     * 添加任务
     *
     * @param task
     * @return
     */
    public static void submit(Runnable task) throws Exception {
        /*
        if (logger.isDebugEnabled()) {
            logger.debug("===线程池信息开始=========================================================");
            logger.debug("== queueSize(当前线程队列大小) : {}", getThreadPoolExecutor().getQueue().size());
            logger.debug("== queueRemainingCapacity(剩余线程队列大小) : {}", getThreadPoolExecutor().getQueue().remainingCapacity());
            logger.debug("== minThreads(核心线程数) : {}", getThreadPoolExecutor().getCorePoolSize());
            logger.debug("== maxThreads(最大线程数) : {}", getThreadPoolExecutor().getMaximumPoolSize());
            logger.debug("== poolSize(当前线程数) : {}", getThreadPoolExecutor().getPoolSize());
            logger.debug("== activeCount(活动线程数) : {}", getThreadPoolExecutor().getActiveCount());
            logger.debug("===线程池信息结束=========================================================");
        }
        */
        getThreadPoolExecutor().submit(task);
    }

    /**
     * 关闭线程池
     */
    public static void shutdownGracefully() {
        ThreadPoolUtils.gracefulShutdown(getThreadPoolExecutor(), 60, TimeUnit.SECONDS);
    }

}
