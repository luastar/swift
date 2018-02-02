package com.luastar.swift.base.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import java.io.ByteArrayOutputStream;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * <p>
 * RSA公钥/私钥/签名工具包
 * RSA加密明文最大长度117字节，解密要求密文最大长度为128字节，所以在加密和解密的过程中需要分块进行，否则会拋出异常。
 * </p>
 * <p>
 * 字符串格式的密钥在未在特殊说明情况下都为BASE64编码格式<br/>
 * 由于非对称加密速度极其缓慢，一般文件不使用它来加密而是使用对称加密，<br/>
 * 非对称加密算法可以用来对对称加密的密钥加密，这样保证密钥的安全也就保证了数据的安全
 * </p>
 */
public class RSAUtils {

    private static final Logger logger = LoggerFactory.getLogger(RSAUtils.class);

    /**
     * 加密算法RSA
     */
    public static final String RSA_ALGORITHM = "RSA";
    /**
     * RSA最大加密明文大小
     */
    private static final int MAX_ENCRYPT_BLOCK = 117;
    /**
     * RSA最大解密密文大小
     */
    private static final int MAX_DECRYPT_BLOCK = 128;

    /**
     * <p>
     * 生成密钥对(公钥和私钥)
     * </p>
     *
     * @return
     * @throws Exception
     */
    public static KeyPair genKeyPair() throws Exception {
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(RSA_ALGORITHM);
        keyPairGen.initialize(1024);
        return keyPairGen.generateKeyPair();
    }

    /**
     * <p>
     * 获取私钥
     * </p>
     *
     * @param keyPair 密钥对
     * @return
     * @throws Exception
     */
    public static String getPrivateKey(KeyPair keyPair) throws Exception {
        return EncodeUtils.encodeBase64(keyPair.getPrivate().getEncoded());
    }


    /**
     * <p>
     * 获取公钥
     * </p>
     *
     * @param keyPair 密钥对
     * @return
     * @throws Exception
     */
    public static String getPublicKey(KeyPair keyPair) throws Exception {
        return EncodeUtils.encodeBase64(keyPair.getPublic().getEncoded());
    }

