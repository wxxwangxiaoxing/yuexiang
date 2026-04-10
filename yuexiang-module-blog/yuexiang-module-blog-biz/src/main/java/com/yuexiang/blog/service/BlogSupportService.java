package com.yuexiang.blog.service;

import com.yuexiang.blog.domain.vo.ShopSearchVO;
import com.yuexiang.blog.domain.vo.TagSuggestVO;

import java.util.List;

public interface BlogSupportService {

    ShopSearchVO searchShop(String keyword, Integer page, Integer size);

    List<TagSuggestVO> suggestTags(String keyword, Integer size);
}
