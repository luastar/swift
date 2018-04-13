package com.luastar.swift.tools.enums;

import com.google.common.collect.Lists;
import com.luastar.swift.base.utils.ObjUtils;

import java.util.List;

/**
 * 数据库类型
 */
public enum DbType {

    H2("H2", "org.h2.Driver"),
    MySQL("MySQL", "com.mysql.jdbc.Driver"),
    PostgreSQL("PostgreSQL", "org.postgresql.Driver"),;

    private final String name;
    private final String driver;

    DbType(String name, String driver) {
        this.name = name;
        this.driver = driver;
    }

    public String getName() {
        return name;
    }

    public String getDriver() {
        return driver;
    }

    public static DbType parse(String name) {
        if (ObjUtils.isEmpty(name)) {
            return null;
        }
        for (DbType item : values()) {
            if (item.getName().equalsIgnoreCase(name)) {
                return item;
            }
        }
        return null;
    }

    public static List<String> getDbList() {
        List<String> itemList = Lists.newArrayList();
        for (DbType item : values()) {
            itemList.add(item.getName());
        }
        return itemList;
    }

}
