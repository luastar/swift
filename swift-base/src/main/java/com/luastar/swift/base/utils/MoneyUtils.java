package com.luastar.swift.base.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.math.BigDecimal;
import java.text.DecimalFormat;

/**
 * 金额相关转换
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
            return "0.00";
        }
        BigDecimal result = new BigDecimal(amount).divide(new BigDecimal(100), 2, BigDecimal.ROUND_HALF_UP);
        return new DecimalFormat("#.00").format(result);
    }

    /**
     * 将元转换成分
     *
     * @param amount
     * @return
     */
    public static Long yuan2fen(String amount) {
        if (StringUtils.isEmpty(amount) || !NumberUtils.isDigits(amount)) {
            return 0L;
        }
        return new BigDecimal(amount).multiply(new BigDecimal(100)).longValue();
    }

}
