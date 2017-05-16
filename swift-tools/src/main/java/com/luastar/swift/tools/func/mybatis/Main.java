package com.luastar.swift.tools.func.mybatis;

import com.google.common.collect.Maps;

import java.util.Map;

public class Main {

    public static void main(String[] args) {
        // 输出路径
        String outDir = "/Users/zhuminghua/Downloads/output/";
        // 表名
        String[] tableNames = new String[]{
                "company_new"
        };
        // 是否生成 model
        boolean bModel = true;
        // 是否生成 dao
        boolean bDao = true;
        // 是否生成 xml
        boolean bXml = true;
        // model 包名
        String modelPackageName = "com.fenbeitong.stereo.usercenter.entity.company";
        // dao 包名
        String daoPackageName = "com.fenbeitong.stereo.usercenter.dao.company";
        // MyBatisRepository 包名
        String mybatisRepPackageName = "org.mybatis.spring.MyBatisRepository";
        // 生成类名时是否删除表前缀
        boolean delPrefix = false;
        Map<String, String> dbMap = getDbInfo(2);
        String dbDriver = dbMap.get("dbDriver");
        String dbUrl = dbMap.get("dbUrl");
        String dbUsername = dbMap.get("dbUsername");
        String dbPassword = dbMap.get("dbPassword");
        // 生成mybatis相关文件
        GenMybatis genMybatis = new GenMybatis(outDir, tableNames, bModel, bDao, bXml,
                modelPackageName, daoPackageName, mybatisRepPackageName, delPrefix,
                dbDriver, dbUrl, dbUsername, dbPassword);
        genMybatis.gen();
    }

    private static Map<String, String> getDbInfo(int type) {
        Map<String, String> dbMap = Maps.newLinkedHashMap();
        if (type == 1) {
            // mysql
            dbMap.put("dbDriver", "com.mysql.jdbc.Driver");
            dbMap.put("dbUrl", "jdbc:mysql://localhost:3306/stereo?useUnicode=true&amp;autoReconnect=true&amp;characterEncoding=UTF8");
            dbMap.put("dbUsername", "root");
            dbMap.put("dbPassword", "root123");
        } else {
            dbMap.put("dbDriver", "org.postgresql.Driver");
            dbMap.put("dbUrl", "jdbc:postgresql://localhost:5432/spacex");
            dbMap.put("dbUsername", "postgres");
            dbMap.put("dbPassword", "");
        }
        return dbMap;
    }

}
