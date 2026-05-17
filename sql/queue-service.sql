-- ============================================
-- 排队服务数据库 (queue-service)
-- 功能：排队取号逻辑、叫号管理
-- ============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS queue_service DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE queue_service;

-- ============================================
-- 1. 排队号码表
-- ============================================
DROP TABLE IF EXISTS `queue_number`;
CREATE TABLE `queue_number` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '排队ID，主键自增',
  `queue_no` VARCHAR(50) NOT NULL COMMENT '排队号码，唯一标识，格式：A001、B002',
  `shop_id` BIGINT(20) NOT NULL COMMENT '店铺ID，关联shop_info.id',
  `user_id` BIGINT(20) DEFAULT NULL COMMENT '用户ID',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
  `party_size` INT(11) NOT NULL DEFAULT 1 COMMENT '用餐人数',
  `queue_type` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '排队类型：1-堂食，2-外带',
  `table_type` TINYINT(1) DEFAULT NULL COMMENT '期望桌台类型：1-普通桌，2-卡座，3-包厢，NULL-不限制',
  `queue_status` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '排队状态：0-等待中，1-已叫号，2-已入座，3-已取消，4-已过号',
  `current_position` INT(11) DEFAULT NULL COMMENT '当前排队位置',
  `total_ahead` INT(11) DEFAULT NULL COMMENT '前方等待人数',
  `estimated_wait_time` INT(11) DEFAULT NULL COMMENT '预计等待时间（分钟）',
  `call_time` DATETIME DEFAULT NULL COMMENT '叫号时间',
  `seat_time` DATETIME DEFAULT NULL COMMENT '入座时间',
  `cancel_time` DATETIME DEFAULT NULL COMMENT '取消时间',
  `cancel_reason` VARCHAR(255) DEFAULT NULL COMMENT '取消原因',
  `expire_time` DATETIME DEFAULT NULL COMMENT '过期时间',
  `is_notified` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已通知：0-否，1-是',
  `notify_count` INT(11) NOT NULL DEFAULT 0 COMMENT '通知次数',
  `last_notify_time` DATETIME DEFAULT NULL COMMENT '最后通知时间',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间（取号时间）',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_queue_no` (`shop_id`, `queue_no`),
  KEY `idx_shop_id` (`shop_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_queue_status` (`queue_status`),
  KEY `idx_phone` (`phone`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='排队号码表';

-- 插入示例数据
INSERT INTO `queue_number` (`queue_no`, `shop_id`, `user_id`, `phone`, `party_size`, `queue_type`, `queue_status`, `current_position`, `total_ahead`, `estimated_wait_time`, `call_time`, `seat_time`) VALUES
('A001', 1, 1001, '13800138001', 3, 1, 2, NULL, 0, NULL, '2026-05-17 11:20:00', '2026-05-17 11:35:00'),
('A002', 1, 1002, '13800138002', 2, 1, 1, 1, 0, 5, '2026-05-17 11:40:00', NULL),
('A003', 1, 1003, '13800138003', 4, 1, 0, 2, 1, 10, NULL, NULL),
('A004', 1, NULL, '13800138004', 2, 1, 0, 3, 2, 15, NULL, NULL),
('B001', 1, 1004, '13800138005', 5, 1, 0, 4, 3, 20, NULL, NULL);

-- ============================================
-- 2. 叫号记录表
-- ============================================
DROP TABLE IF EXISTS `call_record`;
CREATE TABLE `call_record` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '记录ID，主键自增',
  `queue_id` BIGINT(20) NOT NULL COMMENT '排队ID，关联queue_number.id',
  `queue_no` VARCHAR(50) NOT NULL COMMENT '排队号码',
  `shop_id` BIGINT(20) NOT NULL COMMENT '店铺ID',
  `call_type` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '叫号类型：1-首次叫号，2-重复叫号，3-过号重呼',
  `call_method` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '叫号方式：1-语音播报，2-短信通知，3-微信推送，4-APP推送',
  `call_count` INT(11) NOT NULL DEFAULT 1 COMMENT '叫号次数',
  `operator_id` BIGINT(20) DEFAULT NULL COMMENT '操作人ID（店员）',
  `operator_name` VARCHAR(50) DEFAULT NULL COMMENT '操作人姓名',
  `is_answered` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否应答：0-未应答，1-已应答',
  `answer_time` DATETIME DEFAULT NULL COMMENT '应答时间',
  `timeout_duration` INT(11) DEFAULT 300 COMMENT '超时时长（秒），默认5分钟',
  `is_expired` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已过期：0-否，1-是',
  `expire_time` DATETIME DEFAULT NULL COMMENT '过期时间',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间（叫号时间）',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_queue_id` (`queue_id`),
  KEY `idx_queue_no` (`queue_no`),
  KEY `idx_shop_id` (`shop_id`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='叫号记录表';

-- 插入示例数据
INSERT INTO `call_record` (`queue_id`, `queue_no`, `shop_id`, `call_type`, `call_method`, `call_count`, `operator_id`, `operator_name`, `is_answered`, `answer_time`) VALUES
(1, 'A001', 1, 1, 1, 1, 3001, '服务员李四', 1, '2026-05-17 11:35:00'),
(2, 'A002', 1, 1, 1, 1, 3001, '服务员李四', 0, NULL);

-- ============================================
-- 3. 排队配置表
-- ============================================
DROP TABLE IF EXISTS `queue_config`;
CREATE TABLE `queue_config` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '配置ID，主键自增',
  `shop_id` BIGINT(20) NOT NULL COMMENT '店铺ID，关联shop_info.id',
  `config_key` VARCHAR(100) NOT NULL COMMENT '配置键名',
  `config_value` TEXT COMMENT '配置值',
  `config_desc` VARCHAR(255) DEFAULT NULL COMMENT '配置说明',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_shop_config` (`shop_id`, `config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='排队配置表';

-- 插入示例配置数据
INSERT INTO `queue_config` (`shop_id`, `config_key`, `config_value`, `config_desc`) VALUES
(1, 'queue_enabled', 'true', '是否启用排队功能'),
(1, 'max_queue_size', '50', '最大排队数量'),
(1, 'auto_call_enabled', 'true', '是否自动叫号'),
(1, 'auto_call_interval', '300', '自动叫号间隔（秒）'),
(1, 'call_timeout', '300', '叫号超时时间（秒）'),
(1, 'max_reservations', '3', '最多可预约次数'),
(1, 'notify_sms_enabled', 'true', '是否启用短信通知'),
(1, 'notify_wechat_enabled', 'true', '是否启用微信通知'),
(1, 'notify_advance_time', '10', '提前通知时间（分钟）'),
(1, 'queue_prefix_A', 'A', 'A类队列前缀（普通桌）'),
(1, 'queue_prefix_B', 'B', 'B类队列前缀（卡座）'),
(1, 'queue_prefix_V', 'V', 'V类队列前缀（包厢）');

-- ============================================
-- 4. 排队统计表
-- ============================================
DROP TABLE IF EXISTS `queue_statistics`;
CREATE TABLE `queue_statistics` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '统计ID，主键自增',
  `shop_id` BIGINT(20) NOT NULL COMMENT '店铺ID',
  `stat_date` DATE NOT NULL COMMENT '统计日期',
  `total_queue_count` INT(11) NOT NULL DEFAULT 0 COMMENT '总排队数',
  `served_count` INT(11) NOT NULL DEFAULT 0 COMMENT '已服务数',
  `cancelled_count` INT(11) NOT NULL DEFAULT 0 COMMENT '取消数',
  `expired_count` INT(11) NOT NULL DEFAULT 0 COMMENT '过号数',
  `avg_wait_time` INT(11) DEFAULT NULL COMMENT '平均等待时间（分钟）',
  `max_wait_time` INT(11) DEFAULT NULL COMMENT '最长等待时间（分钟）',
  `min_wait_time` INT(11) DEFAULT NULL COMMENT '最短等待时间（分钟）',
  `peak_hour` VARCHAR(20) DEFAULT NULL COMMENT '高峰时段',
  `peak_queue_count` INT(11) DEFAULT NULL COMMENT '高峰排队数',
  `avg_party_size` DECIMAL(5, 2) DEFAULT NULL COMMENT '平均用餐人数',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_shop_date` (`shop_id`, `stat_date`),
  KEY `idx_stat_date` (`stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='排队统计表';

-- 插入示例统计数据
INSERT INTO `queue_statistics` (`shop_id`, `stat_date`, `total_queue_count`, `served_count`, `cancelled_count`, `expired_count`, `avg_wait_time`, `max_wait_time`, `min_wait_time`, `peak_hour`, `peak_queue_count`, `avg_party_size`) VALUES
(1, '2026-05-16', 85, 78, 5, 2, 18, 45, 5, '12:00-13:00', 15, 3.2),
(1, '2026-05-17', 42, 35, 3, 1, 16, 38, 8, '11:30-12:30', 12, 2.9);

-- ============================================
-- 5. 排队操作日志表
-- ============================================
DROP TABLE IF EXISTS `queue_operation_log`;
CREATE TABLE `queue_operation_log` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '日志ID，主键自增',
  `queue_id` BIGINT(20) DEFAULT NULL COMMENT '排队ID',
  `queue_no` VARCHAR(50) DEFAULT NULL COMMENT '排队号码',
  `shop_id` BIGINT(20) NOT NULL COMMENT '店铺ID',
  `operation_type` VARCHAR(50) NOT NULL COMMENT '操作类型：TAKE_NUMBER-取号，CALL_NUMBER-叫号，CANCEL_NUMBER-取消，SEAT_NUMBER-入座，SKIP_NUMBER-过号',
  `operation_desc` VARCHAR(255) DEFAULT NULL COMMENT '操作描述',
  `old_status` TINYINT(1) DEFAULT NULL COMMENT '原状态',
  `new_status` TINYINT(1) DEFAULT NULL COMMENT '新状态',
  `request_data` TEXT COMMENT '请求数据，JSON格式',
  `response_data` TEXT COMMENT '响应数据，JSON格式',
  `operator_type` TINYINT(1) NOT NULL COMMENT '操作人类型：1-系统，2-用户，3-店员',
  `operator_id` BIGINT(20) DEFAULT NULL COMMENT '操作人ID',
  `operator_name` VARCHAR(50) DEFAULT NULL COMMENT '操作人姓名',
  `ip_address` VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
  `duration` INT(11) DEFAULT NULL COMMENT '操作耗时（毫秒）',
  `result` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '操作结果：0-失败，1-成功',
  `error_msg` TEXT COMMENT '错误信息',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_queue_id` (`queue_id`),
  KEY `idx_queue_no` (`queue_no`),
  KEY `idx_shop_id` (`shop_id`),
  KEY `idx_operation_type` (`operation_type`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='排队操作日志表';

-- 插入示例数据
INSERT INTO `queue_operation_log` (`queue_id`, `queue_no`, `shop_id`, `operation_type`, `operation_desc`, `old_status`, `new_status`, `operator_type`, `operator_id`, `operator_name`, `result`) VALUES
(1, 'A001', 1, 'TAKE_NUMBER', '用户取号', NULL, 0, 2, 1001, '用户1001', 1),
(1, 'A001', 1, 'CALL_NUMBER', '店员叫号', 0, 1, 3, 3001, '服务员李四', 1),
(1, 'A001', 1, 'SEAT_NUMBER', '用户入座', 1, 2, 3, 3001, '服务员李四', 1),
(2, 'A002', 1, 'TAKE_NUMBER', '用户取号', NULL, 0, 2, 1002, '用户1002', 1),
(2, 'A002', 1, 'CALL_NUMBER', '店员叫号', 0, 1, 3, 3001, '服务员李四', 1);

-- ============================================
-- 6. 黑名单表（防止恶意取号）
-- ============================================
DROP TABLE IF EXISTS `queue_blacklist`;
CREATE TABLE `queue_blacklist` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '黑名单ID，主键自增',
  `shop_id` BIGINT(20) NOT NULL COMMENT '店铺ID',
  `user_id` BIGINT(20) DEFAULT NULL COMMENT '用户ID',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '电话号码',
  `ip_address` VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
  `device_id` VARCHAR(100) DEFAULT NULL COMMENT '设备ID',
  `blacklist_type` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '黑名单类型：1-用户，2-电话，3-IP，4-设备',
  `reason` VARCHAR(255) NOT NULL COMMENT '拉黑原因',
  `violation_count` INT(11) NOT NULL DEFAULT 1 COMMENT '违规次数',
  `expire_time` DATETIME DEFAULT NULL COMMENT '过期时间，NULL表示永久',
  `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态：0-已解除，1-生效中',
  `operator_id` BIGINT(20) DEFAULT NULL COMMENT '操作人ID',
  `operator_name` VARCHAR(50) DEFAULT NULL COMMENT '操作人姓名',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_shop_id` (`shop_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_phone` (`phone`),
  KEY `idx_ip_address` (`ip_address`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='排队黑名单表';
