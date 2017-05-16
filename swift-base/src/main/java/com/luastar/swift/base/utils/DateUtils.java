package com.luastar.swift.base.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class DateUtils {

    private static final Logger logger = LoggerFactory.getLogger(DateUtils.class);

    /**
     * yyyy-MM-dd
     */
    public static final SimpleDateFormat FORMAT_DATE_WITH_BAR = new SimpleDateFormat("yyyy-MM-dd");
    /**
     * yyyyMMdd
     */
    public static final SimpleDateFormat FORMAT_DATE_NO_BAR = new SimpleDateFormat("yyyyMMdd");
    /**
     * yyyy年MM月dd日
     */
    public static final SimpleDateFormat FORMAT_DATE_CHINESE = new SimpleDateFormat("yyyy年MM月dd日");
    /**
     * yyyy-MM-dd HH:mm:ss
     */
    public static final SimpleDateFormat FORMAT_TIME_WITH_BAR = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    /**
     * yyyy-MM-dd HH:mm
     */
    public static final SimpleDateFormat FORMAT_TIME_WITH_MINUTE = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    /**
     * yyyyMMddHHmmss
     */
    public static final SimpleDateFormat FORMAT_TIME_NO_BAR = new SimpleDateFormat("yyyyMMddHHmmss");
    /**
     * yyyyMM
     */
    public static final SimpleDateFormat FORMAT_MONTH_1 = new SimpleDateFormat("yyyyMM");

    /**
     * 格式化成：yyyy-MM-dd HH:mm:ss
     *
     * @param date
     * @return
     */
    public static String format(Date date) {
        return FORMAT_TIME_WITH_BAR.format(date);
    }

    /**
     * 匹配格式：yyyy-MM-dd HH:mm:ss
     *
     * @param str
     * @return
     */
    public static Date parse(String str) {
        try {
            return FORMAT_TIME_WITH_BAR.parse(str);
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
        return new SimpleDateFormat(sb.toString()).format(date);
    }

}
