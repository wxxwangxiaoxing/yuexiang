package com.yuexiang.blog.service;

import com.yuexiang.blog.domain.vo.AiSummaryVO;

public interface AiSummaryService {

    AiSummaryVO getSummary(Long blogId);
}
