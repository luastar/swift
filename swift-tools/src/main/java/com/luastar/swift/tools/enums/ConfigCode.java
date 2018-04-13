package com.luastar.swift.tools.enums;

import com.luastar.swift.base.utils.ObjUtils;

/**
 * 配置编码
 */
public enum ConfigCode {

    /**
     * 数据库ID
     */
    mybatis_db_id("mybatis_db_id", "数据库ID"),
    /**
     * 输出目录
     */
    mybatis_output("mybatis_output", "输出目录"),;

    private final String configCode;
    private final String configName;

    ConfigCode(String configCode, String configName) {
        this.configCode = configCode;
        this.configName = configName;
    }

    public String getConfigCode() {
        return configCode;
    }

    public String getConfigName() {
        return configName;
    }

    public static ConfigCode parse(String configCode) {
        if (ObjUtils.isEmpty(configCode)) {
            return null;
        }
        for (ConfigCode item : values()) {
            if (item.getConfigCode().equalsIgnoreCase(configCode)) {
                return item;
            }
        }
        return null;
    }

}
