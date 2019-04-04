package com.luastar.swift.base.utils;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

/**
 * 金额相关转换
 * <p>
 * NumberFormat 常用方法
 * 1、NumberFormat.getInstance  返回当前缺省语言环境的缺省数值格式；
 * 2、NumberFormat.getCurrencyInstance(此处填写语言环境，如：Locale.US；可为空，为当前系统语言环境) 返回语言环境的金融格式
 * 3、NumberFormat.getPercentInstance(此处填写语言环境，如：Locale.US；可为空，为当前系统语言环境) 返回语言环境的百分比格式
 * 4、NumberFormat.getNumberInstance(此处填写语言环境，如：Locale.US；可为空，为当前系统语言环境) 返回语言环境的数值格式
 * 5、setNinimumFractionDigits()  设置数值的小数部分允许的最小位数
 * 6、setMaximumFractionDigits()  设置数值的小数部分允许的最大位数
 * 7、setMaximumIntegerDigits()  设置数值的整数部分允许的最大位数
 * 8、setMinimumIntegerDigits()  设置数值的整数部分允许的最小位数
 * </p>
 * <p>
 * DecimalFormat pattern 符号含义：
 * 0    一位数字，缺失补0
 * #    一位数字，缺失不显示
 * .    小数的分隔符（小数点）
 * -    负数前缀
 * ,    分组分隔符（千位分隔符）
 * %    百分比
 * E    科学计数法
 * </p>
 */
public class MoneyUtils {

    /**
     * 将分转换成元
     *
     * @param amount
     * @return
     */
    public static String fen2yuan(Long amount) {
        if (amount == null) {
            return null;
        }
        BigDecimal result = new BigDecimal(amount)
                .divide(new BigDecimal(100), 2, BigDecimal.ROUND_HALF_UP);
        NumberFormat format = NumberFormat.getNumberInstance();
        format.setGroupingUsed(false);
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);
        return format.format(result);
    }

    /**
     * 将分转换成元，带千分位
     *
     * @param amount
     * @return
     */
    public static String fen2yuanWithGroup(Long amount) {
        if (amount == null) {
            return null;
        }
        BigDecimal result = new BigDecimal(amount)
                .divide(new BigDecimal(100), 2, BigDecimal.ROUND_HALF_UP);
        NumberFormat format = NumberFormat.getNumberInstance();
        format.setGroupingUsed(true);
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);
        return format.format(result);
    }

    /**
     * 将分转换成元，带人民币符号￥，带千分位
     *
     * @param amount
     * @return
     */
    public static String fen2yuanWithRMBAndGroup(Long amount) {
        if (amount == null) {
            return null;
        }
        BigDecimal result = new BigDecimal(amount)
                .divide(new BigDecimal(100), 2, BigDecimal.ROUND_HALF_UP);
        NumberFormat format = NumberFormat.getCurrencyInstance(Locale.CHINA);
        format.setGroupingUsed(true);
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);
        return format.format(result);
    }

    /**
     * 将元转换成分
     *
     * @param amount
     * @return
     */
    public static Long yuan2fen(String amount) {
        if (amount == null) {
            return null;
        }
        try {
            NumberFormat format;
            if (amount.contains("￥")) {
                format = NumberFormat.getCurrencyInstance(Locale.CHINA);
            } else {
                format = NumberFormat.getNumberInstance();
            }
            BigDecimal src = new BigDecimal(format.parse(amount).doubleValue());
            return src.setScale(2, BigDecimal.ROUND_HALF_UP)
                    .multiply(new BigDecimal(100))
                    .longValue();
        } catch (ParseException e) {
            return null;
        }
    }

    public static void main(String[] args) {
        System.out.println(fen2yuan(2134512345000L));
        System.out.println(fen2yuanWithGroup(2134512345000L));
        System.out.println(fen2yuanWithRMBAndGroup(2134512345000L));
        System.out.println(yuan2fen("21,345,123,450.00"));
        System.out.println(yuan2fen("￥21,345,123,450.00"));
    }

}
