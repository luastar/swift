package com.luastar.swift.base.utils;

import org.apache.commons.lang3.StringUtils;

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

    public static final String RMB = "￥";

    /**
     * 对象转Number
     *
     * @param amount
     * @return
     */
    public static BigDecimal obj2BigDecimal(Object amount) {
        if (amount == null) {
            return null;
        }
        BigDecimal amountBigDecimal = null;
        try {
            if (amount instanceof BigDecimal) {
                amountBigDecimal = (BigDecimal) amount;
            } else if (amount instanceof Number) {
                amountBigDecimal = new BigDecimal(((Number) amount).doubleValue());
            } else {
                String amountString = StringUtils.trim(amount.toString());
                if (StringUtils.containsIgnoreCase(amountString, RMB)) {
                    NumberFormat format = NumberFormat.getCurrencyInstance(Locale.CHINA);
                    amountBigDecimal = new BigDecimal(format.parse(amountString).doubleValue());
                } else {
                    NumberFormat format = NumberFormat.getNumberInstance();
                    amountBigDecimal = new BigDecimal(format.parse(amountString).doubleValue());
                }
            }
        } catch (ParseException e) {
        }
        return amountBigDecimal;
    }

    /**
     * 将分转换成元
     *
     * @param amount
     * @return
     */
    public static BigDecimal fen2yuan(Object amount) {
        BigDecimal amountBigDecimal = obj2BigDecimal(amount);
        if (amountBigDecimal == null) {
            return null;
        }
        return amountBigDecimal.divide(new BigDecimal(100), 2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 将元转换成分
     *
     * @param amount
     * @return
     */
    public static BigDecimal yuan2fen(Object amount) {
        BigDecimal amountBigDecimal = obj2BigDecimal(amount);
        if (amountBigDecimal == null) {
            return null;
        }
        return amountBigDecimal.multiply(new BigDecimal(100))
                .setScale(0, BigDecimal.ROUND_HALF_UP);
    }

    public static String formatYuan(BigDecimal amount) {
        return formatYuan(amount, false, false);
    }

    public static String formatYuan(BigDecimal amount, boolean group, boolean rmb) {
        if (amount == null) {
            return null;
        }
        NumberFormat format;
        if (rmb) {
            format = NumberFormat.getCurrencyInstance(Locale.CHINA);
        } else {
            format = NumberFormat.getNumberInstance();
        }
        format.setGroupingUsed(group);
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);
        return format.format(amount);
    }

    public static void main(String[] args) {
        BigDecimal yuan = fen2yuan(2134512345000L);
        System.out.println(formatYuan(yuan, false, false));
        System.out.println(formatYuan(yuan, false, true));
        System.out.println(formatYuan(yuan, true, false));
        System.out.println(formatYuan(yuan, true, true));
        System.out.println(yuan2fen("21,345,123,450.00"));
        System.out.println(yuan2fen("￥21,345,123,450.00"));
    }

}
