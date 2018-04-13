package com.luastar.swift.tools.model.gui;

/**
 * 树节点对象
 */
public class TreeItemVO {

    /**
     * 节点ID
     */
    private String id;
    /**
     * 节点名称
     */
    private String name;
    /**
     * 节点父id
     */
    private String pid;
    /**
     * 节点实现类
     */
    private String view;

    public TreeItemVO() {
    }

    public TreeItemVO(String id) {
        this.id = id;
    }

    public TreeItemVO(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public TreeItemVO(String id, String name, String pid, String view) {
        this.id = id;
        this.name = name;
        this.pid = pid;
        this.view = view;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getView() {
        return view;
    }

    public void setView(String view) {
        this.view = view;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return id.equals(((TreeItemVO) o).id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

}
