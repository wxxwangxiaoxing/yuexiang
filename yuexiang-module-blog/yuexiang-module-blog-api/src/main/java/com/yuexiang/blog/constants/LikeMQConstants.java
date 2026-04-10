package com.yuexiang.blog.constants;

public final class LikeMQConstants {

    private LikeMQConstants() {}

    public static final String LIKE_EVENT_TOPIC = "LIKE_EVENT_TOPIC";

    public static final String TAG_LIKE = "LIKE";

    public static final String TAG_UNLIKE = "UNLIKE";

    public static final String CG_LIKE_USER_COUNT = "CG_LIKE_USER_COUNT";

    public static final String CG_LIKE_DEAD_LETTER = "CG_LIKE_DEAD_LETTER";

    public static final String DLQ_TOPIC = "%DLQ%" + CG_LIKE_USER_COUNT;

    public static final String PRODUCER_GROUP = "PG_LIKE_EVENT";
}
