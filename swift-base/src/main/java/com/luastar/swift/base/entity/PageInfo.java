package com.luastar.swift.base.entity;

/**
 * 简单的分页计算
 */
public class PageInfo {

    /**
     * 当前页数
     */
    private int pageNum;
    /**
     * 每页记录数
     */
    private int pageSize;
    /**
     * 总数
     */
    private long total;
    /**
     * 总页数
     */
    private int pages;
    /**
     * 起始行
     */
    private int startRow;

    public PageInfo(int pageNum, int pageSize) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.total = total;
        // 设置默认值
        if (this.pageNum <= 0) {
            this.pageNum = 1;
        }
        if (this.pageSize <= 0) {
            this.pageSize = 20;
        }
        startRow = (pageNum - 1) * pageSize;
    }

    public int getPageNum() {
        return pageNum;
    }

    public int getPageSize() {
        return pageSize;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
        if (total <= 0) {
            pages = 1;
        } else {
            pages = (int) (total / pageSize + ((total % pageSize == 0) ? 0 : 1));
        }
        if (pageNum > pages){
            pageNum = pages;
        }
    }

    public int getPages() {
        return pages;
    }

    public int getStartRow() {
        return startRow;
    }

}
