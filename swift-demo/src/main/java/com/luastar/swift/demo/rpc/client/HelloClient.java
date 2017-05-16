package com.luastar.swift.demo.rpc.client;

import com.luastar.swift.demo.rpc.api.HelloService;
import com.luastar.swift.demo.rpc.api.Person;
import com.luastar.swift.rpc.client.RpcReference;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;


@Component
public class HelloClient {

    @RpcReference
    private HelloService helloService;

    @RpcReference(version = "sample.hello2")
    private HelloService helloService2;

    public void test1() {
        String result = helloService.hello("World");
        System.out.println(result);
        String result2 = helloService2.hello("世界");
        System.out.println(result2);
    }

    public void test2() {
        try {
            String result = helloService.hello(new Person("Yong", "Huang"));
            System.out.println(result);
            File file = new File("/Users/zhuminghua/Downloads/output/test.txt");
            String result2 = helloService.hello(FileUtils.readFileToByteArray(file));
            System.out.println(result2);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void test3() {
        int loopCount = 100;
        long start = System.currentTimeMillis();
        for (int i = 0; i < loopCount; i++) {
            String result = helloService.hello("World");
            System.out.println(result);
        }
        long time = System.currentTimeMillis() - start;
        System.out.println("loop: " + loopCount);
        System.out.println("time: " + time + "ms");
        System.out.println("tps: " + (double) loopCount / ((double) time / 1000));
    }

}
