/**
 * Copyright (c) 2005-2012 springside.org.cn
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.luastar.swift.base.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * 关于异常的工具类.
 *
 * @author calvin
 */
public class ExceptionUtils {

    /**
     * 将CheckedException转换为UncheckedException.
     */
    public static RuntimeException unchecked(Exception e) {
        if (e instanceof RuntimeException) {
            return (RuntimeException) e;
        } else {
            return new RuntimeException(e);
        }
    }

    /**
     * 将ErrorStack转化为String.
     */
    public static String getStackTraceAsString(Exception e) {
        StringWriter stringWriter = new StringWriter();
        e.printStackTrace(new PrintWriter(stringWriter));
        return stringWriter.toString();
    }

    /**
     * 获取组合本异常信息与底层异常信息的异常描述, 适用于本异常为统一包装异常类，底层异常才是根本原因的情况。
     */
    public static String getErrorMessageWithNestedException(Exception e) {
        StringBuilder msg = new StringBuilder().append(e.getMessage());
        Throwable nestedException = e.getCause();
        if (nestedException != null) {
            msg.append(" nested exception is ")
                    .append(nestedException.getClass().getName())
                    .append(":")
                    .append(nestedException.getMessage());
        }
        return msg.toString();
    }

    /**
     * 判断异常是否由某些底层的异常引起.
     */
    public static boolean isCausedBy(Exception ex, Class<? extends Exception>... causeExceptionClasses) {
        Throwable cause = ex;
        while (cause != null) {
            for (Class<? extends Exception> causeClass : causeExceptionClasses) {
                if (causeClass.isInstance(cause)) {
                    return true;
                }
            }
            cause = cause.getCause();
        }
        return false;
    }

}
