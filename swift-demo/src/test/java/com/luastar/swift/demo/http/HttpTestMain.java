package com.luastar.swift.demo.http;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.luastar.swift.base.net.HttpClientUtils;

import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * http server 测试
 */
public class HttpTestMain {

    public static void main(String[] args) {
        // 测试线程池
        ThreadPoolExecutor testThreadPoolExecutor = new ThreadPoolExecutor(
                16,
                100,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(102400),
                new ThreadFactoryBuilder().setNameFormat("test-%d").build(),
                new ThreadPoolExecutor.AbortPolicy());
        for (int i = 0; i < 50000; i++) {
            testThreadPoolExecutor.submit(() -> {
                testGet();
            });
            testThreadPoolExecutor.submit(() -> {
                testPostForm();
            });
            testThreadPoolExecutor.submit(() -> {
                testPostJson();
            });
            if (i <= 30) {
                testThreadPoolExecutor.submit(() -> {
                    testTimeConsuming();
                });
            }
        }
    }

    private static void testGet() {
        System.out.println(HttpClientUtils.get("http://localhost:8081/test?name=zhuminghua&sex=1"));
    }

    private static void testPostForm() {
        Map<String, String> param = Maps.newHashMap();
        param.put("name", "zhuminghua");
        param.put("sex", "1");
        System.out.println(HttpClientUtils.post("http://localhost:8081/test/getpost", param));
    }

    private static void testPostJson() {
        Map<String, String> param = Maps.newHashMap();
        param.put("name", "zhuminghua");
        param.put("sex", "1");
        System.out.println(HttpClientUtils.postBody("http://localhost:8081/test/getpost", JSON.toJSONString(param)));
    }

    private static void testTimeConsuming() {
        System.out.println(HttpClientUtils.get("http://localhost:8081/test/get_or_post"));
    }

}
