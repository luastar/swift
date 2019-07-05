package com.luastar.swift.tools.func.mybatis;

import com.luastar.swift.base.config.ConfigFactory;
import com.luastar.swift.base.config.ItfConfig;

public class MybatisGenUtils {

    public static void mybatisGen(String config) {
        // 从配置文件中获取
        ItfConfig itfConfig = ConfigFactory.getConfig(config);
        String driverClass = itfConfig.getString("mybatis.gen.driverClass");
        String connectionURL = itfConfig.getString("mybatis.gen.connectionURL");
        String userId = itfConfig.getString("mybatis.gen.userId");
        String password = itfConfig.getString("mybatis.gen.password");
        String output = itfConfig.getString("mybatis.gen.output", "~/Downloads/output");
        String modelPackage = itfConfig.getString("mybatis.gen.modelPackage");
        String daoPackage = itfConfig.getString("mybatis.gen.daoPackage");
        String xmlPackage = itfConfig.getString("mybatis.gen.xmlPackage");
        String[] tableNameArray = itfConfig.getStringArray("mybatis.gen.tableName");
        String useActualColumnNames = itfConfig.getString("mybatis.gen.useActualColumnNames", "false");
        MybatisGen mybatisGen = new MybatisGen(driverClass,
                connectionURL,
                userId,
                password,
                output,
                modelPackage,
                daoPackage,
                xmlPackage,
                tableNameArray,
                useActualColumnNames);
        mybatisGen.gen();
    }

    public static void main(String[] args) {
        MybatisGenUtils.mybatisGen("classpath:props/mybatis-gen-h2.properties");
    }

}
