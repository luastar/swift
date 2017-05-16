package com.luastar.swift.base.xml;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;

public class XmlUtils {

    private static Logger logger = LoggerFactory.getLogger(XmlUtils.class);


    public static Document getXmlDocumentFromString(String xmlString) {
        try {
            if (StringUtils.isNotEmpty(xmlString)) {
                return new SAXReader().read(IOUtils.toInputStream(xmlString));
            }
        } catch (DocumentException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    public static Document getXmlDocumentFromFile(File xmlFile) {
        try {
            if (xmlFile != null) {
                return new SAXReader().read(xmlFile);
            }
        } catch (DocumentException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    public static Document getXmlDocumentFromInputStream(InputStream xmlInputStream) {
        try {
            if (xmlInputStream != null) {
                return new SAXReader().read(xmlInputStream);
            }
        } catch (DocumentException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    public static String getNodeTextTrim(Node node) {
        if (node == null) {
            return null;
        }
        String text = node.getText();
        if (StringUtils.isEmpty(text)) {
            return text;
        }
        return text.trim();
    }

    public static String getElementTextTrim(Element element) {
        if (element == null) {
            return null;
        }
        return element.getTextTrim();
    }

}
