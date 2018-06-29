package com.luastar.swift.http.server;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.luastar.swift.http.constant.HttpConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.concurrent.*;

/**
 * http server 线程池
 */
public class HttpThreadPoolExecutor {

    private static final Logger logger = LoggerFactory.getLogger(HttpThreadPoolExecutor.class);

    private static ThreadPoolExecutor mainThreadPoolExecutor;
    private static ThreadPoolExecutor killThreadPoolExecutor;

    /**
     * 获取主线程池
     *
     * @return
     */
    private static ThreadPoolExecutor getMainThreadPoolExecutor() {
        if (mainThreadPoolExecutor == null) {
            synchronized (HttpThreadPoolExecutor.class) {
                if (mainThreadPoolExecutor == null) {
                    mainThreadPoolExecutor = new ThreadPoolExecutor(
                            HttpConstant.SWIFT_BUSINESS_THREADS,
                            HttpConstant.SWIFT_BUSINESS_THREADS,
                            0L, TimeUnit.MILLISECONDS,
                            new LinkedBlockingQueue<>(102400),
                            new ThreadFactoryBuilder().setNameFormat("business-%d").build(),
                            new ThreadPoolExecutor.AbortPolicy());
                }
            }
        }
        return mainThreadPoolExecutor;
    }

    /**
     * 获取杀手线程池（守护线程）
     *
     * @return
     */
    private static ThreadPoolExecutor getKillThreadPoolExecutor() {
        if (killThreadPoolExecutor == null) {
            synchronized (HttpThreadPoolExecutor.class) {
                if (killThreadPoolExecutor == null) {
                    killThreadPoolExecutor = new ThreadPoolExecutor(
                            HttpConstant.SWIFT_BUSINESS_THREADS,
                            HttpConstant.SWIFT_BUSINESS_THREADS,
                            0L, TimeUnit.MILLISECONDS,
                            new LinkedBlockingQueue<>(102400),
                            new ThreadFactoryBuilder().setNameFormat("business-kill-%d").setDaemon(true).build(),
                            new ThreadPoolExecutor.AbortPolicy());
                }
            }
        }
        return killThreadPoolExecutor;
    }

    /**
     * 添加任务
     *
     * @param task
     * @return
     */
    public static Future<?> submit(String requestId, Runnable task) {
        logger.info("---线程池信息开始---------------------------------------------------------");
        logger.info("-- queueSize(线程队列大小) : {}", getMainThreadPoolExecutor().getQueue().size());
        logger.info("-- queueRemainingCapacity(线程队列剩余) : {}", getMainThreadPoolExecutor().getQueue().remainingCapacity());
        logger.info("-- corePoolSize(核心线程数) : {}", getMainThreadPoolExecutor().getCorePoolSize());
        logger.info("-- maxPoolSize(最大线程数) : {}", getMainThreadPoolExecutor().getMaximumPoolSize());
        logger.info("-- poolSize(当前线程数) : {}", getMainThreadPoolExecutor().getPoolSize());
        logger.info("-- activeCount(活动线程数) : {}", getMainThreadPoolExecutor().getActiveCount());
        logger.info("-- taskCount(总任务数) : {}", getMainThreadPoolExecutor().getTaskCount());
        logger.info("-- completedTaskCount(已完成任务数) : {}", getMainThreadPoolExecutor().getCompletedTaskCount());
        logger.info("---线程池信息结束---------------------------------------------------------");
        Future<?> future = getMainThreadPoolExecutor().submit(task);
        getKillThreadPoolExecutor().submit(() -> {
            try {
                // 在自定义线程池中执行
                MDC.put(HttpConstant.MDC_KEY, requestId);
                future.get(HttpConstant.SWIFT_EXECUTE_TIMEOUT, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                logger.error("任务执行中断，强制关闭线程......");
                future.cancel(true);
            } catch (ExecutionException e) {
                logger.error("任务执行异常，强制关闭线程......");
                future.cancel(true);
            } catch (TimeoutException e) {
                logger.error("任务执行超时，强制关闭线程......");
                future.cancel(true);
            } finally {
                MDC.remove(HttpConstant.MDC_KEY);
            }
        });
        return future;
    }

    /**
     * 关闭线程池
     */
    public static void shutdownGracefully() {
        getMainThreadPoolExecutor().shutdown();
        getKillThreadPoolExecutor().shutdown();
    }

}
