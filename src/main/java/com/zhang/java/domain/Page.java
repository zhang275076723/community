package com.zhang.java.domain;

/**
 * @Date 2022/4/27 16:22
 * @Author zsy
 * @Description 自定义分页，redis不能使用PageHelper
 */
public class Page {
    /**
     * 当前是第几页
     */
    private int pageNum;

    /**
     * 每页大小
     */
    private int pageSize;

    /**
     * 元素总数
     */
    private int totalRows;

    /**
     * 总页数
     */
    private int totalPages;

    /**
     * 查询路径，用于分页复用
     */
    private String urlPath;

    public Page() {
    }

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        if (pageNum < 1) {
            this.pageNum = 1;
        } else if (pageNum > totalPages) {
            this.pageNum = totalPages;
        } else {
            this.pageNum = pageNum;
        }
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = Math.max(pageSize, 1);
    }

    public int getTotalRows() {
        return totalRows;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages() {
        if (this.totalRows % this.pageSize == 0) {
            this.totalPages = this.totalRows / this.pageSize;
        } else {
            this.totalPages = this.totalRows / this.pageSize + 1;
        }
    }

    public void setTotalRows(int totalRows) {
        this.totalRows = Math.max(totalRows, 0);
    }

    public String getUrlPath() {
        return urlPath;
    }

    public void setUrlPath(String urlPath) {
        this.urlPath = urlPath;
    }

    /**
     * 当前页起始元素索引，元素从0开始
     *
     * @return
     */
    public int getOffset() {
        return (pageNum - 1) * pageSize;
    }

    /**
     * 获取第一个导航页码，当前页往前两页
     *
     * @return
     */
    public int getFirstNavigatePageNum() {
        return Math.max(this.pageNum - 2, 1);
    }

    /**
     * 获取最后一个导航页码，当前页往前两页
     *
     * @return
     */
    public int getLastNavigatePageNum() {
        return Math.min(this.pageNum + 2, totalPages);
    }
}
