-- ============================================
-- 更新排队号码字段长度以支持年月日时分秒格式
-- 旧格式：A20260518001（11位）
-- 新格式：A20260522143045001（17位）
-- ============================================

USE queue_service;

-- 修改 queue_no 字段长度
ALTER TABLE `queue_number` 
MODIFY COLUMN `queue_no` VARCHAR(50) NOT NULL COMMENT '排队号码，唯一标识，格式：前缀+年月日时分秒+序列号，如：A20260522143045001';

-- 修改 call_record 表中的 queue_no 字段长度
ALTER TABLE `call_record` 
MODIFY COLUMN `queue_no` VARCHAR(50) NOT NULL COMMENT '排队号码';

-- 修改 queue_operation_log 表中的 queue_no 字段长度
ALTER TABLE `queue_operation_log` 
MODIFY COLUMN `queue_no` VARCHAR(50) DEFAULT NULL COMMENT '排队号码';

-- 验证字段长度是否修改成功
DESCRIBE `queue_number`;
DESCRIBE `call_record`;
DESCRIBE `queue_operation_log`;