    /**
     * <p>
     * 公钥加密
     * </p>
     *
     * @param data      源数据
     * @param publicKey 公钥(BASE64编码)
     * @return
     * @throws Exception
     */
    public static byte[] encryptByPublicKey(byte[] data, String publicKey) throws Exception {
        byte[] keyBytes = EncodeUtils.decodeBase64_byte(publicKey);
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
        Key publicK = keyFactory.generatePublic(x509KeySpec);
        // 对数据加密
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.ENCRYPT_MODE, publicK);
        return codeData(cipher, data, MAX_ENCRYPT_BLOCK);
    }


    /**
     * <p>
     * 私钥加密
     * </p>
     *
     * @param data       源数据
     * @param privateKey 私钥(BASE64编码)
     * @return
     * @throws Exception
     */
    public static byte[] encryptByPrivateKey(byte[] data, String privateKey) throws Exception {
        byte[] keyBytes = EncodeUtils.decodeBase64_byte(privateKey);
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
        Key privateK = keyFactory.generatePrivate(pkcs8KeySpec);
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.ENCRYPT_MODE, privateK);
        return codeData(cipher, data, MAX_ENCRYPT_BLOCK);
    }

    /**
     * 分段处理数据
     *
     * @param data
     * @param cipher
     * @return
     * @throws Exception
     */
    private static byte[] codeData(Cipher cipher, byte[] data, int maxBlock) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            int dataLength = data.length, i = 0, offSet = 0;
            byte[] cache;
            while (dataLength - offSet > 0) {
                if (dataLength - offSet > maxBlock) {
                    cache = cipher.doFinal(data, offSet, maxBlock);
                } else {
                    cache = cipher.doFinal(data, offSet, dataLength - offSet);
                }
                out.write(cache, 0, cache.length);
                i++;
                offSet = i * maxBlock;
            }
            return out.toByteArray();
        } finally {
            out.close();
        }
    }

    /**
     * <P>
     * 私钥解密
     * </p>
     *
     * @param data       已加密数据
     * @param privateKey 私钥(BASE64编码)
     * @return
     * @throws Exception
     */
    public static byte[] decryptByPrivateKey(byte[] data, String privateKey) throws Exception {
        byte[] keyBytes = EncodeUtils.decodeBase64_byte(privateKey);
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
        Key privateK = keyFactory.generatePrivate(pkcs8KeySpec);
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.DECRYPT_MODE, privateK);
        return codeData(cipher, data, MAX_DECRYPT_BLOCK);
    }

    /**
     * <p>
     * 公钥解密
     * </p>
     *
     * @param data      已加密数据
     * @param publicKey 公钥(BASE64编码)
     * @return
     * @throws Exception
     */
    public static byte[] decryptByPublicKey(byte[] data, String publicKey) throws Exception {
        byte[] keyBytes = EncodeUtils.decodeBase64_byte(publicKey);
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
        Key publicK = keyFactory.generatePublic(x509KeySpec);
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.DECRYPT_MODE, publicK);
        return codeData(cipher, data, MAX_DECRYPT_BLOCK);
    }

    /**
     * 公钥加密
     *
     * @param source
     * @param publicKey
     * @return
     * @throws Exception
     */
    public static String encryptByPublicKey(String source, String publicKey) throws Exception {
        logger.debug("公钥加密，加密前{}", source);
        byte[] encodedData = encryptByPublicKey(source.getBytes(), publicKey);
        String encodeStr = EncodeUtils.encodeBase64(encodedData);
        logger.debug("公钥加密，加密后{}", encodeStr);
        return encodeStr;
    }


    /**
     * 私钥解密
     *
     * @param source
     * @param privateKey
     * @return
     * @throws Exception
     */
    public static String decryptByPrivateKey(String source, String privateKey) throws Exception {
        logger.debug("私钥解密，解密前{}", source);
        byte[] decodedData = decryptByPrivateKey(EncodeUtils.decodeBase64_byte(source), privateKey);
        String target = new String(decodedData);
        logger.debug("私钥解密，解密后{}", target);
        return target;
    }


    /**
     * 私钥加密
     *
     * @param source
     * @param privateKey
     * @return
     * @throws Exception
     */
    public static String encryptByPrivateKey(String source, String privateKey) throws Exception {
        logger.debug("私钥加密，加密前{}", source);
        byte[] encodedData = encryptByPrivateKey(source.getBytes(), privateKey);
        String encodeStr = EncodeUtils.encodeBase64(encodedData);
        logger.debug("私钥加密，加密后{}", encodeStr);
        return encodeStr;
    }


    /**
     * 公钥解密
     *
     * @param source
     * @param publicKey
     * @return
     * @throws Exception
     */
    public static String decryptByPublicKey(String source, String publicKey) throws Exception {
        logger.debug("公钥解密，解密前{}", source);
        byte[] decodedData = decryptByPublicKey(EncodeUtils.decodeBase64_byte(source), publicKey);
        String target = new String(decodedData);
        logger.debug("公钥解密，解密后{}", target);
        return target;
    }

    public static void main(String[] args) throws Exception {
        // 生成密钥对
        KeyPair keyPair = genKeyPair();
        String publicKey = getPublicKey(keyPair);
        System.out.println("publicKey：" + publicKey);
        String privateKey = getPrivateKey(keyPair);
        System.out.println("privateKey：" + privateKey);
        // 待加密文字
        String source = "这是一行没有任何意义的文字，你看完了等于没看，不是吗？意义的文字，你看完了等于没看，不是吗？意义的文字，你看完了等于没看，不是吗？意义的文字，你看完了等于没看，不是吗？意义的文字，你看完了等于没看，不是吗？意义的文字，你看完了等于没看，不是吗？";
        // 公钥加密——私钥解密
        System.out.println("公钥加密前文字：\r\n" + source);
        String encodeStr = encryptByPublicKey(source, publicKey);
        System.out.println("公钥加密后文字：\r\n" + encodeStr);
        String target = decryptByPrivateKey(encodeStr, privateKey);
        System.out.println("私钥解密后文字: \r\n" + target);
        // 私钥加密——公钥解密
        encodeStr = encryptByPrivateKey(source, privateKey);
        System.out.println("私钥加密后文字：\r\n" + encodeStr);
        target = decryptByPublicKey(encodeStr, publicKey);
        System.out.println("公钥解密后文字: \r\n" + target);
    }

}
