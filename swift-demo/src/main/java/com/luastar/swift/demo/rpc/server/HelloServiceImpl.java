package com.luastar.swift.demo.rpc.server;


import com.luastar.swift.demo.rpc.api.Person;
import com.luastar.swift.demo.rpc.api.HelloService;
import com.luastar.swift.rpc.server.RpcService;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

@RpcService(HelloService.class)
public class HelloServiceImpl implements HelloService {

    public String hello(String name) {
        return "Hello! " + name;
    }

    public String hello(Person person) {
        return "Hello! " + person.getFirstName() + " " + person.getLastName();
    }

    public String hello(byte[] data) {
        try {
            File file = new File("/Users/zhuminghua/Downloads/output/hello.txt");
            FileUtils.writeByteArrayToFile(file, data);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Hello! ";
    }

}
