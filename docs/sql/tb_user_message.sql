CREATE TABLE IF NOT EXISTS `tb_user_message` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `type` tinyint NOT NULL COMMENT '消息类型：1系统通知 2点赞收藏 3新增关注 4评论和@',
  `title` varchar(128) DEFAULT NULL COMMENT '消息标题',
  `content` varchar(512) DEFAULT NULL COMMENT '消息内容',
  `biz_id` bigint DEFAULT NULL COMMENT '关联业务ID',
  `is_read` tinyint NOT NULL DEFAULT 0 COMMENT '是否已读：0未读 1已读',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_user_type_read` (`user_id`, `type`, `is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户消息表';
