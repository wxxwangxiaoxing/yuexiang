-- ==========================================================
-- 悦享生活数据库结构 (整理完整版)
-- 数据库   yuexiang
-- 字符集   utf8mb4 / utf8mb4_unicode_ci
-- 表总数   44 张
-- 修复项   ① like_count 重复定义  ② 编号冲突(两个27)
--          ③ ALTER TABLE / CREATE TABLE 散落末尾 → 归位合并
--          ④ 签到奖励三表无分隔注释 → 归入用户域
--          ⑤ AI分析/死信表 → 归入对应域
-- ==========================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE DATABASE IF NOT EXISTS `yuexiang`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE `yuexiang`;


-- ╔══════════════════════════════════════════════════════════════╗
-- ║              第一部分：用户域（11张表）                       ║
-- ╚══════════════════════════════════════════════════════════════╝


-- ----------------------------
-- 1. 用户表
-- ----------------------------
DROP TABLE IF EXISTS `tb_user`;
CREATE TABLE `tb_user` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT                          COMMENT '主键ID',
  `phone`       VARCHAR(20)  NOT NULL                                         COMMENT '手机号(含国际区号,如+8613812345678)',
  `password`    VARCHAR(255) DEFAULT NULL                                     COMMENT '密码(BCrypt加密)',
  `nick_name`   VARCHAR(32)  NOT NULL                                         COMMENT '昵称(默认随机生成)',
  `avatar`      VARCHAR(255) DEFAULT NULL                                     COMMENT '头像(相对路径,如user2025a.jpg)',
  `status`      TINYINT      NOT NULL DEFAULT 0                               COMMENT '状态(0正常,1冻结,2封禁)',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP               COMMENT '创建时间',
  `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
                             ON UPDATE CURRENT_TIMESTAMP                      COMMENT '更新时间',
  `deleted`     TINYINT      NOT NULL DEFAULT 0                               COMMENT '逻辑删除(0未删,1已删)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户基本信息表';


-- ----------------------------
-- 2. 用户详情表
-- ----------------------------
DROP TABLE IF EXISTS `tb_user_info`;
CREATE TABLE `tb_user_info` (
  `id`                        BIGINT           NOT NULL AUTO_INCREMENT       COMMENT '主键ID',
  `user_id`                   BIGINT           NOT NULL                      COMMENT '用户ID',
  `city`                      VARCHAR(32)      DEFAULT NULL                  COMMENT '城市',
  `introduce`                 VARCHAR(256)     DEFAULT NULL                  COMMENT '个人介绍签名',
  `fans_count`                INT UNSIGNED     NOT NULL DEFAULT 0            COMMENT '粉丝数',
  `follow_count`              INT UNSIGNED     NOT NULL DEFAULT 0            COMMENT '关注数',
  `like_count`                INT              NOT NULL DEFAULT 0            COMMENT '获赞总数(异步更新)',
  `max_continuous_sign_days`  INT              NOT NULL DEFAULT 0            COMMENT '历史最长连续签到天数',
  `gender`                    TINYINT          NOT NULL DEFAULT 0            COMMENT '性别(0未知,1男,2女)',
  `birthday`                  DATE             DEFAULT NULL                  COMMENT '生日',
  `level`                     TINYINT UNSIGNED NOT NULL DEFAULT 1            COMMENT '用户等级(1~10)',
  `points`                    INT UNSIGNED     NOT NULL DEFAULT 0            COMMENT '用户积分',
  `create_time`               DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`               DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP
                                               ON UPDATE CURRENT_TIMESTAMP   COMMENT '更新时间',
  `deleted`                   TINYINT          NOT NULL DEFAULT 0            COMMENT '逻辑删除(0未删,1已删)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_info_user_id` (`user_id`),
  KEY `idx_user_info_level` (`level`),
  KEY `idx_user_info_points` (`points`),
  KEY `idx_user_info_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户详情表';


-- ----------------------------
-- 3. 用户钱包表
-- ----------------------------
DROP TABLE IF EXISTS `tb_user_wallet`;
CREATE TABLE `tb_user_wallet` (
  `id`             BIGINT   NOT NULL AUTO_INCREMENT                           COMMENT '主键ID',
  `user_id`        BIGINT   NOT NULL                                          COMMENT '用户ID',
  `balance`        BIGINT   NOT NULL DEFAULT 0                                COMMENT '可用余额(分)',
  `frozen_balance` BIGINT   NOT NULL DEFAULT 0                                COMMENT '冻结余额(分,退款审核中等)',
  `total_recharge` BIGINT   NOT NULL DEFAULT 0                                COMMENT '累计充值(分)',
  `total_consume`  BIGINT   NOT NULL DEFAULT 0                                COMMENT '累计消费(分)',
  `pay_password`   VARCHAR(255) DEFAULT NULL                                  COMMENT '支付密码(加密)',
  `create_time`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP                COMMENT '创建时间',
  `update_time`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                            ON UPDATE CURRENT_TIMESTAMP                       COMMENT '更新时间',
  `version`        INT      NOT NULL DEFAULT 1                                COMMENT '乐观锁版本号',
  `deleted`        TINYINT  NOT NULL DEFAULT 0                                COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_wallet_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户钱包表';


-- ----------------------------
-- 4. 积分流水表
-- ----------------------------
DROP TABLE IF EXISTS `tb_points_record`;
CREATE TABLE `tb_points_record` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT                          COMMENT '主键ID',
  `user_id`     BIGINT       NOT NULL                                         COMMENT '用户ID',
  `type`        TINYINT      NOT NULL                                         COMMENT '类型(1签到,2评价,3消费,4活动奖励,5兑换扣减,6过期扣减)',
  `points`      INT          NOT NULL                                         COMMENT '积分变动值(正=获得,负=扣减)',
  `balance`     INT UNSIGNED NOT NULL                                         COMMENT '变动后积分余额',
  `biz_type`    VARCHAR(32)  DEFAULT NULL                                     COMMENT '关联业务类型(ORDER,REVIEW,SIGN等)',
  `biz_id`      BIGINT       DEFAULT NULL                                     COMMENT '关联业务ID',
  `description` VARCHAR(255) DEFAULT NULL                                     COMMENT '描述(如签到奖励+10积分)',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP               COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_points_record_user_id` (`user_id`),
  KEY `idx_points_record_biz`     (`biz_type`, `biz_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='积分流水表';


-- ----------------------------
-- 5. 会员等级配置表
-- ----------------------------
DROP TABLE IF EXISTS `tb_member_level`;
CREATE TABLE `tb_member_level` (
  `id`          BIGINT        NOT NULL AUTO_INCREMENT                         COMMENT '主键ID',
  `level`       TINYINT       NOT NULL                                        COMMENT '等级(1~10)',
  `name`        VARCHAR(32)   NOT NULL                                        COMMENT '等级名称(青铜,白银,黄金...)',
  `icon`        VARCHAR(255)  DEFAULT NULL                                    COMMENT '等级图标(相对路径)',
  `min_points`  INT UNSIGNED  NOT NULL                                        COMMENT '升级所需最低积分',
  `discount`    DECIMAL(3,2)  NOT NULL DEFAULT 1.00                           COMMENT '会员折扣(0.85=8.5折)',
  `privileges`  VARCHAR(1024) DEFAULT NULL                                    COMMENT '等级权益描述(JSON)',
  `create_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP              COMMENT '创建时间',
  `update_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP
                              ON UPDATE CURRENT_TIMESTAMP                     COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_member_level` (`level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会员等级配置表';


-- ----------------------------
-- 6. 用户实名认证表
-- ----------------------------
DROP TABLE IF EXISTS `tb_user_auth`;
CREATE TABLE `tb_user_auth` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT                        COMMENT '主键ID',
  `user_id`       BIGINT       NOT NULL                                       COMMENT '用户ID',
  `real_name`     VARCHAR(32)  NOT NULL                                       COMMENT '真实姓名(加密存储)',
  `id_card`       VARCHAR(128) NOT NULL                                       COMMENT '身份证号(加密存储)',
  `front_image`   VARCHAR(255) DEFAULT NULL                                   COMMENT '身份证正面照(相对路径)',
  `back_image`    VARCHAR(255) DEFAULT NULL                                   COMMENT '身份证反面照(相对路径)',
  `status`        TINYINT      NOT NULL DEFAULT 0                             COMMENT '状态(0待审核,1已通过,2已拒绝)',
  `reject_reason` VARCHAR(255) DEFAULT NULL                                   COMMENT '拒绝原因',
  `audit_time`    DATETIME     DEFAULT NULL                                   COMMENT '审核时间',
  `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP             COMMENT '创建时间',
  `update_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
                               ON UPDATE CURRENT_TIMESTAMP                    COMMENT '更新时间',
  `deleted`       TINYINT      NOT NULL DEFAULT 0                             COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_auth_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户实名认证表';


-- ----------------------------
-- 7. 用户反馈表
-- ----------------------------
DROP TABLE IF EXISTS `tb_feedback`;
CREATE TABLE `tb_feedback` (
  `id`          BIGINT        NOT NULL AUTO_INCREMENT                         COMMENT '主键ID',
  `user_id`     BIGINT        NOT NULL                                        COMMENT '用户ID',
  `type`        TINYINT       NOT NULL DEFAULT 0                              COMMENT '类型(0建议,1BUG,2投诉,3其他)',
  `content`     VARCHAR(2048) NOT NULL                                        COMMENT '反馈内容',
  `images`      TEXT          DEFAULT NULL                                    COMMENT '截图(JSON数组)',
  `contact`     VARCHAR(64)   DEFAULT NULL                                    COMMENT '联系方式',
  `status`      TINYINT       NOT NULL DEFAULT 0                              COMMENT '状态(0待处理,1处理中,2已回复,3已关闭)',
  `reply`       VARCHAR(2048) DEFAULT NULL                                    COMMENT '官方回复',
  `reply_time`  DATETIME      DEFAULT NULL                                    COMMENT '回复时间',
  `reply_by`    BIGINT        DEFAULT NULL                                    COMMENT '回复人(运营ID)',
  `create_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP              COMMENT '创建时间',
  `update_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP
                              ON UPDATE CURRENT_TIMESTAMP                     COMMENT '更新时间',
  `deleted`     TINYINT       NOT NULL DEFAULT 0                              COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_feedback_user_id` (`user_id`),
  KEY `idx_feedback_status`  (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户反馈表';


-- ----------------------------
-- 8. 用户签到月度汇总表 (位图方案)
-- ----------------------------
DROP TABLE IF EXISTS `tb_sign`;
CREATE TABLE `tb_sign` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT                          COMMENT '主键ID',
  `user_id`     BIGINT       NOT NULL                                         COMMENT '用户ID',
  `year`        SMALLINT     NOT NULL                                         COMMENT '年份',
  `month`       TINYINT      NOT NULL                                         COMMENT '月份(1-12)',
  `sign_bitmap` INT UNSIGNED NOT NULL DEFAULT 0                               COMMENT '签到位图(bit1~bit31对应1~31号)',
  `sign_days`   TINYINT      NOT NULL DEFAULT 0                               COMMENT '当月累计签到天数',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP               COMMENT '创建时间',
  `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
                             ON UPDATE CURRENT_TIMESTAMP                      COMMENT '更新时间',
  `deleted`     TINYINT      NOT NULL DEFAULT 0                               COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sign_user_month` (`user_id`, `year`, `month`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户签到月度汇总表(位图存储)';


-- ----------------------------
-- 9. 签到里程碑奖励规则表
-- ----------------------------
DROP TABLE IF EXISTS `tb_sign_reward_rule`;
CREATE TABLE `tb_sign_reward_rule` (
  `id`            BIGINT        NOT NULL AUTO_INCREMENT                      COMMENT '主键ID',
  `required_days` INT           NOT NULL                                      COMMENT '所需连续签到天数',
  `reward_type`   TINYINT       NOT NULL                                      COMMENT '奖励类型(1抽奖机会,2优惠券,3积分,4实物)',
  `reward_name`   VARCHAR(64)   NOT NULL                                      COMMENT '奖励名称(如 5元代金券)',
  `reward_icon`   VARCHAR(255)  DEFAULT NULL                                  COMMENT '奖励图标(相对路径)',
  `reward_value`  INT           NOT NULL DEFAULT 0                            COMMENT '奖励值(券=面值分,抽奖=次数,积分=分值)',
  `voucher_id`    BIGINT        DEFAULT NULL                                  COMMENT '关联优惠券模板ID(reward_type=2时)',
  `bonus_points`  INT           NOT NULL DEFAULT 0                            COMMENT '达成时额外赠送积分',
  `description`   VARCHAR(255)  DEFAULT NULL                                  COMMENT '奖励描述文案',
  `sort`          INT           NOT NULL DEFAULT 0                            COMMENT '排序(required_days升序)',
  `status`        TINYINT       NOT NULL DEFAULT 1                            COMMENT '状态(0禁用,1启用)',
  `create_time`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP            COMMENT '创建时间',
  `update_time`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP
                                ON UPDATE CURRENT_TIMESTAMP                   COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sign_reward_days` (`required_days`),
  KEY `idx_sign_reward_status_sort` (`status`, `sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='签到里程碑奖励规则表';


-- ----------------------------
-- 10. 签到奖励领取记录表
-- ----------------------------
DROP TABLE IF EXISTS `tb_sign_reward_record`;
CREATE TABLE `tb_sign_reward_record` (
  `id`          BIGINT   NOT NULL AUTO_INCREMENT                              COMMENT '主键ID',
  `user_id`     BIGINT   NOT NULL                                             COMMENT '用户ID',
  `rule_id`     BIGINT   NOT NULL                                             COMMENT '奖励规则ID',
  `cycle_month` CHAR(7)  NOT NULL                                             COMMENT '所属周期(2026-03)',
  `reward_type` TINYINT  NOT NULL                                             COMMENT '奖励类型(冗余规则表)',
  `reward_value` INT     NOT NULL DEFAULT 0                                   COMMENT '实际发放值',
  `status`      TINYINT  NOT NULL DEFAULT 1                                   COMMENT '状态(1已领取,2已过期)',
  `biz_id`      BIGINT   DEFAULT NULL                                         COMMENT '关联业务ID(券订单ID/抽奖记录ID)',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP                   COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_reward_record`    (`user_id`, `rule_id`, `cycle_month`),
  KEY `idx_reward_user_cycle`      (`user_id`, `cycle_month`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='签到奖励领取记录表';


-- ----------------------------
-- 11. 签到补签记录表
-- ----------------------------
DROP TABLE IF EXISTS `tb_sign_repair_record`;
CREATE TABLE `tb_sign_repair_record` (
  `id`          BIGINT   NOT NULL AUTO_INCREMENT                              COMMENT '主键ID',
  `user_id`     BIGINT   NOT NULL                                             COMMENT '用户ID',
  `repair_date` DATE     NOT NULL                                             COMMENT '补签的日期',
  `cost_points` INT      NOT NULL                                             COMMENT '消耗积分',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP                   COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_repair_user_date` (`user_id`, `repair_date`),
  KEY `idx_repair_user_month`   (`user_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='签到补签记录表';


-- ╔══════════════════════════════════════════════════════════════╗
-- ║              第二部分：商户域（5张表）                       ║
-- ╚══════════════════════════════════════════════════════════════╝


-- ----------------------------
-- 12. 商户类型表
-- ----------------------------
DROP TABLE IF EXISTS `tb_shop_type`;
CREATE TABLE `tb_shop_type` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT                          COMMENT '主键ID',
  `name`        VARCHAR(32)  NOT NULL                                         COMMENT '类型名称(美食,KTV,休闲等)',
  `icon`        VARCHAR(255) DEFAULT NULL                                     COMMENT '类型图标(相对路径)',
  `sort`        INT          NOT NULL DEFAULT 0                               COMMENT '排序字段',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP               COMMENT '创建时间',
  `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
                             ON UPDATE CURRENT_TIMESTAMP                      COMMENT '更新时间',
  `deleted`     TINYINT      NOT NULL DEFAULT 0                               COMMENT '逻辑删除(0未删,1已删)',
  PRIMARY KEY (`id`),
  KEY `idx_shop_type_sort` (`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商户类型表';


-- ----------------------------
-- 13. 商户信息表
-- ----------------------------
DROP TABLE IF EXISTS `tb_shop`;
CREATE TABLE `tb_shop` (
  `id`            BIGINT         NOT NULL AUTO_INCREMENT                      COMMENT '主键ID',
  `name`          VARCHAR(128)   NOT NULL                                     COMMENT '商户名称',
  `type_id`       BIGINT         NOT NULL                                     COMMENT '商户类型ID',
  `images`        TEXT           DEFAULT NULL                                 COMMENT '商户图片(JSON数组)',
  `area`          VARCHAR(128)   DEFAULT NULL                                 COMMENT '商圈区域',
  `address`       VARCHAR(255)   DEFAULT NULL                                 COMMENT '详细地址',
  `longitude`     DECIMAL(10,7)  DEFAULT NULL                                 COMMENT '经度(-180~180)',
  `latitude`      DECIMAL(10,7)  DEFAULT NULL                                 COMMENT '纬度(-90~90)',
  `avg_price`     INT UNSIGNED   DEFAULT NULL                                 COMMENT '人均价格(分)',
  `sales_count`   INT UNSIGNED   NOT NULL DEFAULT 0                           COMMENT '销量',
  `comment_count` INT UNSIGNED   NOT NULL DEFAULT 0                           COMMENT '评论数',
  `score`         DECIMAL(3,1)   NOT NULL DEFAULT 0.0                         COMMENT '综合评分(0.0~5.0,贝叶斯加权)',
  `review_count`  INT UNSIGNED   NOT NULL DEFAULT 0                           COMMENT '评价总人数',
  `open_hours`    VARCHAR(255)   DEFAULT NULL                                 COMMENT '营业时间(JSON格式)',
  `phone`         VARCHAR(20)    DEFAULT NULL                                 COMMENT '联系电话',
  `create_time`   DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP           COMMENT '创建时间',
  `update_time`   DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP
                                 ON UPDATE CURRENT_TIMESTAMP                  COMMENT '更新时间',
  `created_by`    BIGINT         DEFAULT NULL                                 COMMENT '创建人ID',
  `updated_by`    BIGINT         DEFAULT NULL                                 COMMENT '最后修改人ID',
  `deleted`       TINYINT        NOT NULL DEFAULT 0                           COMMENT '逻辑删除(0未删,1已删)',
  `version`       INT            NOT NULL DEFAULT 1                           COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`),
  KEY `idx_shop_type_id` (`type_id`),
  KEY `idx_shop_score`   (`score`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商户信息表';


-- ----------------------------
-- 14. 商户评价明细表
-- ----------------------------
DROP TABLE IF EXISTS `tb_review`;
CREATE TABLE `tb_review` (
  `id`            BIGINT        NOT NULL AUTO_INCREMENT                       COMMENT '主键ID',
  `user_id`       BIGINT        NOT NULL                                      COMMENT '评价用户ID',
  `shop_id`       BIGINT        NOT NULL                                      COMMENT '关联商户ID',
  `order_id`      BIGINT        DEFAULT NULL                                  COMMENT '关联订单ID(核销后才能评价)',
  `score`         TINYINT       NOT NULL                                      COMMENT '综合打分(1~5)',
  `score_taste`   TINYINT       DEFAULT NULL                                  COMMENT '口味评分(1~5,餐饮类适用)',
  `score_env`     TINYINT       DEFAULT NULL                                  COMMENT '环境评分(1~5)',
  `score_service` TINYINT       DEFAULT NULL                                  COMMENT '服务评分(1~5)',
  `content`       VARCHAR(1024) DEFAULT NULL                                  COMMENT '文字评价',
  `images`        TEXT          DEFAULT NULL                                  COMMENT '评价图片(JSON数组)',
  `like_count`    INT UNSIGNED  NOT NULL DEFAULT 0                            COMMENT '点赞数',
  `status`        TINYINT       NOT NULL DEFAULT 0                            COMMENT '状态(0待审核,1已发布,2已屏蔽)',
  `create_time`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP            COMMENT '创建时间',
  `update_time`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP
                                ON UPDATE CURRENT_TIMESTAMP                   COMMENT '更新时间',
  `deleted`       TINYINT       NOT NULL DEFAULT 0                            COMMENT '逻辑删除(0未删,1已删)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_review_order_user`     (`order_id`, `user_id`),
  KEY `idx_review_shop_id`              (`shop_id`),
  KEY `idx_review_user_id`              (`user_id`),
  KEY `idx_review_shop_status_time`     (`shop_id`, `status`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商户评价明细表';


-- ----------------------------
-- 15. 商户入驻申请表
-- ----------------------------
DROP TABLE IF EXISTS `tb_shop_apply`;
CREATE TABLE `tb_shop_apply` (
  `id`             BIGINT        NOT NULL AUTO_INCREMENT                      COMMENT '主键ID',
  `user_id`        BIGINT        NOT NULL                                     COMMENT '申请人ID',
  `shop_name`      VARCHAR(128)  NOT NULL                                     COMMENT '商户名称',
  `type_id`        BIGINT        NOT NULL                                     COMMENT '商户类型ID',
  `address`        VARCHAR(255)  NOT NULL                                     COMMENT '详细地址',
  `longitude`      DECIMAL(10,7) DEFAULT NULL                                 COMMENT '经度',
  `latitude`       DECIMAL(10,7) DEFAULT NULL                                 COMMENT '纬度',
  `contact_name`   VARCHAR(32)   NOT NULL                                     COMMENT '联系人姓名',
  `contact_phone`  VARCHAR(20)   NOT NULL                                     COMMENT '联系电话',
  `license_image`  VARCHAR(255)  NOT NULL                                     COMMENT '营业执照(相对路径)',
  `shop_images`    TEXT          DEFAULT NULL                                  COMMENT '门店照片(JSON数组)',
  `status`         TINYINT       NOT NULL DEFAULT 0                           COMMENT '状态(0待审核,1已通过,2已拒绝)',
  `reject_reason`  VARCHAR(512)  DEFAULT NULL                                 COMMENT '拒绝原因',
  `audit_by`       BIGINT        DEFAULT NULL                                 COMMENT '审核人ID',
  `audit_time`     DATETIME      DEFAULT NULL                                 COMMENT '审核时间',
  `result_shop_id` BIGINT        DEFAULT NULL                                 COMMENT '审核通过后创建的商户ID',
  `create_time`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP           COMMENT '创建时间',
  `update_time`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP
                                 ON UPDATE CURRENT_TIMESTAMP                  COMMENT '更新时间',
  `deleted`        TINYINT       NOT NULL DEFAULT 0                           COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_shop_apply_user_id` (`user_id`),
  KEY `idx_shop_apply_status`  (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商户入驻申请表';


-- ----------------------------
-- 16. 商户AI口碑分析表
-- ----------------------------
DROP TABLE IF EXISTS `tb_shop_ai_analysis`;
CREATE TABLE `tb_shop_ai_analysis` (
  `id`                BIGINT       NOT NULL AUTO_INCREMENT                    COMMENT '主键ID',
  `shop_id`           BIGINT       NOT NULL                                   COMMENT '商户ID',
  `overall_score`     DECIMAL(2,1) NOT NULL                                   COMMENT 'AI综合评分(0.0~5.0)',
  `score_level`       VARCHAR(16)  NOT NULL                                   COMMENT '评分等级(excellent/good/medium/poor)',
  `dimensions`        JSON         DEFAULT NULL                               COMMENT '多维评分JSON',
  `positive_keywords` JSON         DEFAULT NULL                               COMMENT '好评关键词JSON',
  `negative_keywords` JSON         DEFAULT NULL                               COMMENT '差评关键词JSON',
  `summary`           VARCHAR(1024) DEFAULT NULL                              COMMENT 'AI摘要',
  `based_on_count`    INT          NOT NULL DEFAULT 0                         COMMENT '基于评价数量',
  `trending`          VARCHAR(8)   DEFAULT 'stable'                           COMMENT '趋势(up/stable/down)',
  `create_time`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP         COMMENT '创建时间',
  `update_time`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
                                   ON UPDATE CURRENT_TIMESTAMP                COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_shop_analysis` (`shop_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商户AI口碑分析表';


-- ╔══════════════════════════════════════════════════════════════╗
-- ║              第三部分：内容域（13张表）                       ║
-- ╚══════════════════════════════════════════════════════════════╝


-- ----------------------------
-- 17. 探店笔记表
-- ----------------------------
DROP TABLE IF EXISTS `tb_blog`;
CREATE TABLE `tb_blog` (
  `id`             BIGINT       NOT NULL AUTO_INCREMENT                       COMMENT '主键ID',
  `shop_id`        BIGINT       DEFAULT NULL                                    COMMENT '关联商户ID',
  `user_id`        BIGINT       NOT NULL                                      COMMENT '发布用户ID',
  `title`          VARCHAR(255) NOT NULL                                      COMMENT '笔记标题',
  `images`         TEXT         DEFAULT NULL                                  COMMENT '笔记图片(JSON数组)',
  `content`        TEXT         DEFAULT NULL                                  COMMENT '笔记正文',
  `location`       VARCHAR(128) DEFAULT NULL                                  COMMENT '发布地点',
  `like_count`     INT UNSIGNED NOT NULL DEFAULT 0                            COMMENT '点赞数',
  `comment_count`  INT UNSIGNED NOT NULL DEFAULT 0                            COMMENT '评论数',
  `favorite_count` INT UNSIGNED NOT NULL DEFAULT 0                            COMMENT '收藏数',
  `status`         TINYINT      NOT NULL DEFAULT 0                            COMMENT '状态(0待审核,1已发布,2已屏蔽,3草稿)',
  `create_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP            COMMENT '创建时间',
  `update_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
                                ON UPDATE CURRENT_TIMESTAMP                   COMMENT '更新时间',
  `created_by`     BIGINT       DEFAULT NULL                                  COMMENT '创建人ID',
  `updated_by`     BIGINT       DEFAULT NULL                                  COMMENT '最后修改人ID',
  `deleted`        TINYINT      NOT NULL DEFAULT 0                            COMMENT '逻辑删除(0未删,1已删)',
  PRIMARY KEY (`id`),
  KEY `idx_blog_shop_id`    (`shop_id`),
  KEY `idx_blog_user_id`    (`user_id`),
  KEY `idx_blog_like_count` (`like_count`),
  KEY `idx_blog_status`     (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='探店笔记表';


-- ----------------------------
-- 18. 探店笔记评论表
-- ----------------------------
DROP TABLE IF EXISTS `tb_blog_comments`;
CREATE TABLE `tb_blog_comments` (
  `id`               BIGINT        NOT NULL AUTO_INCREMENT                    COMMENT '主键ID',
  `user_id`          BIGINT        NOT NULL                                   COMMENT '评论用户ID',
  `blog_id`          BIGINT        NOT NULL                                   COMMENT '关联笔记ID',
  `root_comment_id`  BIGINT        NOT NULL DEFAULT 0                         COMMENT '顶级评论ID(0=自身为顶级)',
  `reply_comment_id` BIGINT        NOT NULL DEFAULT 0                         COMMENT '回复目标评论ID(0=非回复)',
  `content`          VARCHAR(2048) NOT NULL                                   COMMENT '评论内容',
  `like_count`       INT UNSIGNED  NOT NULL DEFAULT 0                         COMMENT '点赞数',
  `status`           TINYINT       NOT NULL DEFAULT 0                         COMMENT '状态(0正常,1隐藏)',
  `create_time`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP         COMMENT '创建时间',
  `update_time`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP
                                   ON UPDATE CURRENT_TIMESTAMP                COMMENT '更新时间',
  `deleted`          TINYINT       NOT NULL DEFAULT 0                         COMMENT '逻辑删除(0未删,1已删)',
  PRIMARY KEY (`id`),
  KEY `idx_blog_comments_blog_id`  (`blog_id`),
  KEY `idx_blog_comments_user_id`  (`user_id`),
  KEY `idx_blog_comments_root`     (`root_comment_id`),
  KEY `idx_comments_top_hot`       (`blog_id`, `root_comment_id`, `like_count`),
  KEY `idx_comments_top_new`       (`blog_id`, `root_comment_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='探店笔记评论表';


-- ----------------------------
-- 19. 笔记点赞记录表
-- ----------------------------
DROP TABLE IF EXISTS `tb_blog_likes`;
CREATE TABLE `tb_blog_likes` (
  `id`          BIGINT   NOT NULL AUTO_INCREMENT                              COMMENT '主键ID',
  `blog_id`     BIGINT   NOT NULL                                             COMMENT '关联笔记ID',
  `user_id`     BIGINT   NOT NULL                                             COMMENT '点赞用户ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP                   COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_blog_likes_blog_user` (`blog_id`, `user_id`),
  KEY `idx_blog_likes_blog_time`     (`blog_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='笔记点赞记录表';


-- ----------------------------
-- 20. 评论点赞记录表
-- ----------------------------
DROP TABLE IF EXISTS `tb_comment_likes`;
CREATE TABLE `tb_comment_likes` (
  `id`          BIGINT   NOT NULL AUTO_INCREMENT                              COMMENT '主键ID',
  `comment_id`  BIGINT   NOT NULL                                             COMMENT '评论ID',
  `user_id`     BIGINT   NOT NULL                                             COMMENT '点赞用户ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP                   COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_comment_likes` (`comment_id`, `user_id`),
  KEY `idx_comment_likes_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论点赞记录表';


-- ----------------------------
-- 21. 点赞死信表
-- ----------------------------
DROP TABLE IF EXISTS `tb_like_dead_letter`;
CREATE TABLE `tb_like_dead_letter` (
  `id`           BIGINT       NOT NULL AUTO_INCREMENT                         COMMENT '主键ID',
  `message_id`   VARCHAR(128) NOT NULL                                        COMMENT 'MQ消息ID',
  `author_id`    BIGINT       NOT NULL                                        COMMENT '笔记作者ID',
  `blog_id`      BIGINT       NOT NULL                                        COMMENT '笔记ID',
  `like_user_id` BIGINT       NOT NULL                                        COMMENT '点赞用户ID',
  `delta`        INT          NOT NULL                                        COMMENT '增量(+1/-1)',
  `error_msg`    VARCHAR(500) DEFAULT NULL                                    COMMENT '错误信息',
  `retry_count`  INT          NOT NULL DEFAULT 0                              COMMENT '已重试次数',
  `status`       TINYINT      NOT NULL DEFAULT 0                              COMMENT '状态(0待处理,1已处理,2已忽略)',
  `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP              COMMENT '创建时间',
  `update_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
                              ON UPDATE CURRENT_TIMESTAMP                     COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_dead_letter_status`   (`status`),
  KEY `idx_dead_letter_author`   (`author_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='点赞死信表';


-- ----------------------------
-- 22. 关注关系表
-- ----------------------------
DROP TABLE IF EXISTS `tb_follow`;
CREATE TABLE `tb_follow` (
  `id`             BIGINT   NOT NULL AUTO_INCREMENT                           COMMENT '主键ID',
  `user_id`        BIGINT   NOT NULL                                          COMMENT '用户ID',
  `follow_user_id` BIGINT   NOT NULL                                          COMMENT '关注的用户ID',
  `create_time`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP                COMMENT '创建时间',
  `update_time`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                            ON UPDATE CURRENT_TIMESTAMP                       COMMENT '更新时间',
  `deleted`        TINYINT  NOT NULL DEFAULT 0                                COMMENT '逻辑删除(0未删,1已删)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_follow_user` (`user_id`, `follow_user_id`),
  KEY `idx_follow_target`   (`follow_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户关注关系表';


-- ----------------------------
-- 23. 收藏记录表 (通用: 收藏商户/笔记)
-- ----------------------------
DROP TABLE IF EXISTS `tb_favorite`;
CREATE TABLE `tb_favorite` (
  `id`          BIGINT   NOT NULL AUTO_INCREMENT                              COMMENT '主键ID',
  `user_id`     BIGINT   NOT NULL                                             COMMENT '用户ID',
  `biz_type`    TINYINT  NOT NULL                                             COMMENT '收藏类型(1商户,2笔记)',
  `biz_id`      BIGINT   NOT NULL                                             COMMENT '收藏目标ID(shop_id/blog_id)',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP                   COMMENT '创建时间',
  `deleted`     TINYINT  NOT NULL DEFAULT 0                                   COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_favorite_user_biz` (`user_id`, `biz_type`, `biz_id`),
  KEY `idx_favorite_biz`            (`biz_type`, `biz_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收藏记录表';


-- ----------------------------
-- 24. 标签表 (通用标签池)
-- ----------------------------
DROP TABLE IF EXISTS `tb_tag`;
CREATE TABLE `tb_tag` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT                          COMMENT '主键ID',
  `name`        VARCHAR(32)  NOT NULL                                         COMMENT '标签名称(如网红打卡,人均50以下)',
  `type`        TINYINT      NOT NULL DEFAULT 0                               COMMENT '类型(0商户标签,1笔记话题)',
  `icon`        VARCHAR(255) DEFAULT NULL                                     COMMENT '标签图标',
  `sort`        INT          NOT NULL DEFAULT 0                               COMMENT '排序',
  `hot`         INT UNSIGNED NOT NULL DEFAULT 0                               COMMENT '热度(引用次数)',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP               COMMENT '创建时间',
  `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
                             ON UPDATE CURRENT_TIMESTAMP                      COMMENT '更新时间',
  `deleted`     TINYINT      NOT NULL DEFAULT 0                               COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tag_name_type` (`name`, `type`),
  KEY `idx_tag_type_sort`    (`type`, `sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标签表';


-- ----------------------------
-- 25. 标签关联表 (商户/笔记 与 标签 多对多)
-- ----------------------------
DROP TABLE IF EXISTS `tb_tag_relation`;
CREATE TABLE `tb_tag_relation` (
  `id`          BIGINT   NOT NULL AUTO_INCREMENT                              COMMENT '主键ID',
  `tag_id`      BIGINT   NOT NULL                                             COMMENT '标签ID',
  `biz_type`    TINYINT  NOT NULL                                             COMMENT '业务类型(1商户,2笔记)',
  `biz_id`      BIGINT   NOT NULL                                             COMMENT '业务ID(shop_id/blog_id)',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP                   COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tag_rel_biz`  (`tag_id`, `biz_type`, `biz_id`),
  KEY `idx_tag_rel_biz`        (`biz_type`, `biz_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标签关联表';


-- ----------------------------
-- 26. 浏览历史表
-- ----------------------------
DROP TABLE IF EXISTS `tb_browse_history`;
CREATE TABLE `tb_browse_history` (
  `id`         BIGINT       NOT NULL AUTO_INCREMENT                           COMMENT '主键ID',
  `user_id`    BIGINT       NOT NULL                                          COMMENT '用户ID',
  `biz_type`   TINYINT      NOT NULL                                          COMMENT '类型(1商户,2笔记)',
  `biz_id`     BIGINT       NOT NULL                                          COMMENT '目标ID',
  `view_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP                COMMENT '最近浏览时间',
  `view_count` INT UNSIGNED NOT NULL DEFAULT 1                                COMMENT '累计浏览次数',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_browse_user_biz` (`user_id`, `biz_type`, `biz_id`),
  KEY `idx_browse_user_time`   (`user_id`, `view_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='浏览历史表';


-- ----------------------------
-- 27. 搜索历史表
-- ----------------------------
DROP TABLE IF EXISTS `tb_search_history`;
CREATE TABLE `tb_search_history` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT                          COMMENT '主键ID',
  `user_id`     BIGINT       NOT NULL                                         COMMENT '用户ID',
  `keyword`     VARCHAR(128) NOT NULL                                         COMMENT '搜索关键词',
  `search_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP               COMMENT '搜索时间',
  PRIMARY KEY (`id`),
  KEY `idx_search_user_time` (`user_id`, `search_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='搜索历史表';


-- ----------------------------
-- 28. 热搜词统计表
-- ----------------------------
DROP TABLE IF EXISTS `tb_hot_search`;
CREATE TABLE `tb_hot_search` (
  `id`           BIGINT       NOT NULL AUTO_INCREMENT                         COMMENT '主键ID',
  `keyword`      VARCHAR(128) NOT NULL                                        COMMENT '关键词',
  `search_count` INT UNSIGNED NOT NULL DEFAULT 0                              COMMENT '搜索次数',
  `is_manual`    TINYINT      NOT NULL DEFAULT 0                              COMMENT '是否人工置顶(0否,1是)',
  `sort`         INT          NOT NULL DEFAULT 0                              COMMENT '排序权重',
  `status`       TINYINT      NOT NULL DEFAULT 1                              COMMENT '状态(0隐藏,1展示)',
  `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP              COMMENT '创建时间',
  `update_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
                              ON UPDATE CURRENT_TIMESTAMP                     COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_hot_keyword` (`keyword`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='热搜词统计表';


-- ----------------------------
-- 29. 举报记录表 (通用: 笔记/评论/商户/用户)
-- ----------------------------
DROP TABLE IF EXISTS `tb_report`;
CREATE TABLE `tb_report` (
  `id`            BIGINT        NOT NULL AUTO_INCREMENT                       COMMENT '主键ID',
  `user_id`       BIGINT        NOT NULL                                      COMMENT '举报人ID',
  `target_type`   TINYINT       NOT NULL                                      COMMENT '目标类型(1商户,2笔记,3评论,4用户)',
  `target_id`     BIGINT        NOT NULL                                      COMMENT '目标ID',
  `reason_type`   TINYINT       NOT NULL                                      COMMENT '原因类型(1虚假信息,2色情低俗,3广告骚扰,4侵权,5其他)',
  `reason_detail` VARCHAR(1024) DEFAULT NULL                                  COMMENT '补充说明',
  `images`        TEXT          DEFAULT NULL                                  COMMENT '举证图片(JSON数组)',
  `status`        TINYINT       NOT NULL DEFAULT 0                            COMMENT '状态(0待审核,1已处理,2已驳回)',
  `handle_result` VARCHAR(512)  DEFAULT NULL                                  COMMENT '处理结果',
  `handle_by`     BIGINT        DEFAULT NULL                                  COMMENT '处理人(运营ID)',
  `handle_time`   DATETIME      DEFAULT NULL                                  COMMENT '处理时间',
  `create_time`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP            COMMENT '创建时间',
  `update_time`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP
                                ON UPDATE CURRENT_TIMESTAMP                   COMMENT '更新时间',
  `deleted`       TINYINT       NOT NULL DEFAULT 0                            COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_report_target`  (`target_type`, `target_id`),
  KEY `idx_report_status`  (`status`),
  KEY `idx_report_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='举报记录表';


-- ╔══════════════════════════════════════════════════════════════╗
-- ║              第四部分：交易域（6张表）                       ║
-- ╚══════════════════════════════════════════════════════════════╝


-- ----------------------------
-- 30. 优惠券表
-- ----------------------------
DROP TABLE IF EXISTS `tb_voucher`;
CREATE TABLE `tb_voucher` (
  `id`               BIGINT        NOT NULL AUTO_INCREMENT                    COMMENT '主键ID',
  `shop_id`          BIGINT        NOT NULL                                   COMMENT '所属商户ID',
  `title`            VARCHAR(255)  NOT NULL                                   COMMENT '优惠券标题',
  `sub_title`        VARCHAR(255)  DEFAULT NULL                               COMMENT '副标题',
  `rules`            VARCHAR(1024) DEFAULT NULL                               COMMENT '使用规则',
  `pay_value`        INT UNSIGNED  NOT NULL                                   COMMENT '支付金额(分)',
  `actual_value`     INT UNSIGNED  NOT NULL                                   COMMENT '抵扣金额(分)',
  `type`             TINYINT       NOT NULL DEFAULT 0                         COMMENT '券类型(0普通券,1秒杀券)',
  `status`           TINYINT       NOT NULL DEFAULT 0                         COMMENT '状态(0上架,1下架,2过期)',
  `valid_begin_time` DATETIME      DEFAULT NULL                               COMMENT '用券开始时间',
  `valid_end_time`   DATETIME      DEFAULT NULL                               COMMENT '用券截止时间',
  `create_time`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP         COMMENT '创建时间',
  `update_time`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP
                                   ON UPDATE CURRENT_TIMESTAMP                COMMENT '更新时间',
  `created_by`       BIGINT        DEFAULT NULL                               COMMENT '创建人ID',
  `updated_by`       BIGINT        DEFAULT NULL                               COMMENT '最后修改人ID',
  `deleted`          TINYINT       NOT NULL DEFAULT 0                         COMMENT '逻辑删除(0未删,1已删)',
  PRIMARY KEY (`id`),
  KEY `idx_voucher_shop_id` (`shop_id`),
  KEY `idx_voucher_type`    (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='优惠券表';


-- ----------------------------
-- 31. 秒杀场次表
-- ----------------------------
DROP TABLE IF EXISTS `tb_seckill_session`;
CREATE TABLE `tb_seckill_session` (
  `id`          BIGINT      NOT NULL AUTO_INCREMENT                           COMMENT '场次ID',
  `title`       VARCHAR(64) NOT NULL                                          COMMENT '场次名称(如 10:00场)',
  `date`        DATE        NOT NULL                                          COMMENT '活动日期',
  `begin_time`  DATETIME    NOT NULL                                          COMMENT '开始时间',
  `end_time`    DATETIME    NOT NULL                                          COMMENT '结束时间',
  `status`      TINYINT     NOT NULL DEFAULT 1                                COMMENT '状态(0禁用,1启用)',
  `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP                COMMENT '创建时间',
  `update_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP
                            ON UPDATE CURRENT_TIMESTAMP                       COMMENT '更新时间',
  `deleted`     TINYINT     NOT NULL DEFAULT 0                                COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_session_date_status` (`date`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='秒杀场次表';


-- ----------------------------
-- 32. 秒杀优惠券表
-- ----------------------------
DROP TABLE IF EXISTS `tb_seckill_voucher`;
CREATE TABLE `tb_seckill_voucher` (
  `voucher_id`  BIGINT       NOT NULL                                         COMMENT '关联优惠券ID',
  `session_id`  BIGINT       NOT NULL DEFAULT 0                               COMMENT '所属场次ID',
  `total_stock` INT UNSIGNED NOT NULL DEFAULT 0                               COMMENT '初始总库存(不变)',
  `stock`       INT UNSIGNED NOT NULL DEFAULT 0                               COMMENT '剩余库存(UNSIGNED防超卖)',
  `begin_time`  DATETIME     NOT NULL                                         COMMENT '秒杀开始时间',
  `end_time`    DATETIME     NOT NULL                                         COMMENT '秒杀结束时间',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP               COMMENT '创建时间',
  `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
                             ON UPDATE CURRENT_TIMESTAMP                      COMMENT '更新时间',
  `deleted`     TINYINT      NOT NULL DEFAULT 0                               COMMENT '逻辑删除(0未删,1已删)',
  PRIMARY KEY (`voucher_id`),
  KEY `idx_seckill_session` (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='秒杀优惠券表';


-- ----------------------------
-- 33. 优惠券订单表
-- ----------------------------
DROP TABLE IF EXISTS `tb_voucher_order`;
CREATE TABLE `tb_voucher_order` (
  `id`          BIGINT      NOT NULL                                          COMMENT '订单ID(全局唯一,雪花算法)',
  `order_no`    VARCHAR(32) NOT NULL                                          COMMENT '订单编号(对外展示,如YX20260324000001)',
  `user_id`     BIGINT      NOT NULL                                          COMMENT '下单用户ID',
  `voucher_id`  BIGINT      NOT NULL                                          COMMENT '优惠券ID',
  `pay_type`    TINYINT     NOT NULL DEFAULT 0                                COMMENT '支付方式(0未支付,1余额,2微信,3支付宝)',
  `status`      TINYINT     NOT NULL DEFAULT 0                                COMMENT '订单状态(0未支付,1已支付,2已使用,3已退款,4已取消)',
  `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP                COMMENT '创建时间',
  `pay_time`    DATETIME    DEFAULT NULL                                      COMMENT '支付时间',
  `use_time`    DATETIME    DEFAULT NULL                                      COMMENT '使用时间',
  `refund_time` DATETIME    DEFAULT NULL                                      COMMENT '退款时间',
  `update_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP
                            ON UPDATE CURRENT_TIMESTAMP                       COMMENT '更新时间',
  `deleted`     TINYINT     NOT NULL DEFAULT 0                                COMMENT '逻辑删除(0未删,1已删)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no`                  (`order_no`),
  KEY `idx_voucher_order_user_voucher`      (`user_id`, `voucher_id`),
  KEY `idx_voucher_order_voucher_id`        (`voucher_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='优惠券订单表';


-- ----------------------------
-- 34. 支付流水表
-- ----------------------------
DROP TABLE IF EXISTS `tb_payment_record`;
CREATE TABLE `tb_payment_record` (
  `id`               BIGINT      NOT NULL AUTO_INCREMENT                      COMMENT '主键ID',
  `payment_no`       VARCHAR(64) NOT NULL                                     COMMENT '支付流水号(平台生成)',
  `third_payment_no` VARCHAR(64) DEFAULT NULL                                 COMMENT '第三方流水号(微信,支付宝)',
  `user_id`          BIGINT      NOT NULL                                     COMMENT '用户ID',
  `order_id`         BIGINT      NOT NULL                                     COMMENT '关联订单ID',
  `amount`           BIGINT      NOT NULL                                     COMMENT '金额(分)',
  `pay_type`         TINYINT     NOT NULL                                     COMMENT '支付方式(1余额,2微信,3支付宝)',
  `direction`        TINYINT     NOT NULL                                     COMMENT '方向(1收入/退款到账,2支出/支付)',
  `status`           TINYINT     NOT NULL DEFAULT 0                           COMMENT '状态(0处理中,1成功,2失败)',
  `remark`           VARCHAR(255) DEFAULT NULL                                COMMENT '备注',
  `create_time`      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP           COMMENT '创建时间',
  `finish_time`      DATETIME    DEFAULT NULL                                 COMMENT '完成时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_payment_no`     (`payment_no`),
  KEY `idx_payment_third_no`     (`third_payment_no`),
  KEY `idx_payment_user_id`      (`user_id`),
  KEY `idx_payment_order_id`     (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付流水表';


-- ----------------------------
-- 35. 退款记录表
-- ----------------------------
DROP TABLE IF EXISTS `tb_refund_record`;
CREATE TABLE `tb_refund_record` (
  `id`              BIGINT       NOT NULL AUTO_INCREMENT                      COMMENT '主键ID',
  `refund_no`       VARCHAR(64)  NOT NULL                                     COMMENT '退款单号(平台生成)',
  `third_refund_no` VARCHAR(64)  DEFAULT NULL                                 COMMENT '第三方退款单号',
  `order_id`        BIGINT       NOT NULL                                     COMMENT '原订单ID',
  `user_id`         BIGINT       NOT NULL                                     COMMENT '用户ID',
  `refund_amount`   BIGINT       NOT NULL                                     COMMENT '退款金额(分)',
  `reason`          VARCHAR(512) DEFAULT NULL                                 COMMENT '退款原因',
  `status`          TINYINT      NOT NULL DEFAULT 0                           COMMENT '状态(0申请中,1审核通过,2退款成功,3已拒绝)',
  `audit_by`        BIGINT       DEFAULT NULL                                 COMMENT '审核人(运营ID)',
  `audit_time`      DATETIME     DEFAULT NULL                                 COMMENT '审核时间',
  `refund_time`     DATETIME     DEFAULT NULL                                 COMMENT '退款到账时间',
  `reject_reason`   VARCHAR(512) DEFAULT NULL                                 COMMENT '拒绝原因',
  `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP           COMMENT '创建时间',
  `update_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
                                 ON UPDATE CURRENT_TIMESTAMP                  COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_refund_no`     (`refund_no`),
  KEY `idx_refund_order_id`     (`order_id`),
  KEY `idx_refund_user_id`      (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='退款记录表';


-- ╔══════════════════════════════════════════════════════════════╗
-- ║             第五部分：平台运营域（9张表）                     ║
-- ╚══════════════════════════════════════════════════════════════╝


-- ----------------------------
-- 36. 轮播图Banner表
-- ----------------------------
DROP TABLE IF EXISTS `tb_banner`;
CREATE TABLE `tb_banner` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT                          COMMENT '主键ID',
  `title`       VARCHAR(128) NOT NULL                                         COMMENT 'Banner标题',
  `image`       VARCHAR(255) NOT NULL                                         COMMENT '图片(相对路径)',
  `link_type`   TINYINT      NOT NULL DEFAULT 0                               COMMENT '跳转类型(0无跳转,1商户,2笔记,3优惠券,4外链)',
  `link_value`  VARCHAR(512) DEFAULT NULL                                     COMMENT '跳转值(ID或URL)',
  `position`    VARCHAR(32)  NOT NULL DEFAULT 'home'                          COMMENT '投放位置(home首页,shop商户页,type分类页)',
  `sort`        INT          NOT NULL DEFAULT 0                               COMMENT '排序',
  `status`      TINYINT      NOT NULL DEFAULT 0                               COMMENT '状态(0下线,1上线)',
  `begin_time`  DATETIME     DEFAULT NULL                                     COMMENT '生效时间',
  `end_time`    DATETIME     DEFAULT NULL                                     COMMENT '失效时间',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP               COMMENT '创建时间',
  `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
                             ON UPDATE CURRENT_TIMESTAMP                      COMMENT '更新时间',
  `created_by`  BIGINT       DEFAULT NULL                                     COMMENT '创建人ID',
  `deleted`     TINYINT      NOT NULL DEFAULT 0                               COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_banner_position_status` (`position`, `status`, `sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='轮播图Banner表';


-- ----------------------------
-- 37. 消息通知表
-- ----------------------------
DROP TABLE IF EXISTS `tb_message`;
CREATE TABLE `tb_message` (
  `id`           BIGINT        NOT NULL AUTO_INCREMENT                        COMMENT '主键ID',
  `from_user_id` BIGINT        NOT NULL DEFAULT 0                             COMMENT '发送者ID(0=系统)',
  `to_user_id`   BIGINT        NOT NULL                                       COMMENT '接收者ID',
  `type`         TINYINT       NOT NULL                                       COMMENT '类型(1系统通知,2点赞,3评论,4关注,5优惠券到账,6订单状态)',
  `title`        VARCHAR(128)  DEFAULT NULL                                   COMMENT '消息标题',
  `content`      VARCHAR(1024) NOT NULL                                       COMMENT '消息内容',
  `biz_type`     VARCHAR(32)   DEFAULT NULL                                   COMMENT '关联业务类型(BLOG,COMMENT,ORDER等)',
  `biz_id`       BIGINT        DEFAULT NULL                                   COMMENT '关联业务ID(点击跳转用)',
  `is_read`      TINYINT       NOT NULL DEFAULT 0                             COMMENT '是否已读(0未读,1已读)',
  `read_time`    DATETIME      DEFAULT NULL                                   COMMENT '阅读时间',
  `create_time`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP             COMMENT '创建时间',
  `deleted`      TINYINT       NOT NULL DEFAULT 0                             COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_message_to_user` (`to_user_id`, `is_read`, `create_time`),
  KEY `idx_message_type`    (`to_user_id`, `type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息通知表';


-- ----------------------------
-- 38. 短信验证码表
-- ----------------------------
DROP TABLE IF EXISTS `tb_sms_code`;
CREATE TABLE `tb_sms_code` (
  `id`          BIGINT      NOT NULL AUTO_INCREMENT                           COMMENT '主键ID',
  `phone`       VARCHAR(20) NOT NULL                                          COMMENT '手机号',
  `code`        VARCHAR(6)  NOT NULL                                          COMMENT '验证码',
  `type`        TINYINT     NOT NULL DEFAULT 0                                COMMENT '类型(0登录,1注册,2找回密码,3绑定手机)',
  `status`      TINYINT     NOT NULL DEFAULT 0                                COMMENT '状态(0未使用,1已使用,2已过期)',
  `ip`          VARCHAR(45) DEFAULT NULL                                      COMMENT '请求IP(防刷)',
  `expire_time` DATETIME    NOT NULL                                          COMMENT '过期时间',
  `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP                COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_sms_phone_type` (`phone`, `type`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='短信验证码表';


-- ----------------------------
-- 39. 操作日志表 (审计追踪)
-- ----------------------------
DROP TABLE IF EXISTS `tb_operation_log`;
CREATE TABLE `tb_operation_log` (
  `id`             BIGINT        NOT NULL AUTO_INCREMENT                      COMMENT '主键ID',
  `user_id`        BIGINT        DEFAULT NULL                                 COMMENT '操作人ID',
  `user_type`      TINYINT       NOT NULL DEFAULT 0                           COMMENT '操作人类型(0系统,1用户,2运营,3管理员)',
  `module`         VARCHAR(64)   NOT NULL                                     COMMENT '操作模块(SHOP,BLOG,VOUCHER,ORDER等)',
  `action`         VARCHAR(64)   NOT NULL                                     COMMENT '操作动作(CREATE,UPDATE,DELETE,AUDIT等)',
  `target_id`      BIGINT        DEFAULT NULL                                 COMMENT '目标ID',
  `description`    VARCHAR(512)  DEFAULT NULL                                 COMMENT '操作描述',
  `request_method` VARCHAR(10)   DEFAULT NULL                                 COMMENT '请求方法(GET,POST等)',
  `request_url`    VARCHAR(512)  DEFAULT NULL                                 COMMENT '请求URL',
  `request_body`   TEXT          DEFAULT NULL                                 COMMENT '请求参数(脱敏后的JSON)',
  `ip`             VARCHAR(45)   DEFAULT NULL                                 COMMENT '操作IP',
  `user_agent`     VARCHAR(512)  DEFAULT NULL                                 COMMENT '浏览器UA',
  `cost_time`      INT UNSIGNED  DEFAULT NULL                                 COMMENT '耗时(毫秒)',
  `status`         TINYINT       NOT NULL DEFAULT 1                           COMMENT '结果(0失败,1成功)',
  `error_msg`      VARCHAR(2048) DEFAULT NULL                                 COMMENT '异常信息',
  `create_time`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP           COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_oplog_user_id`     (`user_id`),
  KEY `idx_oplog_module`      (`module`, `action`),
  KEY `idx_oplog_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';


-- ----------------------------
-- 40. 敏感词库表
-- ----------------------------
DROP TABLE IF EXISTS `tb_sensitive_word`;
CREATE TABLE `tb_sensitive_word` (
  `id`          BIGINT      NOT NULL AUTO_INCREMENT                           COMMENT '主键ID',
  `word`        VARCHAR(64) NOT NULL                                          COMMENT '敏感词',
  `category`    TINYINT     NOT NULL DEFAULT 0                                COMMENT '分类(0通用,1政治,2色情,3广告,4辱骂)',
  `level`       TINYINT     NOT NULL DEFAULT 1                                COMMENT '级别(1提醒,2替换,3拦截)',
  `replace_to`  VARCHAR(64) DEFAULT ''                                       COMMENT '替换文本(level=2时使用)',
  `status`      TINYINT     NOT NULL DEFAULT 1                                COMMENT '状态(0禁用,1启用)',
  `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP                COMMENT '创建时间',
  `update_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP
                            ON UPDATE CURRENT_TIMESTAMP                       COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sensitive_word`   (`word`),
  KEY `idx_sensitive_category`     (`category`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='敏感词库表';


-- ----------------------------
-- 41. 系统配置表 (键值对)
-- ----------------------------
DROP TABLE IF EXISTS `tb_sys_config`;
CREATE TABLE `tb_sys_config` (
  `id`           BIGINT        NOT NULL AUTO_INCREMENT                        COMMENT '主键ID',
  `config_key`   VARCHAR(128)  NOT NULL                                       COMMENT '配置键(如sign.points.daily)',
  `config_value` VARCHAR(1024) NOT NULL                                       COMMENT '配置值',
  `config_type`  VARCHAR(32)   NOT NULL DEFAULT 'STRING'                      COMMENT '值类型(STRING,NUMBER,JSON,BOOLEAN)',
  `group_name`   VARCHAR(64)   NOT NULL DEFAULT 'default'                     COMMENT '分组(sign,review,order等)',
  `description`  VARCHAR(255)  DEFAULT NULL                                   COMMENT '配置说明',
  `editable`     TINYINT       NOT NULL DEFAULT 1                             COMMENT '是否可编辑(0否,1是)',
  `create_time`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP             COMMENT '创建时间',
  `update_time`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP
                               ON UPDATE CURRENT_TIMESTAMP                    COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key`  (`config_key`),
  KEY `idx_config_group`      (`group_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';


-- ----------------------------
-- 42. 地区字典表 (省市区三级)
-- ----------------------------
DROP TABLE IF EXISTS `tb_region`;
CREATE TABLE `tb_region` (
  `id`        INT         NOT NULL                                            COMMENT '地区编码(如110000)',
  `name`      VARCHAR(64) NOT NULL                                            COMMENT '地区名称',
  `parent_id` INT         NOT NULL DEFAULT 0                                  COMMENT '父级编码(0=省/直辖市)',
  `level`     TINYINT     NOT NULL                                            COMMENT '层级(1省,2市,3区)',
  `sort`      INT         NOT NULL DEFAULT 0                                  COMMENT '排序',
  `status`    TINYINT     NOT NULL DEFAULT 1                                  COMMENT '状态(0禁用,1启用)',
  PRIMARY KEY (`id`),
  KEY `idx_region_parent` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='地区字典表';


-- ----------------------------
-- 43. AI探店会话表
-- ----------------------------
DROP TABLE IF EXISTS `tb_ai_session`;
CREATE TABLE `tb_ai_session` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT                        COMMENT '主键ID',
  `session_id`    VARCHAR(64)  NOT NULL                                       COMMENT '会话ID(UUID)',
  `user_id`       BIGINT       NOT NULL                                       COMMENT '用户ID',
  `title`         VARCHAR(255) DEFAULT NULL                                   COMMENT '会话标题(首条问题)',
  `message_count` INT          NOT NULL DEFAULT 0                             COMMENT '消息数量',
  `shop_ids`      TEXT         DEFAULT NULL                                   COMMENT '推荐的商户ID(JSON数组)',
  `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP             COMMENT '创建时间',
  `update_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
                               ON UPDATE CURRENT_TIMESTAMP                    COMMENT '更新时间',
  `deleted`       TINYINT      NOT NULL DEFAULT 0                             COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ai_session_id` (`session_id`),
  KEY `idx_ai_session_user`    (`user_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI探店会话表';


-- ----------------------------
-- 44. AI探店消息表
-- ----------------------------
DROP TABLE IF EXISTS `tb_ai_message`;
CREATE TABLE `tb_ai_message` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT                          COMMENT '主键ID',
  `session_id`  VARCHAR(64)  NOT NULL                                         COMMENT '会话ID',
  `role`        VARCHAR(16)  NOT NULL                                         COMMENT '角色',
  `content`     TEXT         NOT NULL                                         COMMENT '消息内容',
  `shop_ids`    TEXT         DEFAULT NULL                                     COMMENT '本条消息关联的商户ID(JSON数组)',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP               COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_ai_message_session` (`session_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI探店消息表';


SET FOREIGN_KEY_CHECKS = 1;


-- ==========================================================
-- 全部表总览 (共 44 张)
-- ==========================================================
-- ┌──────┬──────────────────────────┬───────────┬──────────────────────────────┐
-- │  #   │ 表名                     │ 业务域     │ 说明                          │
-- ├──────┼──────────────────────────┼───────────┼──────────────────────────────┤
-- │  1   │ tb_user                  │ 用户域     │ 用户基本信息                   │
-- │  2   │ tb_user_info             │ 用户域     │ 用户详情(等级积分粉丝获赞)     │
-- │  3   │ tb_user_wallet           │ 用户域     │ 用户钱包(余额/冻结/乐观锁)     │
-- │  4   │ tb_points_record         │ 用户域     │ 积分变动流水                   │
-- │  5   │ tb_member_level          │ 用户域     │ 会员等级配置                   │
-- │  6   │ tb_user_auth             │ 用户域     │ 实名认证                      │
-- │  7   │ tb_feedback              │ 用户域     │ 用户反馈意见                   │
-- │  8   │ tb_sign                  │ 用户域     │ 签到月度汇总(位图)             │
-- │  9   │ tb_sign_reward_rule      │ 用户域     │ 签到里程碑奖励规则              │
-- │ 10   │ tb_sign_reward_record    │ 用户域     │ 签到奖励领取记录               │
-- │ 11   │ tb_sign_repair_record    │ 用户域     │ 签到补签记录                   │
-- ├──────┼──────────────────────────┼───────────┼──────────────────────────────┤
-- │ 12   │ tb_shop_type             │ 商户域     │ 商户类型                      │
-- │ 13   │ tb_shop                  │ 商户域     │ 商户信息(含联系电话)           │
-- │ 14   │ tb_review                │ 商户域     │ 商户评价明细(多维评分)          │
-- │ 15   │ tb_shop_apply            │ 商户域     │ 商户入驻申请                   │
-- │ 16   │ tb_shop_ai_analysis      │ 商户域     │ 商户AI口碑分析                 │
-- ├──────┼──────────────────────────┼───────────┼──────────────────────────────┤
-- │ 17   │ tb_blog                  │ 内容域     │ 探店笔记                      │
-- │ 18   │ tb_blog_comments         │ 内容域     │ 笔记评论(楼中楼)               │
-- │ 19   │ tb_blog_likes            │ 内容域     │ 笔记点赞记录                   │
-- │ 20   │ tb_comment_likes         │ 内容域     │ 评论点赞记录                   │
-- │ 21   │ tb_like_dead_letter      │ 内容域     │ 点赞死信表(MQ消费失败兜底)      │
-- │ 22   │ tb_follow                │ 内容域     │ 用户关注关系                   │
-- │ 23   │ tb_favorite              │ 内容域     │ 收藏(商户/笔记)               │
-- │ 24   │ tb_tag                   │ 内容域     │ 标签话题池                     │
-- │ 25   │ tb_tag_relation          │ 内容域     │ 标签关联(多对多)               │
-- │ 26   │ tb_browse_history        │ 内容域     │ 浏览历史                      │
-- │ 27   │ tb_search_history        │ 内容域     │ 搜索历史                      │
-- │ 28   │ tb_hot_search            │ 内容域     │ 热搜词统计                     │
-- │ 29   │ tb_report                │ 内容域     │ 举报记录(通用)                 │
-- ├──────┼──────────────────────────┼───────────┼──────────────────────────────┤
-- │ 30   │ tb_voucher               │ 交易域     │ 优惠券                        │
-- │ 31   │ tb_seckill_session       │ 交易域     │ 秒杀场次                      │
-- │ 32   │ tb_seckill_voucher       │ 交易域     │ 秒杀优惠券(库存防超卖)          │
-- │ 33   │ tb_voucher_order         │ 交易域     │ 优惠券订单                     │
-- │ 34   │ tb_payment_record        │ 交易域     │ 支付流水                      │
-- │ 35   │ tb_refund_record         │ 交易域     │ 退款记录                      │
-- ├──────┼──────────────────────────┼───────────┼──────────────────────────────┤
-- │ 36   │ tb_banner                │ 运营域     │ 轮播图Banner                   │
-- │ 37   │ tb_message               │ 运营域     │ 消息通知                      │
-- │ 38   │ tb_sms_code              │ 运营域     │ 短信验证码                     │
-- │ 39   │ tb_operation_log         │ 运营域     │ 操作日志(审计)                 │
-- │ 40   │ tb_sensitive_word        │ 运营域     │ 敏感词库                      │
-- │ 41   │ tb_sys_config            │ 运营域     │ 系统配置(键值对)               │
-- │ 42   │ tb_region                │ 运营域     │ 地区字典(省市区)               │
-- │ 43   │ tb_ai_session            │ 运营域     │ AI探店会话                     │
-- │ 44   │ tb_ai_message            │ 运营域     │ AI探店消息                     │
-- └──────┴──────────────────────────┴───────────┴──────────────────────────────┘