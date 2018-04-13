package com.luastar.swift.tools.enums;

import com.luastar.swift.base.utils.ObjUtils;

/**
 * 配置分组编码
 */
public enum ConfigGroupCode {

    /**
     * mybatis生成配置
     */
    mybatis("mybatis", "mybatis生成配置"),;

    private final String groupCode;
    private final String groupName;

    ConfigGroupCode(String groupCode, String groupName) {
        this.groupCode = groupCode;
        this.groupName = groupName;
    }

    public String getGroupCode() {
        return groupCode;
    }

    public String getGroupName() {
        return groupName;
    }

    public static ConfigGroupCode parse(String groupCode) {
        if (ObjUtils.isEmpty(groupCode)) {
            return null;
        }
        for (ConfigGroupCode item : values()) {
            if (item.getGroupCode().equalsIgnoreCase(groupCode)) {
                return item;
            }
        }
        return null;
    }

}
