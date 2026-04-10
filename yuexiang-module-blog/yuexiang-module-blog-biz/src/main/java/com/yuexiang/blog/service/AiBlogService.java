package com.yuexiang.blog.service;

import com.yuexiang.blog.domain.dto.AiExpandDTO;
import com.yuexiang.blog.domain.dto.AiPolishDTO;
import com.yuexiang.blog.domain.dto.AiTagsDTO;
import com.yuexiang.blog.domain.dto.AiTitleDTO;
import com.yuexiang.blog.domain.vo.AiExpandVO;
import com.yuexiang.blog.domain.vo.AiPolishVO;
import com.yuexiang.blog.domain.vo.AiTagsVO;
import com.yuexiang.blog.domain.vo.AiTitleVO;

public interface AiBlogService {

    AiTitleVO generateTitle(AiTitleDTO dto, Long userId);

    AiPolishVO polishContent(AiPolishDTO dto, Long userId);

    AiExpandVO expandContent(AiExpandDTO dto, Long userId);

    AiTagsVO suggestTags(AiTagsDTO dto, Long userId);
}
