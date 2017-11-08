package com.luastar.swift.base.config;

import com.luastar.swift.base.utils.ClassLoaderUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * config.xml格式：
 * <?xml version="1.0" encoding="UTF-8"?>
 * <configuration>
 * <properties resource="classpath:properties/cms.properties" />
 * </configuration>
 */
public class ConfigFactory {

    private static final Logger logger = LoggerFactory.getLogger(ConfigFactory.class);

    private static final String DEFAULT_CFG_FILE = "config.xml";

    /**
     * 默认从配置文件“classpath:config.xml”中加载资源
     *
     * @return
     */
    public synchronized static ItfConfig getConfig() {
        try {
            InputStream is = ClassLoaderUtils.getInputStream(DEFAULT_CFG_FILE);
            if (is == null) {
                logger.warn("没有默认资源配置文件：{}", DEFAULT_CFG_FILE);
                return new ConfigImpl();
            }
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document xmlDoc = builder.parse(is);
            if (xmlDoc == null) {
                return new ConfigImpl();
            }
            XPathExpression xpathExpression = XPathFactory.newInstance().newXPath().compile("//properties/@resource");
            NodeList nodeList = (NodeList) xpathExpression.evaluate(xmlDoc, XPathConstants.NODESET);
            List<String> resourceList = new ArrayList<String>();
            for (int i = 0, size = nodeList.getLength(); i < size; i++) {
                resourceList.add(StringUtils.trimToEmpty(nodeList.item(i).getTextContent()));
            }
            return new ConfigImpl(resourceList.toArray(new String[0]));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return new ConfigImpl();
        }
    }

    /**
     * 从指定的配置文件中加载资源
     *
     * @param resource
     * @return
     */
    public synchronized static ItfConfig getConfig(String... resource) {
        return new ConfigImpl(resource);
    }

}
