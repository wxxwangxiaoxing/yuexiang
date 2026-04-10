package com.yuexiang.shop.service;

public interface ShopEsSyncService {

    void syncShopToEs(Long shopId);

    void removeShopFromEs(Long shopId);

    void syncAllShopsToEs();
}
