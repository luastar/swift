package com.luastar.swift.http.server;

import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.luastar.swift.http.constant.HttpConstant;

import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * http server 线程池
 */
public class HttpThreadPoolExecutor {

    private static ThreadPoolExecutor threadPoolExecutor;

    /**
     * 获取线程池
     * @return
     */
    public static ThreadPoolExecutor getThreadPoolExecutor() {
        if (threadPoolExecutor == null) {
            synchronized (HttpThreadPoolExecutor.class) {
                if (threadPoolExecutor == null) {
                    threadPoolExecutor = new ThreadPoolExecutor(
                            HttpConstant.SWIFT_BUSINESS_THREADS,
                            HttpConstant.SWIFT_BUSINESS_THREADS,
                            0L, TimeUnit.MILLISECONDS,
                            new LinkedBlockingQueue<>(102400),
                            new ThreadFactoryBuilder().setNameFormat("business-%d").build(),
                            new ThreadPoolExecutor.AbortPolicy());
                }
            }
        }
        return threadPoolExecutor;
    }

    /**
     * 获取线程池信息
     * @return
     */
    public static Map getThreadPoolInfo() {
        Map<String, Object> threadPoolInfo = Maps.newLinkedHashMap();
        threadPoolInfo.put("queueSize(线程队列数)", getThreadPoolExecutor().getQueue().size());
        threadPoolInfo.put("queueRemainingCapacity(线程队列剩余数)", getThreadPoolExecutor().getQueue().remainingCapacity());
        threadPoolInfo.put("maxPoolSize(最大线程数)", getThreadPoolExecutor().getMaximumPoolSize());
        threadPoolInfo.put("poolSize(当前线程数)", getThreadPoolExecutor().getPoolSize());
        threadPoolInfo.put("activeCount(活动线程数)", getThreadPoolExecutor().getActiveCount());
        threadPoolInfo.put("taskCount(总任务数)", getThreadPoolExecutor().getTaskCount());
        threadPoolInfo.put("completedTaskCount(已完成任务数)", getThreadPoolExecutor().getCompletedTaskCount());
        return threadPoolInfo;
    }

    /**
     * 关闭线程池
     */
    public static void shutdownGracefully() {
        threadPoolExecutor.shutdown();
    }

}
