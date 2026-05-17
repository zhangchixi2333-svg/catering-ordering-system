-- ============================================
-- 订单服务数据库 (order-service)
-- 功能：订单生命周期管理（待接单→制作中→待取餐→已完成）
-- ============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS order_service DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE order_service;

-- ============================================
-- 1. 订单主表
-- ============================================
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '订单ID，主键自增',
  `order_no` VARCHAR(50) NOT NULL COMMENT '订单编号，唯一标识，格式：ORD+年月日+流水号',
  `shop_id` BIGINT(20) NOT NULL COMMENT '店铺ID，关联shop_info.id',
  `table_id` BIGINT(20) DEFAULT NULL COMMENT '桌台ID，关联table_info.id',
  `table_number` VARCHAR(20) DEFAULT NULL COMMENT '桌台编号，冗余字段便于查询',
  `user_id` BIGINT(20) DEFAULT NULL COMMENT '用户ID',
  `order_type` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '订单类型：1-堂食，2-外带，3-外卖',
  `order_status` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '订单状态：0-待支付，1-待接单，2-制作中，3-待取餐，4-已完成，5-已取消，6-退款中，7-已退款',
  `total_amount` DECIMAL(10, 2) NOT NULL COMMENT '订单总金额（元）',
  `discount_amount` DECIMAL(10, 2) DEFAULT 0.00 COMMENT '优惠金额（元）',
  `actual_amount` DECIMAL(10, 2) NOT NULL COMMENT '实付金额（元）',
  `item_count` INT(11) NOT NULL DEFAULT 0 COMMENT '菜品总数量',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '订单备注，如：少辣、不要香菜',
  `payment_method` TINYINT(1) DEFAULT NULL COMMENT '支付方式：1-微信支付，2-支付宝，3-现金，4-会员卡',
  `payment_status` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '支付状态：0-未支付，1-已支付，2-支付失败，3-退款中，4-已退款',
  `payment_time` DATETIME DEFAULT NULL COMMENT '支付时间',
  `accept_time` DATETIME DEFAULT NULL COMMENT '接单时间',
  `prepare_time` DATETIME DEFAULT NULL COMMENT '开始制作时间',
  `ready_time` DATETIME DEFAULT NULL COMMENT '制作完成时间',
  `complete_time` DATETIME DEFAULT NULL COMMENT '订单完成时间',
  `cancel_time` DATETIME DEFAULT NULL COMMENT '取消时间',
  `cancel_reason` VARCHAR(255) DEFAULT NULL COMMENT '取消原因',
  `queue_number` VARCHAR(20) DEFAULT NULL COMMENT '排队号码，如：A001',
  `estimated_time` INT(11) DEFAULT NULL COMMENT '预计等待时间（分钟）',
  `priority` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '优先级：0-普通，1-加急',
  `is_evaluated` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已评价：0-否，1-是',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_shop_id` (`shop_id`),
  KEY `idx_table_id` (`table_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_order_status` (`order_status`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单主表';

-- 插入示例数据
INSERT INTO `orders` (`order_no`, `shop_id`, `table_id`, `table_number`, `user_id`, `order_type`, `order_status`, `total_amount`, `discount_amount`, `actual_amount`, `item_count`, `remark`, `payment_method`, `payment_status`, `payment_time`, `accept_time`, `prepare_time`, `ready_time`, `complete_time`, `queue_number`, `estimated_time`) VALUES
('ORD2026051700001', 1, 1, 'A01', 1001, 1, 4, 98.00, 0.00, 98.00, 3, '少辣', 1, 1, '2026-05-17 11:30:00', '2026-05-17 11:31:00', '2026-05-17 11:32:00', '2026-05-17 11:45:00', '2026-05-17 11:50:00', 'A001', 15),
('ORD2026051700002', 1, 2, 'A02', 1002, 1, 2, 125.00, 5.00, 120.00, 4, '不要香菜', 1, 1, '2026-05-17 11:35:00', '2026-05-17 11:36:00', '2026-05-17 11:37:00', NULL, NULL, 'A002', 18),
('ORD2026051700003', 1, 3, 'A03', 1003, 1, 1, 68.00, 0.00, 68.00, 2, NULL, 2, 1, '2026-05-17 11:40:00', NULL, NULL, NULL, NULL, 'A003', 12);

-- ============================================
-- 2. 订单明细表
-- ============================================
DROP TABLE IF EXISTS `order_item`;
CREATE TABLE `order_item` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '明细ID，主键自增',
  `order_id` BIGINT(20) NOT NULL COMMENT '订单ID，关联orders.id',
  `order_no` VARCHAR(50) NOT NULL COMMENT '订单编号，冗余字段',
  `item_id` BIGINT(20) NOT NULL COMMENT '菜品ID，关联menu_item.id',
  `item_name` VARCHAR(100) NOT NULL COMMENT '菜品名称，冗余字段',
  `item_code` VARCHAR(50) DEFAULT NULL COMMENT '菜品编码，冗余字段',
  `image_url` VARCHAR(255) DEFAULT NULL COMMENT '菜品图片URL，冗余字段',
  `price` DECIMAL(10, 2) NOT NULL COMMENT '菜品单价（元）',
  `quantity` INT(11) NOT NULL DEFAULT 1 COMMENT '购买数量',
  `subtotal` DECIMAL(10, 2) NOT NULL COMMENT '小计金额（元）= price * quantity',
  `specification` VARCHAR(255) DEFAULT NULL COMMENT '规格信息，JSON格式，如：{"size":"大份","spicy":"微辣"}',
  `toppings` VARCHAR(500) DEFAULT NULL COMMENT '配料信息，JSON格式，如：[{"name":"加蛋","price":3},{"name":"加火腿","price":5}]',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '单项备注',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_order_no` (`order_no`),
  KEY `idx_item_id` (`item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细表';

-- 插入示例数据（对应订单ORD2026051700001）
INSERT INTO `order_item` (`order_id`, `order_no`, `item_id`, `item_name`, `item_code`, `price`, `quantity`, `subtotal`, `specification`, `toppings`, `remark`) VALUES
(1, 'ORD2026051700001', 1, '宫保鸡丁', 'ITEM001', 38.00, 1, 38.00, '{"size":"标准份","spicy":"微辣"}', NULL, '少辣'),
(1, 'ORD2026051700001', 3, '扬州炒饭', 'ITEM003', 32.00, 1, 32.00, NULL, '[{"name":"加蛋","price":3}]', NULL),
(1, 'ORD2026051700001', 11, '酸梅汤', 'ITEM011', 15.00, 2, 30.00, '{"temperature":"加冰"}', NULL, NULL);

-- 插入示例数据（对应订单ORD2026051700002）
INSERT INTO `order_item` (`order_id`, `order_no`, `item_id`, `item_name`, `item_code`, `price`, `quantity`, `subtotal`, `specification`, `toppings`, `remark`) VALUES
(2, 'ORD2026051700002', 5, '红烧肉', 'ITEM005', 58.00, 1, 58.00, NULL, NULL, NULL),
(2, 'ORD2026051700002', 8, '口水鸡', 'ITEM008', 35.00, 1, 35.00, '{"spicy":"中辣"}', NULL, '不要香菜'),
(2, 'ORD2026051700002', 9, '酸辣汤', 'ITEM009', 18.00, 2, 36.00, NULL, NULL, NULL),
(2, 'ORD2026051700002', 13, '红豆沙', 'ITEM013', 16.00, 1, 16.00, NULL, NULL, NULL);

-- ============================================
-- 3. 订单状态流转记录表
-- ============================================
DROP TABLE IF EXISTS `order_status_log`;
CREATE TABLE `order_status_log` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '记录ID，主键自增',
  `order_id` BIGINT(20) NOT NULL COMMENT '订单ID，关联orders.id',
  `order_no` VARCHAR(50) NOT NULL COMMENT '订单编号，冗余字段',
  `old_status` TINYINT(1) DEFAULT NULL COMMENT '原状态',
  `new_status` TINYINT(1) NOT NULL COMMENT '新状态',
  `operator_type` TINYINT(1) NOT NULL COMMENT '操作人类型：1-系统，2-用户，3-店员，4-后厨',
  `operator_id` BIGINT(20) DEFAULT NULL COMMENT '操作人ID',
  `operator_name` VARCHAR(50) DEFAULT NULL COMMENT '操作人姓名',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '状态变更说明',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_order_no` (`order_no`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单状态流转记录表';

-- 插入示例数据（订单ORD2026051700001的状态流转）
INSERT INTO `order_status_log` (`order_id`, `order_no`, `old_status`, `new_status`, `operator_type`, `operator_id`, `operator_name`, `remark`) VALUES
(1, 'ORD2026051700001', NULL, 0, 2, 1001, '用户1001', '创建订单'),
(1, 'ORD2026051700001', 0, 1, 2, 1001, '用户1001', '完成支付'),
(1, 'ORD2026051700001', 1, 2, 4, 2001, '后厨张三', '开始制作'),
(1, 'ORD2026051700001', 2, 3, 4, 2001, '后厨张三', '制作完成'),
(1, 'ORD2026051700001', 3, 4, 3, 3001, '服务员李四', '订单完成');

-- ============================================
-- 4. 订单操作日志表
-- ============================================
DROP TABLE IF EXISTS `order_operation_log`;
CREATE TABLE `order_operation_log` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '日志ID，主键自增',
  `order_id` BIGINT(20) NOT NULL COMMENT '订单ID，关联orders.id',
  `order_no` VARCHAR(50) NOT NULL COMMENT '订单编号，冗余字段',
  `operation_type` VARCHAR(50) NOT NULL COMMENT '操作类型，如：CREATE_ORDER、PAY_ORDER、ACCEPT_ORDER、CANCEL_ORDER',
  `operation_desc` VARCHAR(255) DEFAULT NULL COMMENT '操作描述',
  `request_data` TEXT COMMENT '请求数据，JSON格式',
  `response_data` TEXT COMMENT '响应数据，JSON格式',
  `ip_address` VARCHAR(50) DEFAULT NULL COMMENT '操作IP地址',
  `user_agent` VARCHAR(500) DEFAULT NULL COMMENT '用户代理信息',
  `operator_id` BIGINT(20) DEFAULT NULL COMMENT '操作人ID',
  `operator_name` VARCHAR(50) DEFAULT NULL COMMENT '操作人姓名',
  `duration` INT(11) DEFAULT NULL COMMENT '操作耗时（毫秒）',
  `result` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '操作结果：0-失败，1-成功',
  `error_msg` TEXT COMMENT '错误信息',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_order_no` (`order_no`),
  KEY `idx_operation_type` (`operation_type`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单操作日志表';

-- ============================================
-- 5. 订单评价表
-- ============================================
DROP TABLE IF EXISTS `order_evaluation`;
CREATE TABLE `order_evaluation` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '评价ID，主键自增',
  `order_id` BIGINT(20) NOT NULL COMMENT '订单ID，关联orders.id',
  `order_no` VARCHAR(50) NOT NULL COMMENT '订单编号，冗余字段',
  `shop_id` BIGINT(20) NOT NULL COMMENT '店铺ID，关联shop_info.id',
  `user_id` BIGINT(20) DEFAULT NULL COMMENT '用户ID',
  `overall_rating` TINYINT(1) NOT NULL COMMENT '总体评分，1-5星',
  `taste_rating` TINYINT(1) DEFAULT NULL COMMENT '口味评分，1-5星',
  `service_rating` TINYINT(1) DEFAULT NULL COMMENT '服务评分，1-5星',
  `environment_rating` TINYINT(1) DEFAULT NULL COMMENT '环境评分，1-5星',
  `comment` TEXT COMMENT '评价内容',
  `images` TEXT COMMENT '评价图片URL，多个用逗号分隔',
  `is_anonymous` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否匿名：0-否，1-是',
  `reply_content` TEXT COMMENT '商家回复内容',
  `reply_time` DATETIME DEFAULT NULL COMMENT '商家回复时间',
  `like_count` INT(11) DEFAULT 0 COMMENT '点赞数',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_id` (`order_id`),
  KEY `idx_shop_id` (`shop_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单评价表';

-- ============================================
-- 6. 订单统计表（用于快速统计）
-- ============================================
DROP TABLE IF EXISTS `order_statistics`;
CREATE TABLE `order_statistics` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '统计ID，主键自增',
  `shop_id` BIGINT(20) NOT NULL COMMENT '店铺ID，关联shop_info.id',
  `stat_date` DATE NOT NULL COMMENT '统计日期',
  `total_orders` INT(11) NOT NULL DEFAULT 0 COMMENT '总订单数',
  `completed_orders` INT(11) NOT NULL DEFAULT 0 COMMENT '完成订单数',
  `cancelled_orders` INT(11) NOT NULL DEFAULT 0 COMMENT '取消订单数',
  `total_amount` DECIMAL(12, 2) NOT NULL DEFAULT 0.00 COMMENT '总营业额（元）',
  `avg_order_amount` DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '平均订单金额（元）',
  `total_items_sold` INT(11) NOT NULL DEFAULT 0 COMMENT '总售出菜品数',
  `peak_hour` VARCHAR(20) DEFAULT NULL COMMENT '高峰时段，如：12:00-13:00',
  `avg_prepare_time` INT(11) DEFAULT NULL COMMENT '平均制作时长（分钟）',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_shop_date` (`shop_id`, `stat_date`),
  KEY `idx_stat_date` (`stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单统计表';

-- 插入示例统计数据
INSERT INTO `order_statistics` (`shop_id`, `stat_date`, `total_orders`, `completed_orders`, `cancelled_orders`, `total_amount`, `avg_order_amount`, `total_items_sold`, `peak_hour`, `avg_prepare_time`) VALUES
(1, '2026-05-16', 156, 148, 8, 15680.00, 100.51, 523, '12:00-13:00', 16),
(1, '2026-05-17', 89, 75, 5, 8920.00, 100.22, 298, '11:30-12:30', 15);
