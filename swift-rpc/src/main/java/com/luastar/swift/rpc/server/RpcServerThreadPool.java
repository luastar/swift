package com.luastar.swift.rpc.server;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RpcServerThreadPool {

    private static ListeningExecutorService threadPoolExecutor;

    public static ListeningExecutorService getThreadPoolExecutor() {
        if (threadPoolExecutor == null) {
            synchronized (RpcServerThreadPool.class) {
                if (threadPoolExecutor == null) {
                    ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(16, 16, 600L, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(65536));
                    threadPoolExecutor = MoreExecutors.listeningDecorator(poolExecutor);
                }
            }
        }
        return threadPoolExecutor;
    }

}
