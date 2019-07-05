package com.luastar.swift.tools.func.mybatis;

import com.luastar.swift.tools.enums.DbType;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;

import java.util.List;

/**
 * mybatis generator 自定义插件
 * 1、查询支持分页（暂只支持mysql/postgres）
 * 2、mapper 增加 @Mapper 注解
 */
public class MybatisCustomPlugin extends PluginAdapter {

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    /**
     * 为 Example 类添加 limit offset 属性 及 set get 方法
     *
     * @param topLevelClass
     * @param introspectedTable
     * @return
     */
    @Override
    public boolean modelExampleClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        // Integer 整数类型
        FullyQualifiedJavaType integer = PrimitiveTypeWrapper.getIntegerInstance();
        // 增加 limit 属性;
        Field limit = new Field();
        limit.setVisibility(JavaVisibility.PRIVATE);
        limit.setType(integer);
        limit.setName("limit");
        topLevelClass.addField(limit);

        // 增加 offset 属性;
        Field offset = new Field();
        offset.setVisibility(JavaVisibility.PRIVATE);
        offset.setType(integer);
        offset.setName("offset");
        topLevelClass.addField(offset);

        // 增加 limit get 方法
        Method getLimit = new Method();
        getLimit.setVisibility(JavaVisibility.PUBLIC);
        getLimit.setReturnType(integer);
        getLimit.setName("getLimit");
        getLimit.addBodyLine("return limit;");
        topLevelClass.addMethod(getLimit);

        // 增加 limit set 方法
        Method setLimit = new Method();
        setLimit.setVisibility(JavaVisibility.PUBLIC);
        setLimit.setName("setLimit");
        setLimit.addParameter(new Parameter(integer, "limit"));
        setLimit.addBodyLine("this.limit = limit;");
        topLevelClass.addMethod(setLimit);

        // 增加 offset get 方法
        Method getOffset = new Method();
        getOffset.setVisibility(JavaVisibility.PUBLIC);
        getOffset.setReturnType(integer);
        getOffset.setName("getOffset");
        getOffset.addBodyLine("return offset;");
        topLevelClass.addMethod(getOffset);

        // 增加 offset set 方法
        Method setOffset = new Method();
        setOffset.setVisibility(JavaVisibility.PUBLIC);
        setOffset.setName("setOffset");
        setOffset.addParameter(new Parameter(integer, "offset"));
        setOffset.addBodyLine("this.offset = offset;");
        topLevelClass.addMethod(setOffset);

        return super.modelExampleClassGenerated(topLevelClass, introspectedTable);
    }


    /**
     * 为 Mapper 增加 @Mapper 注解
     * @param interfaze
     * @param topLevelClass
     * @param introspectedTable
     * @return
     */
    @Override
    public boolean clientGenerated(Interface interfaze, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        // 增加 import
        interfaze.addImportedType(new FullyQualifiedJavaType("org.apache.ibatis.annotations.Mapper"));
        // 增加注解
        interfaze.addAnnotation("@Mapper");
        return super.clientGenerated(interfaze, topLevelClass, introspectedTable);
    }

    /**
     * 为 Mapper.xml 的 selectByExample 方法添加 limit 动态SQL
     *
     * @param element
     * @param introspectedTable
     * @return
     */
    @Override
    public boolean sqlMapSelectByExampleWithoutBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        // <if test="limit != null">
        XmlElement ifLimitNotNull = new XmlElement("if");
        ifLimitNotNull.addAttribute(new Attribute("test", "limit != null"));
        // <choose>
        XmlElement choose = new XmlElement("choose");
        // <when test="offset == null">
        XmlElement when = new XmlElement("when");
        when.addAttribute(new Attribute("test", "offset == null"));
        when.addElement(new TextElement("limit ${limit}"));
        choose.addElement(when);
        // <otherwise>
        XmlElement otherwise = new XmlElement("otherwise");
        // 不同的数据库分页语法不一样
        if (DbType.parse(properties.getProperty("dbType")) == DbType.PostgreSQL) {
            otherwise.addElement(new TextElement("limit ${limit} offset ${offset}"));
        } else {
            otherwise.addElement(new TextElement("limit ${offset}, ${limit}"));
        }
        choose.addElement(otherwise);
        ifLimitNotNull.addElement(choose);
        // 添加判断语句
        element.addElement(ifLimitNotNull);
        return super.sqlMapSelectByExampleWithoutBLOBsElementGenerated(element, introspectedTable);
    }

}
