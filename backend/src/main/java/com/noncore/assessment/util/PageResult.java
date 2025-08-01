package com.noncore.assessment.util;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 分页结果工具类
 * 用于封装分页查询的返回结果
 *
 * @author System
 * @version 1.0.0
 * @since 2024-12-28
 */
@Setter
@Getter
public class PageResult<T> {

    // Getter和Setter方法
    private List<T> items;
    private Integer page;
    private Integer size;
    private Long total;
    private Integer totalPages;
    private boolean hasNext;
    private boolean hasPrevious;

    // 默认构造方法
    public PageResult() {}

    // 完整构造方法
    public PageResult(List<T> items, Integer page, Integer size, Long total, Integer totalPages) {
        this.items = items;
        this.page = page;
        this.size = size;
        this.total = total;
        this.totalPages = totalPages;
        this.hasNext = page < totalPages;
        this.hasPrevious = page > 1;
    }

    // 静态工厂方法
    public static <T> PageResult<T> of(List<T> items, Integer page, Integer size, Long total, Integer totalPages) {
        return new PageResult<>(items, page, size, total, totalPages);
    }

    // 空结果
    public static <T> PageResult<T> empty() {
        return new PageResult<>(List.of(), 1, 0, 0L, 0);
    }

    @Override
    public String toString() {
        return "PageResult{" +
                "items=" + (items != null ? items.size() : 0) + " items" +
                ", page=" + page +
                ", size=" + size +
                ", total=" + total +
                ", totalPages=" + totalPages +
                '}';
    }
} 