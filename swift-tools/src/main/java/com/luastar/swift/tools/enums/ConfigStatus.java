package com.luastar.swift.tools.enums;

/**
 * 状态
 */
public enum ConfigStatus {

    /**
     * 启用
     */
    ENABLE(1, "启用"),
    /**
     * 停用
     */
    DISABLE(2, "停用"),;

    private final int status;
    private final String name;

    ConfigStatus(int status, String name) {
        this.status = status;
        this.name = name;
    }

    public int getStatus() {
        return status;
    }

    public String getName() {
        return name;
    }

    public static ConfigStatus parse(Integer status) {
        if (status == null) {
            return null;
        }
        for (ConfigStatus item : values()) {
            if (item.getStatus() == status) {
                return item;
            }
        }
        return null;
    }

}
