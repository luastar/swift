package com.luastar.swift.demo.rpc.client;


import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ClientMain {

    public static void main(String[] args) throws Exception {

        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:spring/spring-swift-client.xml");
        HelloClient helloClient = applicationContext.getBean(HelloClient.class);
        helloClient.test1();
        helloClient.test2();
        helloClient.test3();

    }

}
