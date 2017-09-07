package com.luastar.swift.base.utils;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.util.Arrays;

/**
 * JCA (Java Cryptography Architecture)
 * AES/CTR/NoPadding
 * 从play框架中扣出来的
 */
public class JcaUtils {

    private static final String version = "1";

    /**
     * Encrypts a string.
     *
     * @param value The plain text to encrypt.
     * @return The encrypted string.
     */
    public static String encrypt(String privateKey, String value) throws Exception {
        SecretKeySpec keySpec = secretKeyWithSha256(privateKey, "AES");
        Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        byte[] encryptedValue = cipher.doFinal(value.getBytes("UTF-8"));
        byte[] iv = cipher.getIV();
        if (iv == null) {
            throw new RuntimeException("Cannot get IV! There must be a bug in your underlying JCE");
        }
        String result = EncodeUtils.encodeBase64(ArrayUtils.addAll(iv, encryptedValue));
        return new StringBuffer(version).append("-").append(result).toString();
    }

    /**
     * Decrypts a string.
     *
     * @param value The value to decrypt.
     * @return The plain text string.
     */
    public static String decrypt(String privateKey, String value) throws Exception {
        if (value == null) {
            throw new IllegalArgumentException("value is null");
        }
        String[] valAry = StringUtils.split(value, "-", 2);
        if (valAry.length != 2) {
            throw new RuntimeException("Unexpected format; expected [VERSION]-[ENCRYPTED STRING]");
        }
        if (!version.equals(valAry[0])) {
            throw new RuntimeException("Unknown version: " + valAry[0]);
        }
        return decryptVersion1(privateKey, valAry[1]);
    }

    /**
     * Generates the SecretKeySpec, given the private key and the algorithm.
     */
    private static SecretKeySpec secretKeyWithSha256(String privateKey, String algorithm) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(privateKey.getBytes("UTF-8"));
        // max allowed length in bits / (8 bits to a byte)
        int maxAllowedKeyLength = Cipher.getMaxAllowedKeyLength(algorithm) / 8;
        byte[] dest = Arrays.copyOfRange(digest.digest(), 0, maxAllowedKeyLength);
        return new SecretKeySpec(dest, algorithm);
    }

    /**
     * V1 decryption algorithm (AES/CTR/NoPadding - IV present).
     */
    private static String decryptVersion1(String privateKey, String value) throws Exception {
        byte[] data = EncodeUtils.decodeBase64_byte(value);
        SecretKeySpec keySpec = secretKeyWithSha256(privateKey, "AES");
        Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
        int blockSize = cipher.getBlockSize();
        byte[] iv = Arrays.copyOfRange(data, 0, blockSize);
        byte[] payload = Arrays.copyOfRange(data, blockSize, data.length);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(iv));
        return new String(cipher.doFinal(payload), "UTF-8");
    }

    public static void main(String[] args) throws Exception {
        System.out.println(decrypt("fuckGFW", "1-Sq5ecoIaJRR+1/Y3QAxAmxhsGY6mu8fLylKCr0x0l1MoyiIbuBFGPS79JG4DBsgvmS4yaTXHcSTahSNPAGijSqdn5LzSpm9k"));
    }

}
