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

/**
 * dom4j 支持的xpath：
 * /AAA/DDD/BBB： 表示一层一层的，AAA下面 DDD下面的BBB
 * //BBB： 表示和这个名称相同，表示只要名称是BBB，都得到
 * /*: 所有元素
 * BBB[1]：　表示第一个BBB元素
 * BBB[last()]：表示最后一个BBB元素
 * //BBB[@id]： 表示只要BBB元素上面有id属性，都得到
 * //BBB[@id='aaaa'] 表示元素名称是BBB,在BBB上面有id属性，并且id的属性值是aaa
 */
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
