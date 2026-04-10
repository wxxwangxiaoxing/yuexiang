package com.yuexiang.ai.functions;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlogSummaryVO {

    private Long id;
    private String title;
    private String coverImage;
    private Long userId;
    private String userName;
    private String userAvatar;
    private Integer likeCount;
    private Integer commentCount;
    private Long shopId;
    private String shopName;
}
