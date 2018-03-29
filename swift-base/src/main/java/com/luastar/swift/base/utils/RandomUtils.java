package com.luastar.swift.base.utils;

import org.apache.commons.lang3.RandomStringUtils;
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

    private static final String NUMBER_CHAR = "0123456789";
    private static final String LETTER_CHAR = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String ALL_CHAR = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String NUMBER_LOWER_LETTER_CHAR = "0123456789abcdefghijklmnopqrstuvwxyz";

    /**
     * 封装JDK自带的UUID, 通过Random数字生成, 中间有-分割.
     */
    public static String uuid() {
        return UUID.randomUUID().toString();
    }

    /**
     * 封装JDK自带的UUID, 通过Random数字生成, 中间无-分割.
     */
    public static String uuidNoDelimiter() {
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
     * 包括数字和字母
     */
    public static String randomStr(int length) {
        return RandomStringUtils.randomAlphanumeric(length);
    }

    /**
     * 只包括数字
     *
     * @param length
     * @return
     */
    public static String randomNum(int length) {
        return RandomStringUtils.randomNumeric(length);
    }

    /**
     * 只包括字母
     *
     * @param length
     * @return
     */
    public static String randomLetter(int length) {
        return RandomStringUtils.randomAlphabetic(length);
    }

    /**
     * 只包括数字和小写字母
     *
     * @param length
     * @return
     */
    public static String randomNumAndLowerLetter(int length) {
        if (length == 0) {
            return "";
        } else if (length < 0) {
            throw new IllegalArgumentException("Requested random string length " + length + " is less than 0.");
        }
        int len = NUMBER_LOWER_LETTER_CHAR.length();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(NUMBER_LOWER_LETTER_CHAR.charAt(randomInt(len)));
        }
        return sb.toString();
    }

}
