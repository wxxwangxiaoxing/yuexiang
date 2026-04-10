package com.yuexiang.common.utils;

public final class PageUtil {

    private PageUtil() {}

    /**
     * 计算分页偏移量（用 long 防止大页码溢出）
     */
    public static int offset(int pageNo, int pageSize) {
        return (int) ((long) (Math.max(1, pageNo) - 1) * pageSize);
    }
}