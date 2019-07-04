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
     * 每页最大记录数
     */
    private int maxPageSize;
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
        this.maxPageSize = Integer.MAX_VALUE;
        this.total = 0;
        calculate();
    }

    public PageInfo(int pageNum, int pageSize, int maxPageSize) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.maxPageSize = maxPageSize;
        this.total = 0;
        calculate();
    }

    public PageInfo(int pageNum, int pageSize, long total) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.maxPageSize = Integer.MAX_VALUE;
        this.total = total;
        calculate();
    }

    public PageInfo(int pageNum, int pageSize, int maxPageSize, long total) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.maxPageSize = maxPageSize;
        this.total = total;
        calculate();
    }

    private void calculate() {
        if (pageNum <= 0) {
            pageNum = 1;
        }
        if (pageSize <= 0) {
            pageSize = 20;
        }
        if (pageSize > maxPageSize) {
            pageSize = maxPageSize;
        }
        if (total <= 0) {
            total = 0;
            pages = 1;
        } else {
            pages = (int) (total / pageSize + ((total % pageSize == 0) ? 0 : 1));
        }
        startRow = (pageNum - 1) * pageSize;
    }

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
        calculate();
    }

    public void nextPageNum() {
        this.pageNum = this.pageNum + 1;
        calculate();
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getMaxPageSize() {
        return maxPageSize;
    }

    public void setMaxPageSize(int maxPageSize) {
        this.maxPageSize = maxPageSize;
        calculate();
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
        calculate();
    }

    public int getPages() {
        return pages;
    }

    public int getStartRow() {
        return startRow;
    }

}
