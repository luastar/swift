package com.luastar.swift.tools.utils;

import org.beetl.core.Configuration;
import org.beetl.core.GroupTemplate;
import org.beetl.core.Template;
import org.beetl.core.resource.ClasspathResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;

public class BeetlUtils {

    private static final Logger logger = LoggerFactory.getLogger(BeetlUtils.class);

    private GroupTemplate groupTemplate;
    private Template template;

    public BeetlUtils() {
        init();
    }

    private void init() {
        try {
            ClasspathResourceLoader resourceLoader = new ClasspathResourceLoader();
            Configuration cfg = Configuration.defaultConfiguration();
            groupTemplate = new GroupTemplate(resourceLoader, cfg);
        } catch (Exception e) {
            logger.error("转换为文件时异常", e);
        }
    }

    /**
     * 设置模版
     *
     * @param templateFile 模版文件
     * @throws Exception
     */
    public void setTemplate(String templateFile) {
        template = groupTemplate.getTemplate(templateFile);
    }

    /**
     * @param key
     * @param value
     */
    public void binding(String key, Object value) {
        if (template == null) {
            return;
        }
        template.binding(key, value);
    }

    /**
     * 转换为文本
     */
    public String toText() throws Exception {
        if (template == null) {
            logger.info("模板为空！");
            return null;
        }
        return template.render();
    }

    /**
     * @param fileName
     */
    public void toFile(String path, String fileName) {
        try {
            if (template == null) {
                logger.info("模板为空！");
                return;
            }
            File filePath = new File(path);
            if (!filePath.exists()) {
                filePath.mkdirs();
            }
            template.renderTo(new FileOutputStream(new File(filePath, fileName)));
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("转换为文件时异常", e);
        }
    }

}
