package com.luastar.swift.tools.func.user;

import com.luastar.swift.base.threads.NamedThreadFactory;
import com.luastar.swift.base.threads.SmartThreadPoolExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RenRenMain {

    private static final Logger logger = LoggerFactory.getLogger(RenRenMain.class);

    public static void main(String[] args) {
        // 线程池
        final ThreadPoolExecutor threadPoolExecutor = new SmartThreadPoolExecutor(
                5, 10, 120L, TimeUnit.SECONDS,
                new PriorityBlockingQueue<Runnable>(100),
                new NamedThreadFactory("getRenRenUser", true),
                new ThreadPoolExecutor.CallerRunsPolicy());
        // 线程池关闭钩子
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                logger.info("Shutdown getRenRenUser threadpool");
                if (threadPoolExecutor != null && !threadPoolExecutor.isShutdown()) {
                    threadPoolExecutor.shutdownNow();
                }
            }
        }));
        // 601724522
        for (long uid = 600396830L; uid <= 700000000L; uid++) {
            threadPoolExecutor.submit(new RenRenUserTask(uid));
        }
    }

}
