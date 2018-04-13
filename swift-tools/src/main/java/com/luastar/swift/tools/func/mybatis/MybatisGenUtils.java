package com.luastar.swift.tools.func.mybatis;

import com.luastar.swift.base.config.ConfigImpl;
import com.luastar.swift.base.config.ItfConfig;

public class MybatisGenUtils {

    public static void mybatisGen(String config) {
        // 从配置文件中获取
        ItfConfig itfConfig = new ConfigImpl(new String[]{config});
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

    public static void mybatisGenSwift(String config) {
        ItfConfig itfConfig = new ConfigImpl(new String[]{config});
        String output = itfConfig.getString("mybatis.gen.output", "~/Downloads/output");
        String[] tableNameArray = itfConfig.getStringArray("mybatis.gen.tableName");
        String modelPackage = itfConfig.getString("mybatis.gen.modelPackage");
        String daoPackage = itfConfig.getString("mybatis.gen.daoPackage");
        String mybatisRepPackage = itfConfig.getString("mybatis.gen.mybatisRepPackage");
        String dbDriver = itfConfig.getString("mybatis.gen.dbDriver");
        String dbUrl = itfConfig.getString("mybatis.gen.dbUrl");
        String dbUsername = itfConfig.getString("mybatis.gen.dbUsername");
        String dbPassword = itfConfig.getString("mybatis.gen.dbPassword");
        // 生成mybatis相关文件
        MybatisGenSwift genMybatis = new MybatisGenSwift(output,
                tableNameArray,
                modelPackage,
                daoPackage,
                mybatisRepPackage,
                dbDriver,
                dbUrl,
                dbUsername,
                dbPassword);
        genMybatis.gen();
    }

    public static void main(String[] args) {
//        MybatisGenUtils.mybatisGenSwift("classpath:props/mybatis-gen-swift-mysql.properties");
        MybatisGenUtils.mybatisGen("classpath:props/mybatis-gen-h2.properties");
    }

}
