-- =============================================
-- 添加用户在线状态字段
-- 说明: 为 sys_user 表添加 is_online 字段，用于跟踪用户在线状态
-- 执行时间: 2026-05-18
-- =============================================

-- 1. 添加 is_online 字段
ALTER TABLE sys_user 
ADD COLUMN is_online TINYINT DEFAULT 0 COMMENT '是否在线：0-离线，1-在线' AFTER status;

-- 2. 添加索引
ALTER TABLE sys_user 
ADD INDEX idx_is_online (is_online);

-- 3. 验证字段是否添加成功
SELECT id, username, nickname, status, is_online, last_login_time 
FROM sys_user;

-- 完成提示
SELECT '✅ 用户在线状态字段添加成功！' AS message;
