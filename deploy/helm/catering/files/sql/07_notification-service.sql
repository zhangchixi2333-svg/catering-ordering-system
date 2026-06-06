-- ============================================
-- 通知服务数据库 (notification-service)
-- 功能：消息推送、短信通知、WebSocket连接管理
-- ============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS notification_service DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE notification_service;

SET NAMES utf8mb4;

-- ============================================
-- 1. 消息模板表
-- ============================================
DROP TABLE IF EXISTS `message_template`;
CREATE TABLE `message_template` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '模板ID，主键自增',
  `template_code` VARCHAR(50) NOT NULL COMMENT '模板编码，唯一标识',
  `template_name` VARCHAR(100) NOT NULL COMMENT '模板名称',
  `template_type` TINYINT(1) NOT NULL COMMENT '模板类型：1-短信，2-微信，3-APP推送，4-邮件，5-语音',
  `business_type` VARCHAR(50) NOT NULL COMMENT '业务类型：ORDER_CREATE-订单创建，ORDER_STATUS_CHANGE-订单状态变更，QUEUE_CALL-排队叫号，PAYMENT_SUCCESS-支付成功',
  `title` VARCHAR(200) DEFAULT NULL COMMENT '消息标题',
  `content` TEXT NOT NULL COMMENT '消息内容模板，支持变量占位符，如：{order_no}',
  `variables` VARCHAR(500) DEFAULT NULL COMMENT '变量说明，JSON格式，如：["order_no","shop_name"]',
  `is_enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用：0-禁用，1-启用',
  `priority` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '优先级：0-普通，1-重要，2-紧急',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_template_code` (`template_code`),
  KEY `idx_business_type` (`business_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息模板表';

-- 插入示例数据
INSERT INTO `message_template` (`template_code`, `template_name`, `template_type`, `business_type`, `title`, `content`, `variables`, `is_enabled`, `priority`) VALUES
('SMS_ORDER_CREATE', '订单创建短信通知', 1, 'ORDER_CREATE', NULL, '【美味餐厅】您的订单{order_no}已创建，金额{amount}元，请及时支付。', '["order_no","amount"]', 1, 0),
('SMS_PAYMENT_SUCCESS', '支付成功短信通知', 1, 'PAYMENT_SUCCESS', NULL, '【美味餐厅】您的订单{order_no}已支付成功，金额{amount}元，请等待叫号。', '["order_no","amount"]', 1, 0),
('SMS_QUEUE_CALL', '排队叫号短信通知', 1, 'QUEUE_CALL', NULL, '【美味餐厅】您的号码{queue_no}已叫号，请尽快到店用餐。', '["queue_no"]', 1, 1),
('WX_ORDER_STATUS', '订单状态微信通知', 2, 'ORDER_STATUS_CHANGE', '订单状态更新', '您的订单{order_no}状态已变更为{status}，预计等待{wait_time}分钟。', '["order_no","status","wait_time"]', 1, 0),
('WX_QUEUE_CALL', '排队叫号微信通知', 2, 'QUEUE_CALL', '叫号通知', '【{shop_name}】尊敬的顾客，您的号码{queue_no}已开始叫号，请前往用餐区。前方还有{ahead_count}桌。', '["shop_name","queue_no","ahead_count"]', 1, 1),
('PUSH_ORDER_READY', '订单制作完成推送', 3, 'ORDER_READY', '取餐通知', '您的订单{order_no}已制作完成，请到{table_number}取餐。', '["order_no","table_number"]', 1, 0);

-- ============================================
-- 2. 消息发送记录表
-- ============================================
DROP TABLE IF EXISTS `message_send_log`;
CREATE TABLE `message_send_log` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '日志ID，主键自增',
  `message_id` VARCHAR(50) NOT NULL COMMENT '消息ID，唯一标识',
  `template_code` VARCHAR(50) DEFAULT NULL COMMENT '模板编码',
  `recipient_type` TINYINT(1) NOT NULL COMMENT '接收者类型：1-用户，2-店员，3-后厨',
  `recipient_id` BIGINT(20) DEFAULT NULL COMMENT '接收者ID',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `open_id` VARCHAR(100) DEFAULT NULL COMMENT '微信OpenID',
  `device_token` VARCHAR(255) DEFAULT NULL COMMENT '设备Token（APP推送）',
  `message_type` TINYINT(1) NOT NULL COMMENT '消息类型：1-短信，2-微信，3-APP推送，4-邮件，5-语音',
  `business_type` VARCHAR(50) NOT NULL COMMENT '业务类型',
  `business_id` VARCHAR(100) DEFAULT NULL COMMENT '业务ID（订单号、排队号等）',
  `title` VARCHAR(200) DEFAULT NULL COMMENT '消息标题',
  `content` TEXT NOT NULL COMMENT '消息内容',
  `send_status` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '发送状态：0-待发送，1-发送中，2-发送成功，3-发送失败',
  `send_time` DATETIME DEFAULT NULL COMMENT '发送时间',
  `receive_time` DATETIME DEFAULT NULL COMMENT '接收时间',
  `read_time` DATETIME DEFAULT NULL COMMENT '阅读时间',
  `retry_count` INT(11) NOT NULL DEFAULT 0 COMMENT '重试次数',
  `max_retry` INT(11) NOT NULL DEFAULT 3 COMMENT '最大重试次数',
  `error_code` VARCHAR(50) DEFAULT NULL COMMENT '错误码',
  `error_msg` VARCHAR(500) DEFAULT NULL COMMENT '错误信息',
  `channel_response` TEXT COMMENT '渠道返回数据',
  `duration` INT(11) DEFAULT NULL COMMENT '发送耗时（毫秒）',
  `cost` DECIMAL(10, 4) DEFAULT 0.0000 COMMENT '发送成本（元）',
  `extra_data` TEXT COMMENT '扩展数据，JSON格式',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_message_id` (`message_id`),
  KEY `idx_recipient_id` (`recipient_id`),
  KEY `idx_phone` (`phone`),
  KEY `idx_business_type` (`business_type`),
  KEY `idx_business_id` (`business_id`),
  KEY `idx_chat_conversation` (`business_type`, `business_id`, `send_time`),
  KEY `idx_chat_recent` (`business_type`, `recipient_id`, `send_time`),
  KEY `idx_send_status` (`send_status`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息发送记录表';

-- 插入示例数据
INSERT INTO `message_send_log` (`message_id`, `template_code`, `recipient_type`, `recipient_id`, `phone`, `message_type`, `business_type`, `business_id`, `title`, `content`, `send_status`, `send_time`, `retry_count`) VALUES
('MSG2026051700001', 'SMS_ORDER_CREATE', 1, 1001, '13800138001', 1, 'ORDER_CREATE', 'ORD2026051700001', NULL, '【美味餐厅】您的订单ORD2026051700001已创建，金额98.00元，请及时支付。', 2, '2026-05-17 11:25:00', 0),
('MSG2026051700002', 'SMS_PAYMENT_SUCCESS', 1, 1001, '13800138001', 1, 'PAYMENT_SUCCESS', 'ORD2026051700001', NULL, '【美味餐厅】您的订单ORD2026051700001已支付成功，金额98.00元，请等待叫号。', 2, '2026-05-17 11:30:00', 0),
('MSG2026051700003', 'WX_QUEUE_CALL', 1, 1001, NULL, 2, 'QUEUE_CALL', 'A001', '叫号通知', '【美味餐厅旗舰店】尊敬的顾客，您的号码A001已开始叫号，请前往用餐区。', 2, '2026-05-17 11:35:00', 0);

-- ============================================
-- 3. WebSocket连接会话表
-- ============================================
-- ============================================
-- 3. Chat message table
-- ============================================
DROP TABLE IF EXISTS `chat_message_recipient`;
DROP TABLE IF EXISTS `chat_message`;
CREATE TABLE `chat_message` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `message_id` VARCHAR(64) NOT NULL COMMENT 'message id',
  `sender_id` BIGINT NOT NULL COMMENT 'sender user id',
  `sender_name` VARCHAR(100) DEFAULT NULL COMMENT 'sender display name',
  `sender_role` VARCHAR(50) DEFAULT NULL COMMENT 'sender role',
  `shop_id` BIGINT DEFAULT NULL COMMENT 'shop id',
  `content` TEXT NOT NULL COMMENT 'message content',
  `delivered` TINYINT NOT NULL DEFAULT 0 COMMENT 'delivered to at least one recipient',
  `send_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'send time',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'created time',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'updated time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_chat_message_id` (`message_id`),
  KEY `idx_chat_sender_time` (`sender_id`, `send_time`),
  KEY `idx_chat_shop_time` (`shop_id`, `send_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='chat message';

CREATE TABLE `chat_message_recipient` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `message_id` VARCHAR(64) NOT NULL COMMENT 'message id',
  `recipient_id` BIGINT NOT NULL COMMENT 'recipient user id',
  `delivered` TINYINT NOT NULL DEFAULT 0 COMMENT 'realtime delivered',
  `delivered_time` DATETIME DEFAULT NULL COMMENT 'delivered time',
  `read_status` TINYINT NOT NULL DEFAULT 0 COMMENT '0-unread, 1-read',
  `read_time` DATETIME DEFAULT NULL COMMENT 'read time',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'created time',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'updated time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_chat_message_recipient` (`message_id`, `recipient_id`),
  KEY `idx_chat_recipient_time` (`recipient_id`, `created_at`),
  KEY `idx_chat_message_id` (`message_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='chat message recipient';

DROP TABLE IF EXISTS `websocket_session`;
CREATE TABLE `websocket_session` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '会话ID，主键自增',
  `session_id` VARCHAR(100) NOT NULL COMMENT 'WebSocket会话ID，唯一标识',
  `user_id` BIGINT(20) DEFAULT NULL COMMENT '用户ID',
  `user_type` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '用户类型：1-顾客，2-店员，3-后厨，4-管理员',
  `shop_id` BIGINT(20) DEFAULT NULL COMMENT '店铺ID',
  `device_type` VARCHAR(20) DEFAULT NULL COMMENT '设备类型：WEB、IOS、ANDROID',
  `ip_address` VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
  `user_agent` VARCHAR(500) DEFAULT NULL COMMENT '用户代理',
  `connect_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '连接时间',
  `disconnect_time` DATETIME DEFAULT NULL COMMENT '断开时间',
  `last_heartbeat_time` DATETIME DEFAULT NULL COMMENT '最后心跳时间',
  `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态：0-已断开，1-在线',
  `subscribe_channels` TEXT COMMENT '订阅频道列表，JSON数组格式',
  `extra_data` TEXT COMMENT '扩展数据，JSON格式',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_session_id` (`session_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_shop_id` (`shop_id`),
  KEY `idx_status` (`status`),
  KEY `idx_connect_time` (`connect_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='WebSocket连接会话表';

-- ============================================
-- 4. 通知配置表
-- ============================================
DROP TABLE IF EXISTS `notification_config`;
CREATE TABLE `notification_config` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '配置ID，主键自增',
  `shop_id` BIGINT(20) NOT NULL COMMENT '店铺ID，0表示全局配置',
  `config_key` VARCHAR(100) NOT NULL COMMENT '配置键名',
  `config_value` TEXT COMMENT '配置值',
  `config_desc` VARCHAR(255) DEFAULT NULL COMMENT '配置说明',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_shop_config` (`shop_id`, `config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知配置表';

-- 插入示例配置数据
INSERT INTO `notification_config` (`shop_id`, `config_key`, `config_value`, `config_desc`) VALUES
(0, 'sms_provider', 'aliyun', '短信服务商：aliyun-阿里云，tencent-腾讯云'),
(0, 'sms_access_key', 'encrypted_key_here', '短信AccessKey（加密存储）'),
(0, 'sms_secret_key', 'encrypted_secret_here', '短信SecretKey（加密存储）'),
(0, 'sms_sign_name', '美味餐厅', '短信签名'),
(0, 'wechat_app_id', 'wx_app_id_123456', '微信公众号AppID'),
(0, 'wechat_app_secret', 'encrypted_secret_here', '微信公众号AppSecret（加密存储）'),
(0, 'push_provider', 'jiguang', '推送服务商：jiguang-极光，getui-个推'),
(0, 'push_api_key', 'encrypted_key_here', '推送API Key（加密存储）'),
(1, 'sms_enabled', 'true', '是否启用短信通知'),
(1, 'wechat_enabled', 'true', '是否启用微信通知'),
(1, 'push_enabled', 'true', '是否启用APP推送'),
(1, 'voice_enabled', 'false', '是否启用语音通知'),
(1, 'order_create_notify', 'sms,wechat', '订单创建通知方式'),
(1, 'payment_success_notify', 'sms,wechat,push', '支付成功通知方式'),
(1, 'queue_call_notify', 'sms,wechat,voice', '排队叫号通知方式');

-- ============================================
-- 5. 消息统计表
-- ============================================
DROP TABLE IF EXISTS `message_statistics`;
CREATE TABLE `message_statistics` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '统计ID，主键自增',
  `shop_id` BIGINT(20) NOT NULL COMMENT '店铺ID，0表示全局',
  `stat_date` DATE NOT NULL COMMENT '统计日期',
  `message_type` TINYINT(1) NOT NULL COMMENT '消息类型：1-短信，2-微信，3-APP推送，4-邮件，5-语音',
  `business_type` VARCHAR(50) NOT NULL DEFAULT 'ALL' COMMENT '业务类型，ALL表示全部',
  `total_sent` INT(11) NOT NULL DEFAULT 0 COMMENT '总发送数',
  `success_count` INT(11) NOT NULL DEFAULT 0 COMMENT '成功数',
  `failed_count` INT(11) NOT NULL DEFAULT 0 COMMENT '失败数',
  `pending_count` INT(11) NOT NULL DEFAULT 0 COMMENT '待发送数',
  `success_rate` DECIMAL(5, 2) DEFAULT 0.00 COMMENT '成功率（%）',
  `total_cost` DECIMAL(10, 4) DEFAULT 0.0000 COMMENT '总成本（元）',
  `avg_duration` INT(11) DEFAULT NULL COMMENT '平均耗时（毫秒）',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_shop_date_type` (`shop_id`, `stat_date`, `message_type`, `business_type`),
  KEY `idx_stat_date` (`stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息统计表';

-- 插入示例统计数据
INSERT INTO `message_statistics` (`shop_id`, `stat_date`, `message_type`, `business_type`, `total_sent`, `success_count`, `failed_count`, `pending_count`, `success_rate`, `total_cost`) VALUES
(0, '2026-05-16', 1, 'ALL', 256, 254, 2, 0, 99.22, 12.80),
(0, '2026-05-16', 2, 'ALL', 189, 188, 1, 0, 99.47, 0.00),
(1, '2026-05-17', 1, 'ORDER_CREATE', 45, 45, 0, 0, 100.00, 2.25),
(1, '2026-05-17', 1, 'PAYMENT_SUCCESS', 38, 38, 0, 0, 100.00, 1.90);
