package com.luastar.swift.base.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class DateUtils {

    private static final Logger logger = LoggerFactory.getLogger(DateUtils.class);

    public static final String FORMAT_DATE_WITH_BAR = "yyyy-MM-dd";
    public static final String FORMAT_DATE_NO_BAR = "yyyyMMdd";
    public static final String FORMAT_DATE_CHINESE = "yyyy年MM月dd日";
    public static final String FORMAT_TIME_WITH_BAR = "yyyy-MM-dd HH:mm:ss";
    public static final String FORMAT_TIME_WITH_MINUTE = "yyyy-MM-dd HH:mm";
    public static final String FORMAT_TIME_NO_BAR = "yyyyMMddHHmmss";
    public static final String FORMAT_MONTH = "yyyyMM";

    /** 线程安全 */
    public static final FastDateFormat NORMAL_FORMAT = FastDateFormat.getInstance(FORMAT_TIME_WITH_BAR);

    /**
     * 格式化成：yyyy-MM-dd HH:mm:ss
     *
     * @param date
     * @return
     */
    public static String format(Date date) {
        if (date == null) {
            return null;
        }
        return NORMAL_FORMAT.format(date);
    }

    /**
     * 格式化成：输入的格式
     *
     * @param date
     * @return
     */
    public static String format(Date date, String pattern) {
        if (date == null) {
            return null;
        }
        // 该方法存在时区的问题，格式化会有误差
        // return DateFormatUtils.format(date, pattern);
        return new SimpleDateFormat(pattern).format(date);
    }

    /**
     * 匹配：yyyy-MM-dd HH:mm:ss
     *
     * @param str
     * @return
     */
    public static Date parse(String str) {
        try {
            if (StringUtils.isEmpty(str)) {
                return null;
            }
            return NORMAL_FORMAT.parse(str);
        } catch (ParseException e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * 匹配日期格式
     *
     * @param str
     * @return
     */
    public static Date parse(String str, String... parsePatterns) {
        try {
            if (StringUtils.isEmpty(str)) {
                return null;
            }
            return org.apache.commons.lang3.time.DateUtils.parseDate(str, parsePatterns);
        } catch (ParseException e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * 匹配日期格式
     *
     * @param str
     * @return
     */
    public static Date parseDateStrictly(String str, String... parsePatterns) {
        try {
            if (StringUtils.isEmpty(str)) {
                return null;
            }
            return org.apache.commons.lang3.time.DateUtils.parseDateStrictly(str, parsePatterns);
        } catch (ParseException e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * 获取Unix时间戳，精确到秒
     *
     * @return
     */
    public static long getUnixTimestamp() {
        return System.currentTimeMillis() / 1000;
    }

    /**
     * 获取指定日期的目录  2015/06/28/
     *
     * @param date
     * @return
     */
    public static String getDateFilePath(Date date) {
        StringBuilder sb = new StringBuilder();
        sb.append("yyyy")
                .append(File.separator)
                .append("MM")
                .append(File.separator)
                .append("dd");
        return format(date, sb.toString());
    }

}
