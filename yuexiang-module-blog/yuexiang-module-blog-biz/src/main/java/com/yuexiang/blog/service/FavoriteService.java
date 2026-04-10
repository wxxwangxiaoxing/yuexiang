package com.yuexiang.blog.service;

import com.yuexiang.blog.domain.vo.FavoriteVO;

public interface FavoriteService {

    FavoriteVO toggleFavorite(Long blogId, Long userId);

    boolean isFavorited(Long blogId, Long userId);
}
