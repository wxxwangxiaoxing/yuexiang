package com.yuexiang.blog.service;

import com.yuexiang.blog.domain.dto.BlogDraftDTO;
import com.yuexiang.blog.domain.dto.BlogPublishDTO;
import com.yuexiang.blog.domain.vo.BlogDraftVO;
import com.yuexiang.blog.domain.vo.BlogPublishVO;

public interface BlogPublishService {

    BlogPublishVO publishBlog(BlogPublishDTO dto, Long userId);

    BlogDraftVO createDraft(BlogDraftDTO dto, Long userId);

    BlogDraftVO updateDraft(Long draftId, BlogDraftDTO dto, Long userId);
}
