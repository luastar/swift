package com.luastar.swift.tools.func.mybatis;

import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.internal.types.JavaTypeResolverDefaultImpl;

import java.sql.Types;

/**
 * java类型转换替换
 */
public class MybatisJavaTypeResolverImpl extends JavaTypeResolverDefaultImpl {

    public MybatisJavaTypeResolverImpl() {
        super();
        // SMALLINT不使用Short，而使用Integer
        typeMap.put(Types.SMALLINT, new JdbcTypeInformation("SMALLINT", new FullyQualifiedJavaType(Integer.class.getName())));
    }

}
