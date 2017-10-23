/**
 * Copyright (c) 2005-2012 springside.org.cn
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.luastar.swift.base.utils;

import org.bson.types.ObjectId;

import java.security.SecureRandom;
import java.util.UUID;

/**
 * 封装各种生成唯一性ID算法的工具类.
 *
 * @author calvin
 */
public class RandomUtils {

    private static SecureRandom random = new SecureRandom();
    private static final String ALL_CHAR = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LETTER_CHAR = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String NUMBER_LOWERLETTER__CHAR = "0123456789abcdefghijklmnopqrstuvwxyz";

    /**
     * 封装JDK自带的UUID, 通过Random数字生成, 中间有-分割.
     */
    public static String uuid() {
        return UUID.randomUUID().toString();
    }

    /**
     * 封装JDK自带的UUID, 通过Random数字生成, 中间无-分割.
     */
    public static String uuid2() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    /**
     * 生成bson id
     *
     * @return
     */
    public static String bsonId() {
        return ObjectId.get().toString();
    }

    /**
     * 使用SecureRandom随机生成${max}以内的int.
     */
    public static int randomInt(int max) {
        return Math.abs(random.nextInt(max));
    }

    /**
     * 使用SecureRandom随机生成${min}和${max}之间的int.
     */
    public static int randomInt(int min, int max) {
        return Math.abs(random.nextInt(max - min) + min);
    }

    /**
     * 使用SecureRandom随机生成Long.
     */
    public static long randomLong() {
        return Math.abs(random.nextLong());
    }

    /**
     * 基于Base62编码的SecureRandom随机生成bytes.
     */
    public static String randomStr(int length) {
        byte[] randomBytes = new byte[length];
        random.nextBytes(randomBytes);
        return EncodeUtils.encodeBase62(randomBytes);
    }

    /**
     * 只包括数字和小写字母
     *
     * @param length
     * @return
     */
    public static String randomStr2(int length) {
        StringBuffer sb = new StringBuffer();
        int len = NUMBER_LOWERLETTER__CHAR.length();
        for (int i = 0; i < length; i++) {
            sb.append(NUMBER_LOWERLETTER__CHAR.charAt(randomInt(len)));
        }
        return sb.toString();
    }

}
