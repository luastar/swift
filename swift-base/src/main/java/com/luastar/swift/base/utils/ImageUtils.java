package com.luastar.swift.base.utils;

import net.coobird.thumbnailator.Thumbnails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ImageUtils {

    private static final Logger logger = LoggerFactory.getLogger(ClassLoaderUtils.class);

    /**
     * 图片按比例缩略
     *
     * @param inFilePath
     * @param width
     * @param height
     * @param outFilePath
     */
    public static void compressScale(String inFilePath, Integer width, Integer height, String outFilePath) {
        try {
            Thumbnails.of(inFilePath).size(width, height).toFile(outFilePath);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * 指定坐标和大小
     *
     * @param inFilePath
     * @param x_start
     * @param y_start
     * @param width
     * @param height
     * @param outFilePath
     */
    public static void compressScale(String inFilePath, Integer x_start, Integer y_start, Integer width, Integer height, String outFilePath) {
        try {
            Thumbnails.of(inFilePath).sourceRegion(x_start, y_start, width, height).size(width, height).toFile(outFilePath);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

}
