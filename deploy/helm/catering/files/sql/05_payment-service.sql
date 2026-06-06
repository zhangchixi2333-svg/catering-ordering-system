-- ============================================
-- 支付服务数据库 (payment-service)
-- 功能：支付记录管理、支付流水追踪
-- ============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS payment_service DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE payment_service;

SET NAMES utf8mb4;

-- ============================================
-- 1. 支付订单表
-- ============================================
DROP TABLE IF EXISTS `payment_order`;
CREATE TABLE `payment_order` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '支付ID，主键自增',
  `payment_no` VARCHAR(50) NOT NULL COMMENT '支付单号，唯一标识，格式：PAY+年月日+流水号',
  `order_no` VARCHAR(50) NOT NULL COMMENT '关联订单编号',
  `order_id` BIGINT(20) NOT NULL COMMENT '关联订单ID',
  `shop_id` BIGINT(20) NOT NULL COMMENT '店铺ID，关联shop_info.id',
  `user_id` BIGINT(20) DEFAULT NULL COMMENT '用户ID',
  `payment_amount` DECIMAL(10, 2) NOT NULL COMMENT '支付金额（元）',
  `payment_method` TINYINT(1) NOT NULL COMMENT '支付方式：1-微信支付，2-支付宝，3-现金，4-会员卡，5-银行卡',
  `payment_status` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '支付状态：0-待支付，1-支付中，2-支付成功，3-支付失败，4-已退款，5-退款中',
  `currency` VARCHAR(10) NOT NULL DEFAULT 'CNY' COMMENT '货币类型，默认CNY人民币',
  `subject` VARCHAR(255) DEFAULT NULL COMMENT '支付主题，如：美味餐厅订单支付',
  `body` VARCHAR(500) DEFAULT NULL COMMENT '支付描述',
  `transaction_id` VARCHAR(100) DEFAULT NULL COMMENT '第三方支付交易号',
  `channel_order_no` VARCHAR(100) DEFAULT NULL COMMENT '渠道订单号',
  `pay_time` DATETIME DEFAULT NULL COMMENT '支付成功时间',
  `expire_time` DATETIME DEFAULT NULL COMMENT '支付过期时间',
  `refund_amount` DECIMAL(10, 2) DEFAULT 0.00 COMMENT '退款金额（元）',
  `refund_time` DATETIME DEFAULT NULL COMMENT '退款时间',
  `refund_reason` VARCHAR(255) DEFAULT NULL COMMENT '退款原因',
  `client_ip` VARCHAR(50) DEFAULT NULL COMMENT '客户端IP地址',
  `device_info` VARCHAR(255) DEFAULT NULL COMMENT '设备信息',
  `notify_url` VARCHAR(255) DEFAULT NULL COMMENT '异步通知地址',
  `return_url` VARCHAR(255) DEFAULT NULL COMMENT '同步返回地址',
  `extra_params` TEXT COMMENT '扩展参数，JSON格式',
  `error_code` VARCHAR(50) DEFAULT NULL COMMENT '错误码',
  `error_msg` VARCHAR(500) DEFAULT NULL COMMENT '错误信息',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_payment_no` (`payment_no`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_shop_id` (`shop_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_payment_status` (`payment_status`),
  KEY `idx_transaction_id` (`transaction_id`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付订单表';

-- 插入示例数据
INSERT INTO `payment_order` (`payment_no`, `order_no`, `order_id`, `shop_id`, `user_id`, `payment_amount`, `payment_method`, `payment_status`, `subject`, `body`, `transaction_id`, `pay_time`, `expire_time`) VALUES
('PAY2026051700001', 'ORD2026051700001', 1, 1, 1001, 98.00, 1, 2, '美味餐厅订单支付', '宫保鸡丁等3件商品', 'WX20260517113000123456', '2026-05-17 11:30:00', '2026-05-17 11:45:00'),
('PAY2026051700002', 'ORD2026051700002', 2, 1, 1002, 120.00, 2, 2, '美味餐厅订单支付', '红烧肉等4件商品', 'ALI20260517113500789012', '2026-05-17 11:35:00', '2026-05-17 11:50:00'),
('PAY2026051700003', 'ORD2026051700003', 3, 1, 1003, 68.00, 1, 0, '美味餐厅订单支付', '麻婆豆腐等2件商品', NULL, NULL, '2026-05-17 11:55:00');

-- ============================================
-- 2. 支付流水表
-- ============================================
DROP TABLE IF EXISTS `payment_transaction`;
CREATE TABLE `payment_transaction` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '流水ID，主键自增',
  `transaction_no` VARCHAR(50) NOT NULL COMMENT '流水号，唯一标识',
  `payment_no` VARCHAR(50) NOT NULL COMMENT '支付单号，关联payment_order.payment_no',
  `order_no` VARCHAR(50) NOT NULL COMMENT '订单编号',
  `shop_id` BIGINT(20) NOT NULL COMMENT '店铺ID',
  `user_id` BIGINT(20) DEFAULT NULL COMMENT '用户ID',
  `transaction_type` TINYINT(1) NOT NULL COMMENT '交易类型：1-支付，2-退款，3-转账',
  `amount` DECIMAL(10, 2) NOT NULL COMMENT '交易金额（元）',
  `payment_method` TINYINT(1) NOT NULL COMMENT '支付方式：1-微信支付，2-支付宝，3-现金，4-会员卡，5-银行卡',
  `status` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '交易状态：0-处理中，1-成功，2-失败',
  `request_data` TEXT COMMENT '请求数据，JSON格式',
  `response_data` TEXT COMMENT '响应数据，JSON格式',
  `third_party_response` TEXT COMMENT '第三方返回原始数据',
  `error_code` VARCHAR(50) DEFAULT NULL COMMENT '错误码',
  `error_msg` VARCHAR(500) DEFAULT NULL COMMENT '错误信息',
  `duration` INT(11) DEFAULT NULL COMMENT '交易耗时（毫秒）',
  `ip_address` VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
  `user_agent` VARCHAR(500) DEFAULT NULL COMMENT '用户代理',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_transaction_no` (`transaction_no`),
  KEY `idx_payment_no` (`payment_no`),
  KEY `idx_order_no` (`order_no`),
  KEY `idx_shop_id` (`shop_id`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付流水表';

-- 插入示例数据
INSERT INTO `payment_transaction` (`transaction_no`, `payment_no`, `order_no`, `shop_id`, `user_id`, `transaction_type`, `amount`, `payment_method`, `status`, `duration`, `remark`) VALUES
('TXN2026051700001', 'PAY2026051700001', 'ORD2026051700001', 1, 1001, 1, 98.00, 1, 1, 1250, '微信支付成功'),
('TXN2026051700002', 'PAY2026051700002', 'ORD2026051700002', 1, 1002, 1, 120.00, 2, 1, 980, '支付宝支付成功');

-- ============================================
-- 3. 退款记录表
-- ============================================
DROP TABLE IF EXISTS `refund_record`;
CREATE TABLE `refund_record` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '退款ID，主键自增',
  `refund_no` VARCHAR(50) NOT NULL COMMENT '退款单号，唯一标识，格式：REF+年月日+流水号',
  `payment_no` VARCHAR(50) NOT NULL COMMENT '原支付单号',
  `order_no` VARCHAR(50) NOT NULL COMMENT '订单编号',
  `order_id` BIGINT(20) NOT NULL COMMENT '订单ID',
  `shop_id` BIGINT(20) NOT NULL COMMENT '店铺ID',
  `user_id` BIGINT(20) DEFAULT NULL COMMENT '用户ID',
  `refund_amount` DECIMAL(10, 2) NOT NULL COMMENT '退款金额（元）',
  `refund_reason` VARCHAR(500) NOT NULL COMMENT '退款原因',
  `refund_type` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '退款类型：1-全额退款，2-部分退款',
  `refund_status` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '退款状态：0-申请中，1-审核中，2-退款中，3-退款成功，4-退款失败，5-已关闭',
  `payment_method` TINYINT(1) NOT NULL COMMENT '原支付方式',
  `refund_method` TINYINT(1) DEFAULT NULL COMMENT '退款方式：1-原路返回，2-余额，3-线下退款',
  `transaction_id` VARCHAR(100) DEFAULT NULL COMMENT '第三方退款交易号',
  `apply_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  `approve_time` DATETIME DEFAULT NULL COMMENT '审核时间',
  `approve_by` BIGINT(20) DEFAULT NULL COMMENT '审核人ID',
  `approve_remark` VARCHAR(255) DEFAULT NULL COMMENT '审核备注',
  `refund_time` DATETIME DEFAULT NULL COMMENT '退款成功时间',
  `failure_reason` VARCHAR(500) DEFAULT NULL COMMENT '失败原因',
  `evidence_images` TEXT COMMENT '凭证图片URL，多个用逗号分隔',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_refund_no` (`refund_no`),
  KEY `idx_payment_no` (`payment_no`),
  KEY `idx_order_no` (`order_no`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_shop_id` (`shop_id`),
  KEY `idx_refund_status` (`refund_status`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='退款记录表';

-- ============================================
-- 4. 支付配置表
-- ============================================
DROP TABLE IF EXISTS `payment_config`;
CREATE TABLE `payment_config` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '配置ID，主键自增',
  `shop_id` BIGINT(20) NOT NULL COMMENT '店铺ID，0表示全局配置',
  `payment_method` TINYINT(1) NOT NULL COMMENT '支付方式：1-微信支付，2-支付宝，3-现金，4-会员卡，5-银行卡',
  `is_enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用：0-禁用，1-启用',
  `app_id` VARCHAR(100) DEFAULT NULL COMMENT '应用ID',
  `merchant_id` VARCHAR(100) DEFAULT NULL COMMENT '商户号',
  `api_key` VARCHAR(255) DEFAULT NULL COMMENT 'API密钥（加密存储）',
  `private_key` TEXT COMMENT '私钥（加密存储）',
  `public_key` TEXT COMMENT '公钥',
  `notify_url` VARCHAR(255) DEFAULT NULL COMMENT '异步通知地址',
  `return_url` VARCHAR(255) DEFAULT NULL COMMENT '同步返回地址',
  `min_amount` DECIMAL(10, 2) DEFAULT 0.01 COMMENT '最小支付金额（元）',
  `max_amount` DECIMAL(10, 2) DEFAULT 50000.00 COMMENT '最大支付金额（元）',
  `timeout_minutes` INT(11) DEFAULT 15 COMMENT '支付超时时间（分钟）',
  `auto_refund` TINYINT(1) DEFAULT 0 COMMENT '是否自动退款：0-否，1-是',
  `extra_config` TEXT COMMENT '额外配置，JSON格式',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_shop_payment` (`shop_id`, `payment_method`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付配置表';

-- 插入示例配置数据
INSERT INTO `payment_config` (`shop_id`, `payment_method`, `is_enabled`, `app_id`, `merchant_id`, `notify_url`, `min_amount`, `max_amount`, `timeout_minutes`) VALUES
(0, 1, 1, 'wx_app_id_123456', '1234567890', 'https://api.example.com/payment/wechat/notify', 0.01, 50000.00, 15),
(0, 2, 1, 'ali_app_id_789012', '0987654321', 'https://api.example.com/payment/alipay/notify', 0.01, 50000.00, 15),
(1, 1, 1, 'wx_shop1_app_id', 'shop1_merchant_id', 'https://api.example.com/payment/wechat/notify/shop1', 0.01, 10000.00, 15);

-- ============================================
-- 5. 支付回调日志表
-- ============================================
DROP TABLE IF EXISTS `payment_callback_log`;
CREATE TABLE `payment_callback_log` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '日志ID，主键自增',
  `payment_no` VARCHAR(50) DEFAULT NULL COMMENT '支付单号',
  `transaction_id` VARCHAR(100) DEFAULT NULL COMMENT '第三方交易号',
  `callback_type` TINYINT(1) NOT NULL COMMENT '回调类型：1-支付回调，2-退款回调',
  `request_data` TEXT NOT NULL COMMENT '回调请求数据',
  `response_data` TEXT COMMENT '响应数据',
  `signature_valid` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '签名验证：0-失败，1-成功',
  `process_status` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '处理状态：0-待处理，1-处理成功，2-处理失败',
  `error_msg` VARCHAR(500) DEFAULT NULL COMMENT '错误信息',
  `ip_address` VARCHAR(50) DEFAULT NULL COMMENT '回调IP地址',
  `retry_count` INT(11) NOT NULL DEFAULT 0 COMMENT '重试次数',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_payment_no` (`payment_no`),
  KEY `idx_transaction_id` (`transaction_id`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付回调日志表';
