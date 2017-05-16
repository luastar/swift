package com.luastar.swift.demo.rpc.api;

public interface HelloService {

    String hello(String name);

    String hello(Person person);

    String hello(byte[] data);

}
