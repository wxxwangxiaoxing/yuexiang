-- 签到里程碑奖励规则表
CREATE TABLE IF NOT EXISTS `tb_sign_reward_rule` (
  `id`            BIGINT        NOT NULL AUTO_INCREMENT              COMMENT '主键ID',
  `required_days` INT           NOT NULL                             COMMENT '所需连续签到天数',
  `reward_type`   TINYINT       NOT NULL                             COMMENT '奖励类型(1抽奖机会,2优惠券,3积分,4实物)',
  `reward_name`   VARCHAR(64)   NOT NULL                             COMMENT '奖励名称(如 5元代金券)',
  `reward_icon`   VARCHAR(255)  DEFAULT NULL                         COMMENT '奖励图标(相对路径)',
  `reward_value`  INT           NOT NULL DEFAULT 0                   COMMENT '奖励值(券=面值分,抽奖=次数,积分=分值)',
  `voucher_id`    BIGINT        DEFAULT NULL                         COMMENT '关联优惠券模板ID(reward_type=2时)',
  `bonus_points`  INT           NOT NULL DEFAULT 0                   COMMENT '达成时额外赠送积分',
  `description`   VARCHAR(255)  DEFAULT NULL                         COMMENT '奖励描述文案',
  `sort`          INT           NOT NULL DEFAULT 0                   COMMENT '排序(required_days升序)',
  `status`        TINYINT       NOT NULL DEFAULT 1                   COMMENT '状态(0禁用,1启用)',
  `create_time`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP   COMMENT '创建时间',
  `update_time`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sign_reward_days` (`required_days`),
  KEY `idx_sign_reward_status_sort` (`status`, `sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='签到里程碑奖励规则表';

-- 签到奖励领取记录表
CREATE TABLE IF NOT EXISTS `tb_sign_reward_record` (
  `id`           BIGINT      NOT NULL AUTO_INCREMENT                 COMMENT '主键ID',
  `user_id`      BIGINT      NOT NULL                                COMMENT '用户ID',
  `rule_id`      BIGINT      NOT NULL                                COMMENT '奖励规则ID',
  `cycle_month`  CHAR(7)     NOT NULL                                COMMENT '所属周期(2026-03)',
  `reward_type`  TINYINT     NOT NULL                                COMMENT '奖励类型(冗余规则表)',
  `reward_value` INT         NOT NULL DEFAULT 0                      COMMENT '实际发放值',
  `status`       TINYINT     NOT NULL DEFAULT 1                      COMMENT '状态(1已领取,2已过期)',
  `biz_id`       BIGINT      DEFAULT NULL                            COMMENT '关联业务ID(券订单ID/抽奖记录ID)',
  `create_time`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP      COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_reward_record` (`user_id`, `rule_id`, `cycle_month`),
  KEY `idx_reward_user_cycle`   (`user_id`, `cycle_month`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='签到奖励领取记录表';

-- 签到补签记录表
CREATE TABLE IF NOT EXISTS `tb_sign_repair_record` (
  `id`          BIGINT   NOT NULL AUTO_INCREMENT                     COMMENT '主键ID',
  `user_id`     BIGINT   NOT NULL                                    COMMENT '用户ID',
  `repair_date` DATE     NOT NULL                                    COMMENT '补签的日期',
  `cost_points` INT      NOT NULL                                    COMMENT '消耗积分',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP          COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_repair_user_date` (`user_id`, `repair_date`),
  KEY `idx_repair_user_month`      (`user_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='签到补签记录表';

-- 用户详情表新增字段
ALTER TABLE `tb_user_info`
  ADD COLUMN IF NOT EXISTS `max_continuous_sign_days` INT NOT NULL DEFAULT 0 COMMENT '历史最长连续签到天数' AFTER `like_count`;

-- 初始化里程碑奖励规则数据
INSERT INTO `tb_sign_reward_rule` (`required_days`, `reward_type`, `reward_name`, `reward_icon`, `reward_value`, `voucher_id`, `bonus_points`, `description`, `sort`, `status`) VALUES
(3, 1, '抽奖1次', '/imgs/reward/lottery.png', 1, NULL, 0, '连续签到3天，获得1次幸运抽奖机会', 1, 1),
(7, 2, '5元代金券', '/imgs/reward/voucher5.png', 500, NULL, 30, '连续签到7天，获得5元无门槛代金券 + 额外30积分', 2, 1),
(15, 2, '20元代金券', '/imgs/reward/voucher20.png', 2000, NULL, 50, '连续签到15天，获得20元代金券 + 额外50积分', 3, 1),
(30, 2, '50元代金券', '/imgs/reward/voucher50.png', 5000, NULL, 150, '连续签到30天，获得50元代金券 + 额外150积分', 4, 1);

-- 初始化系统配置
INSERT INTO `tb_sys_config` (`config_key`, `config_value`, `config_type`, `group_name`, `description`) VALUES
('sign.daily_points_rules', '[{"minDays":1,"maxDays":2,"points":5,"desc":"签到第1-2天，每天+5积分"},{"minDays":3,"maxDays":4,"points":10,"desc":"连续3-4天，每天+10积分"},{"minDays":5,"maxDays":6,"points":15,"desc":"连续5-6天，每天+15积分"},{"minDays":7,"maxDays":999,"points":20,"desc":"连续7天及以上，每天+20积分"}]', 'JSON', 'sign', '每日签到积分规则'),
('sign.repair.cost_points', '50', 'INT', 'sign', '单次补签消耗积分'),
('sign.repair.max_per_month', '3', 'INT', 'sign', '每月补签次数上限'),
('sign.repair.window_days', '7', 'INT', 'sign', '可补签时间窗口(天)');
