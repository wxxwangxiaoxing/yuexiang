package com.yuexiang.blog.constants;

public final class BlogPublishConstants {

    private BlogPublishConstants() {
    }

    public static final String BLOG_RATE_KEY_PREFIX = "blog:rate:limit:";
    public static final String DRAFT_RATE_KEY_PREFIX = "draft:rate:limit:";
    public static final int BLOG_RATE_WINDOW_SECONDS = 300;
    public static final int DRAFT_RATE_WINDOW_SECONDS = 10;
}
