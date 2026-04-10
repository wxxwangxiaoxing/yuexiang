package com.yuexiang.ai.functions;

import lombok.Data;

@Data
public class BlogSearchRequest {

    private Long shopId;
    private String keyword;
    private Integer limit = 5;
}
