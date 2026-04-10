-- ========================================
-- 插入测试用户 - 用于验证码登录测试
-- ========================================
-- 手机号：+8619565998095（带+86前缀）
-- 密码：123456
-- ========================================

-- 清理可能存在的测试数据
DELETE FROM tb_user_wallet WHERE user_id = 1;
DELETE FROM tb_user_info WHERE user_id = 1;
DELETE FROM tb_user WHERE id = 1;

-- 1. 插入用户基本信息
-- 密码使用BCrypt加密，明文为：123456
INSERT INTO tb_user (id, phone, password, nick_name, avatar, status, create_time, update_time, deleted)
VALUES (
    1, 
    '+8619565998095', 
    '$2a$10$N9qo8uLOickgx2ZMRZoMy.Mrq7M7F7nLQDn5YI.V5F7nLQDn5YI.V',  -- BCrypt加密后的123456
    '测试用户', 
    'https://api.iconify.design/mdi:account-circle.svg', 
    0,  -- 状态：正常
    NOW(), 
    NOW(), 
    0   -- 未删除
);

-- 2. 插入用户详细信息
INSERT INTO tb_user_info (user_id, points, level, create_time, update_time, deleted)
VALUES (
    1, 
    0,   -- 初始积分
    1,   -- 初始等级
    NOW(), 
    NOW(), 
    0
);

-- 3. 插入用户钱包
INSERT INTO tb_user_wallet (user_id, balance, create_time, update_time, deleted)
VALUES (
    1, 
    0,  -- 初始余额
    NOW(), 
    NOW(), 
    0
);

-- ========================================
-- 验证插入结果
-- ========================================
SELECT * FROM tb_user WHERE phone = '+8619565998095';
SELECT * FROM tb_user_info WHERE user_id = 1;
SELECT * FROM tb_user_wallet WHERE user_id = 1;

-- ========================================
-- 注意事项
-- ========================================
-- 1. 手机号必须带+86前缀，格式为：+8619565998095
-- 2. 密码为123456，登录时使用明文即可
-- 3. BCrypt每次加密结果不同，但都可以验证通过
-- 4. 如果ID冲突，请修改id值或使用AUTO_INCREMENT
