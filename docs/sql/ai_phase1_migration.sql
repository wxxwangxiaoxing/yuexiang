-- AI 模块 Phase 1 表结构迁移
-- 执行前请确认当前库已存在 tb_ai_session / tb_ai_message

ALTER TABLE `tb_ai_session`
  ADD COLUMN `status` TINYINT NOT NULL DEFAULT 1 COMMENT '会话状态：1-活跃 0-关闭' AFTER `message_count`,
  ADD COLUMN `last_active_time` DATETIME DEFAULT NULL COMMENT '最近活跃时间' AFTER `update_time`,
  ADD COLUMN `total_tokens` INT NOT NULL DEFAULT 0 COMMENT '累计Token消耗' AFTER `last_active_time`,
  ADD COLUMN `summary` TEXT DEFAULT NULL COMMENT '会话摘要' AFTER `total_tokens`,
  ADD COLUMN `context_json` JSON DEFAULT NULL COMMENT '上下文快照(JSON)' AFTER `summary`,
  ADD COLUMN `longitude` DECIMAL(10,6) DEFAULT NULL COMMENT '用户经度' AFTER `context_json`,
  ADD COLUMN `latitude` DECIMAL(10,6) DEFAULT NULL COMMENT '用户纬度' AFTER `longitude`,
  ADD KEY `idx_ai_session_user_update` (`user_id`, `update_time`);

UPDATE `tb_ai_session`
SET `status` = 1,
    `last_active_time` = COALESCE(`last_active_time`, `update_time`, `create_time`),
    `total_tokens` = COALESCE(`total_tokens`, 0)
WHERE `status` IS NULL
   OR `last_active_time` IS NULL
   OR `total_tokens` IS NULL;

ALTER TABLE `tb_ai_message`
  ADD COLUMN `message_type` VARCHAR(32) NOT NULL DEFAULT 'text' COMMENT '消息类型：text/tool/card' AFTER `content`,
  ADD COLUMN `cards_data` JSON DEFAULT NULL COMMENT '卡片完整数据快照(JSON数组)' AFTER `shop_ids`,
  ADD COLUMN `tool_name` VARCHAR(64) DEFAULT NULL COMMENT '工具名称' AFTER `cards_data`,
  ADD COLUMN `tool_args` JSON DEFAULT NULL COMMENT '工具参数(JSON)' AFTER `tool_name`,
  ADD COLUMN `tool_result` LONGTEXT DEFAULT NULL COMMENT '工具执行结果' AFTER `tool_args`,
  ADD COLUMN `finish_reason` VARCHAR(32) DEFAULT NULL COMMENT '结束原因' AFTER `tool_result`,
  ADD COLUMN `token_usage` INT NOT NULL DEFAULT 0 COMMENT '本条消息Token消耗' AFTER `finish_reason`,
  ADD COLUMN `error_code` VARCHAR(64) DEFAULT NULL COMMENT '错误码' AFTER `token_usage`;

UPDATE `tb_ai_message`
SET `message_type` = COALESCE(`message_type`, 'text'),
    `token_usage` = COALESCE(`token_usage`, 0)
WHERE `message_type` IS NULL
   OR `token_usage` IS NULL;
