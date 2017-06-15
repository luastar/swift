package com.luastar.swift.tools.func.mybatis;

import com.google.common.collect.Maps;

import java.util.Map;

public class GenMybatisMain {

    public static void main(String[] args) {
        // 输出路径
        String outDir = "/Users/zhuminghua/Downloads/output/";
        // 表名
        String[] tableNames = new String[]{
                "fb_user"
        };
        // model 包名
        String modelPackageName = "com.fenbeitong.stereo.core.entity.mysql.operations.recommend";
        // dao 包名
        String daoPackageName = "com.fenbeitong.stereo.core.dao.mysql.operations.recommend";
        // MyBatisRepository 包名
        String mybatisRepPackageName = "org.mybatis.spring.MyBatisRepository";
        // 数据库类型
        String dbName = GenMybatis.DB_POSTGRESQL;
//        String dbName = GenMybatis.DB_MYSQL;
        Map<String, String> dbMap = getDbInfo(dbName);
        // 生成mybatis相关文件
        GenMybatis genMybatis = new GenMybatis(outDir,
                tableNames,
                modelPackageName,
                daoPackageName,
                mybatisRepPackageName,
                dbMap.get("dbName"),
                dbMap.get("dbDriver"),
                dbMap.get("dbUrl"),
                dbMap.get("dbUsername"),
                dbMap.get("dbPassword"));
        genMybatis.gen();
    }

    private static Map<String, String> getDbInfo(String type) {
        Map<String, String> dbMap = Maps.newLinkedHashMap();
        if (GenMybatis.DB_POSTGRESQL.equals(type)) {
            // postgresql
            dbMap.put("dbName", GenMybatis.DB_POSTGRESQL);
            dbMap.put("dbDriver", "org.postgresql.Driver");
            dbMap.put("dbUrl", "jdbc:postgresql://localhost:5432/spacex");
            dbMap.put("dbUsername", "postgres");
            dbMap.put("dbPassword", "");
        } else {
            // mysql
            dbMap.put("dbName", GenMybatis.DB_MYSQL);
            dbMap.put("dbDriver", "com.mysql.jdbc.Driver");
            dbMap.put("dbUrl", "jdbc:mysql://localhost:3306/stereo?useUnicode=true&amp;autoReconnect=true&amp;useSSL=false&amp;characterEncoding=UTF8");
            dbMap.put("dbUsername", "root");
            dbMap.put("dbPassword", "root123");
        }
        return dbMap;
    }

}
