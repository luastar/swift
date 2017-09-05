package com.luastar.swift.demo.rpc.api;

import com.luastar.swift.demo.rpc.entity.Person;

public interface HelloService {

    String hello(String name);

    String hello(Person person);

    String hello(byte[] data);

}
