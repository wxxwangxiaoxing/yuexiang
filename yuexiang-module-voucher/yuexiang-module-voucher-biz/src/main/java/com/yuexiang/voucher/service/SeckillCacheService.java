package com.yuexiang.voucher.service;

public interface SeckillCacheService {

    void evictListCache();

    void evictListCache(Integer pageSize);

    void evictStockCache(Long voucherId);
}
