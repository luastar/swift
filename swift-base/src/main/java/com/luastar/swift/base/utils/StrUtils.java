package com.luastar.swift.base.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.beetl.core.BeetlKit;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 特殊字符串处理常用类，
 * 一般处理请直接使用：org.apache.commons.lang3.StringUtils
 */
public class StrUtils {

    public static final Pattern FORMAT_PATTERN = Pattern.compile("(\\{\\d\\})", Pattern.CASE_INSENSITIVE);

    public static boolean isDigits(String str) {
        return NumberUtils.isDigits(str);
    }

    public static boolean isDigits(String strNum, int length) {
        if (isDigits(strNum) && strNum.length() == length) {
            return true;
        }
        return false;
    }

    public static boolean isNumber(String str) {
        return NumberUtils.isNumber(str);
    }

    /**
     * 格式化字符串，格式：hi,{0} hello world {1}, welcome {0}
     */
    public static String formatString(String str, Object... args) {
        if (args == null) {
            return str;
        }
        String result = str;
        Matcher matcher = FORMAT_PATTERN.matcher(str);
        while (matcher.find()) {
            String token = matcher.group();
            int idx = ObjUtils.toInteger(token.substring(1, token.length() - 1));
            if (idx >= 0 && idx < args.length) {
                result = result.replace(token, ObjUtils.toString(args[idx], ""));
            }
        }
        return result;
    }

    /**
     * 格式化字符串，格式：hi,${a} hello world ${b}, welcome ${a}
     */
    public static String formatString(String str, Map<String, Object> args) {
        if (str == null || args == null) {
            return str;
        }
        return BeetlKit.render(str, args);
    }

    public static String getFisrtCharLower(String str) {
        if (StringUtils.isEmpty(str)) {
            return str;
        }
        String firstCharacter = StringUtils.substring(str, 0, 1);
        String otherCharacter = StringUtils.substring(str, 1);
        return firstCharacter.toLowerCase() + otherCharacter;
    }

    /**
     * 获取驼峰命名，将特殊符号分割
     *
     * @param inputString
     * @param firstCharacterUppercase
     * @return
     */
    public static String getCamelCaseString(String inputString, boolean firstCharacterUppercase) {
        StringBuilder sb = new StringBuilder();
        boolean nextUpperCase = false;
        for (int i = 0; i < inputString.length(); i++) {
            char c = inputString.charAt(i);
            switch (c) {
                case '_':
                case '-':
                case '@':
                case '$':
                case '#':
                case ' ':
                case '/':
                case '&':
                    if (sb.length() > 0) {
                        nextUpperCase = true;
                    }
                    break;
                default:
                    if (nextUpperCase) {
                        sb.append(Character.toUpperCase(c));
                        nextUpperCase = false;
                    } else {
                        sb.append(Character.toLowerCase(c));
                    }
                    break;
            }
        }
        if (firstCharacterUppercase) {
            sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
        }
        return sb.toString();
    }

    /**
     * 用于截取字符串
     *
     * @param name   标题名称
     * @param length 截取的长度
     * @return 截取后的字符串
     */
    public static String subName(String name, int length) {
        if (StringUtils.isBlank(name)) {
            return name;
        }
        try {
            byte[] bytes = name.getBytes("Unicode");
            int n = 0; // 表示当前的字节数
            int i = 2; // 要截取的字节数，从第3个字节开始
            for (; i < bytes.length && n < length; i++) {
                // 奇数位置，如3、5、7等，为UCS2编码中两个字节的第二个字节
                if (i % 2 == 1) {
                    n++; // 在UCS2第二个字节时n加1
                } else {
                    // 当UCS2编码的第一个字节不等于0时，该UCS2字符为汉字，一个汉字算两个字节
                    if (bytes[i] != 0) {
                        n++;
                    }
                }
            }
            // 如果i为奇数时，处理成偶数
            if (i % 2 == 1) {
                // 该UCS2字符是汉字时，去掉这个截一半的汉字
                if (bytes[i - 1] != 0) {
                    i = i - 1;
                    // 该UCS2字符是字母或数字，则保留该字符
                } else {
                    i = i + 1;
                }
            }
            name = new String(bytes, 0, i, "Unicode");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return name;
    }

    /**
     * 获取字符串的字节长度
     *
     * @param str
     * @return
     */
    public static int getWordLength(String str) {
        if (StringUtils.isBlank(str)) {
            return 0;
        }
        int length = 0;
        for (int i = 0; i < str.length(); i++) {
            int ascii = Character.codePointAt(str, i);
            if (ascii >= 0 && ascii <= 255) {
                length++;
            } else {
                length += 2;
            }
        }
        return length;
    }

    /**
     * 去掉特殊符号
     *
     * @param str
     * @return
     */
    public static String removeSpecial(String str) {
        if (str == null) {
            return null;
        }
        str = str.replaceAll("\\.", "");
        str = str.replaceAll("-", "");
        str = str.replaceAll("（", "");
        str = str.replaceAll("）", "");
        str = str.replaceAll("\\)", "");
        str = str.replaceAll("\\(", "");
        str = str.replaceAll("·", "");
        str = str.replaceAll(" ", "");
        str = str.replaceAll("　", "");
        return str;
    }

    public static String toISO88591(String str) {
        try {
            if (str == null) {
                return str;
            }
            return new String(str.getBytes(), "ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            return str;
        }
    }

    public static String toGB2312(String str) {
        try {
            if (str == null) {
                return str;
            }
            return new String(str.getBytes(), "GB2312");
        } catch (UnsupportedEncodingException e) {
            return str;
        }
    }

    public static String toUTF8(String str) {
        try {
            if (str == null) {
                return str;
            }
            return new String(str.getBytes(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return str;
        }
    }

}
