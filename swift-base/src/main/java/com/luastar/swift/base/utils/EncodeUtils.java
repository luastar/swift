package com.luastar.swift.base.utils;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
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

    private static final String DEFAULT_CHARSET = "UTF-8";
    private static final String AES_ALGORITHM = "AES";
    private static final String AES_CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";

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
                charset = DEFAULT_CHARSET;
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
        return urlDecode(part, DEFAULT_CHARSET);
    }

    /**
     * URL 解码, Encode默认为UTF-8.
     */
    public static String urlDecode(String part, String charset) {
        try {
            if (StringUtils.isEmpty(charset)) {
                charset = DEFAULT_CHARSET;
            }
            return URLDecoder.decode(part, charset);
        } catch (UnsupportedEncodingException e) {
            throw ExceptionUtils.unchecked(e);
        }
    }

    /**
     * AES/CBC/PKCS5Padding 加密
     * AES 为对称加密算法
     *
     * @param content
     * @param password
     * @param passwordIV
     * @return
     * @throws Exception
     */
    public static String aesEncode(String content, String password, byte[] passwordIV) throws Exception {
        if (StringUtils.isEmpty(content)
                || StringUtils.isEmpty(password)
                || passwordIV == null) {
            throw new IllegalArgumentException("内容/密码/向量都不能为空！");
        }
        SecretKeySpec key = new SecretKeySpec(password.getBytes(DEFAULT_CHARSET), AES_ALGORITHM);
        IvParameterSpec iv = new IvParameterSpec(passwordIV);
        // 默认“AES”为：“AES/ECB/PKCS5Padding”，此处使用“AES/CBC/PKCS5Padding”
        Cipher cipher = Cipher.getInstance(AES_CIPHER_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key, iv);
        byte[] result = cipher.doFinal(content.getBytes(DEFAULT_CHARSET));
        return encodeBase64(result);
    }

    /**
     * AES/CBC/PKCS5Padding 解密
     *
     * @param content
     * @param password
     * @param passwordIV
     * @return
     * @throws Exception
     */
    public static String aesDecode(String content, String password, byte[] passwordIV) throws Exception {
        if (StringUtils.isEmpty(content)
                || StringUtils.isEmpty(password)
                || passwordIV == null) {
            throw new IllegalArgumentException("内容/密码/向量都不能为空！");
        }
        SecretKeySpec key = new SecretKeySpec(password.getBytes(DEFAULT_CHARSET), AES_ALGORITHM);
        IvParameterSpec zeroIv = new IvParameterSpec(passwordIV);
        Cipher cipher = Cipher.getInstance(AES_CIPHER_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key, zeroIv);
        byte[] result = cipher.doFinal(decodeBase64_byte(content));
        return new String(result, DEFAULT_CHARSET);
    }

    public static void main(String[] args) throws Exception {
        String key = "1234567890123456";
        byte[] password = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        String encode = aesEncode("测试AES加解密", key, password);
        System.out.println(encode);
        String decode = aesDecode(encode, key, password);
        System.out.println(decode);
    }

}
