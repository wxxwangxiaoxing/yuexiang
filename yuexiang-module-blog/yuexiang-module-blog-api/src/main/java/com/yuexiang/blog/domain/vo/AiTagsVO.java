package com.yuexiang.blog.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@Schema(name = "AiTagsVO", description = "AI打标签响应")
public class AiTagsVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "标签列表")
    private List<TagVO> tags;

    @Schema(description = "是否为降级内容")
    private Boolean fallback;

    @Data
    @Schema(name = "TagVO", description = "标签信息")
    public static class TagVO implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "标签ID(已存在返回ID，新建议为null)")
        private Long id;

        @Schema(description = "标签名称")
        private String name;

        @Schema(description = "是否在标签库中匹配到")
        private Boolean matched;
    }
}
