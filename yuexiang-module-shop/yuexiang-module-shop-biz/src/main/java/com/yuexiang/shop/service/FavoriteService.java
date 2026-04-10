package com.yuexiang.shop.service;

import java.util.Set;

public interface FavoriteService {

    void toggleFavorite(Long userId, Long shopId);

    boolean isFavorite(Long userId, Long shopId);

    Set<Long> getFavoriteShopIds(Long userId, Set<Long> shopIds);
}
