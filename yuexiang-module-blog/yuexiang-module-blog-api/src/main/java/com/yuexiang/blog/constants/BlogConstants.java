package com.yuexiang.blog.constants;

public final class BlogConstants {


    private BlogConstants() {}

    public static final String BLOG_SUMMARY_KEY_PREFIX = "blog:summary:";
    public static final String COMMENT_RATE_KEY_PREFIX = "comment:rate:";
    public static final Integer BLOG_STATUS_PENDING = 0;

    public static final long BLOG_SUMMARY_TTL = 7 * 24 * 60 * 60L;
    public static final int COMMENT_RATE_LIMIT = 3;
    public static final int COMMENT_RATE_WINDOW = 60;

    public static final int BIZ_TYPE_SHOP = 1;
    public static final int BIZ_TYPE_BLOG = 2;
    public static final int BIZ_TYPE_COMMENT = 3;
    public static final int BIZ_TYPE_USER = 4;

    public static final int BLOG_STATUS_DRAFT = 0;
    public static final int BLOG_STATUS_PUBLISHED = 1;
    public static final int BLOG_STATUS_BLOCKED = 2;

    public static final int COMMENT_STATUS_NORMAL = 0;
    public static final int COMMENT_STATUS_HIDDEN = 1;
}
