package com.luastar.swift.base.config;

import com.luastar.swift.base.utils.ClassLoaderUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
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
    private static XPathExpression resourceXpathExpression;
    private static ItfConfig configInstance;

    static {
        try {
            XPathFactory xpathFactory = XPathFactory.newInstance();
            resourceXpathExpression = xpathFactory.newXPath().compile("//properties/@resource");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public synchronized static ItfConfig getConfig() {
        try {
            if (configInstance != null) {
                return configInstance;
            }
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputStream is = ClassLoaderUtils.getStream(DEFAULT_CFG_FILE);
            if (is == null) {
                logger.warn("没有默认资源配置文件：" + DEFAULT_CFG_FILE);
                return null;
            }
            Document xmlDoc = builder.parse(is);
            if (xmlDoc == null) {
                return null;
            }
            NodeList resourceNodes = (NodeList) resourceXpathExpression.evaluate(xmlDoc, XPathConstants.NODESET);
            List<String> resourceNames = new ArrayList<String>();
            for (int i = 0, size = resourceNodes.getLength(); i < size; i++) {
                resourceNames.add(StringUtils.trimToEmpty(resourceNodes.item(i).getTextContent()));
            }
            configInstance = new ConfigImpl(resourceNames.toArray(new String[]{}));
            return configInstance;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

}
