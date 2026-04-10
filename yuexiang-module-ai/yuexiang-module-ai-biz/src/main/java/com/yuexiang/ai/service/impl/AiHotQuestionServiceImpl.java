package com.yuexiang.ai.service.impl;

import com.yuexiang.ai.config.AiHotQuestionProperties;
import com.yuexiang.ai.domain.vo.AiHotQuestionVO;
import com.yuexiang.ai.service.AiHotQuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AiHotQuestionServiceImpl implements AiHotQuestionService {

    private final AiHotQuestionProperties properties;

    @Override
    public List<AiHotQuestionVO> listHotQuestions() {
        if (properties.getItems() == null || properties.getItems().isEmpty()) {
            return Collections.emptyList();
        }
        return new ArrayList<>(properties.getItems());
    }
}
