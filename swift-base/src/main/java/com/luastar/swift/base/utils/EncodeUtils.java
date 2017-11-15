/**
 * Copyright (c) 2005-2012 springside.org.cn
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.luastar.swift.base.utils;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * 封装各种格式的编码解码工具类.
 * <p/>
 * 1.Commons-Codec的 hex/base64 编码
 * 2.自制的base62 编码
 * 3.Commons-Lang的xml/html escape
 * 4.JDK提供的URLEncoder
 */
public class EncodeUtils {

    private static final String DEFAULT_URL_ENCODING = "UTF-8";
    private static final char[] BASE62 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();

    /**
     * Md5编码.
     */
    public static String md5(String input) {
        return DigestUtils.md5Hex(input);
    }

    /**
     * 文件Md5编码.
     */
    public static String fileMd5(File input) {
        InputStream is = null;
        try {
            is = FileUtils.openInputStream(input);
            return DigestUtils.md5Hex(is);
        } catch (IOException e) {
            throw ExceptionUtils.unchecked(e);
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    /**
     * Hex编码.
     */
    public static String encodeHex(byte[] input) {
        return Hex.encodeHexString(input);
    }

    /**
     * Hex解码.
     */
    public static byte[] decodeHex(String input) {
        try {
            return Hex.decodeHex(input.toCharArray());
        } catch (DecoderException e) {
            throw ExceptionUtils.unchecked(e);
        }
    }

    /**
     * Base64编码.
     */
    public static String encodeBase64(byte[] input) {
        return Base64.encodeBase64String(input);
    }

    /**
     * Base64编码.
     */
    public static String encodeBase64(String input) {
        return Base64.encodeBase64String(org.apache.commons.codec.binary.StringUtils.getBytesUtf8(input));
    }

    /**
     * Base64解码.
     */
    public static byte[] decodeBase64_byte(String input) {
        return Base64.decodeBase64(input);
    }

    /**
     * Base64解码.
     */
    public static String decodeBase64(String input) {
        return org.apache.commons.codec.binary.StringUtils.newStringUtf8(Base64.decodeBase64(input));
    }

    /**
     * Base62编码。
     */
    public static String encodeBase62(byte[] input) {
        char[] chars = new char[input.length];
        for (int i = 0; i < input.length; i++) {
            chars[i] = BASE62[((input[i] & 0xFF) % BASE62.length)];
        }
        return new String(chars);
    }

    /**
     * Html 转码.
     */
    public static String escapeHtml(String html) {
        return StringEscapeUtils.escapeHtml4(html);
    }

    /**
     * Html 解码.
     */
    public static String unescapeHtml(String htmlEscaped) {
        return StringEscapeUtils.unescapeHtml4(htmlEscaped);
    }

    /**
     * URL 编码, Encode默认为UTF-8.
     */
    public static String urlEncode(String part) {
        return urlEncode(part, null);
    }

    /**
     * URL 编码, Encode默认为UTF-8.
     */
    public static String urlEncode(String part, String charset) {
        try {
            if (StringUtils.isEmpty(charset)) {
                charset = DEFAULT_URL_ENCODING;
            }
            return URLEncoder.encode(part, charset);
        } catch (UnsupportedEncodingException e) {
            throw ExceptionUtils.unchecked(e);
        }
    }

    /**
     * URL 编码, Encode默认为UTF-8.
     */
    public static String urlEncodeComponent(String part) {
        return urlEncodeComponent(part, null);
    }

    /**
     * URL 编码, Encode默认为UTF-8.
     */
    public static String urlEncodeComponent(String part, String charset) {
        return urlEncode(part, charset).replace("+", "%20");
    }

    /**
     * URL 解码, Encode默认为UTF-8.
     */
    public static String urlDecode(String part) {
        return urlDecode(part, DEFAULT_URL_ENCODING);
    }

    /**
     * URL 解码, Encode默认为UTF-8.
     */
    public static String urlDecode(String part, String charset) {
        try {
            if (StringUtils.isEmpty(charset)) {
                charset = DEFAULT_URL_ENCODING;
            }
            return URLDecoder.decode(part, charset);
        } catch (UnsupportedEncodingException e) {
            throw ExceptionUtils.unchecked(e);
        }
    }

}
