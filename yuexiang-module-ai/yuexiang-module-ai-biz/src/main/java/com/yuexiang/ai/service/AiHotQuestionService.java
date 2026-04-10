package com.yuexiang.ai.service;

import com.yuexiang.ai.domain.vo.AiHotQuestionVO;

import java.util.List;

public interface AiHotQuestionService {

    List<AiHotQuestionVO> listHotQuestions();
}
