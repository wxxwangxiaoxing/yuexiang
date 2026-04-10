package com.yuexiang.blog.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LikeEvent implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long authorId;

    private Long blogId;

    private Long likeUserId;

    private Integer delta;

    private String type;

    private Long timestamp;
}
