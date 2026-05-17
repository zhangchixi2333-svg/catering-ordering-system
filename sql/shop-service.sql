-- ============================================
-- 店铺服务数据库 (shop-service)
-- 功能：店铺信息管理、桌号管理
-- ============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS shop_service DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE shop_service;

-- ============================================
-- 1. 店铺信息表
-- ============================================
DROP TABLE IF EXISTS `shop_info`;
CREATE TABLE `shop_info` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '店铺ID，主键自增',
  `shop_name` VARCHAR(100) NOT NULL COMMENT '店铺名称',
  `shop_code` VARCHAR(50) NOT NULL COMMENT '店铺编码，唯一标识',
  `address` VARCHAR(255) DEFAULT NULL COMMENT '店铺详细地址',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '店铺联系电话',
  `business_hours` VARCHAR(100) DEFAULT NULL COMMENT '营业时间，格式：09:00-22:00',
  `shop_status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '店铺状态：0-停业，1-营业，2-装修中',
  `capacity` INT(11) DEFAULT 0 COMMENT '店铺总座位数',
  `description` TEXT COMMENT '店铺描述信息',
  `logo_url` VARCHAR(255) DEFAULT NULL COMMENT '店铺Logo图片URL',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_shop_code` (`shop_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='店铺信息表';

-- 插入示例数据
INSERT INTO `shop_info` (`shop_name`, `shop_code`, `address`, `phone`, `business_hours`, `shop_status`, `capacity`, `description`) VALUES
('美味餐厅旗舰店', 'SHOP001', '北京市朝阳区建国路88号', '010-12345678', '09:00-22:00', 1, 120, '专业中式餐饮连锁品牌'),
('美味餐厅万达店', 'SHOP002', '北京市海淀区中关村大街1号', '010-87654321', '10:00-21:00', 1, 80, '购物中心内特色餐厅');

-- ============================================
-- 2. 桌台信息表
-- ============================================
DROP TABLE IF EXISTS `table_info`;
CREATE TABLE `table_info` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '桌台ID，主键自增',
  `shop_id` BIGINT(20) NOT NULL COMMENT '所属店铺ID，关联shop_info.id',
  `table_number` VARCHAR(20) NOT NULL COMMENT '桌台编号，如：A01、B02',
  `table_name` VARCHAR(50) DEFAULT NULL COMMENT '桌台名称，如：一号桌、VIP包厢',
  `seats` INT(11) NOT NULL DEFAULT 4 COMMENT '座位数量',
  `table_type` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '桌台类型：1-普通桌，2-卡座，3-包厢，4-吧台',
  `table_status` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '桌台状态：0-空闲，1-已占用，2-预订，3-清洁中',
  `qr_code` VARCHAR(255) DEFAULT NULL COMMENT '扫码点餐二维码URL',
  `location` VARCHAR(50) DEFAULT NULL COMMENT '位置描述，如：一楼大厅、二楼东侧',
  `is_available` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否可用：0-不可用，1-可用',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_shop_table` (`shop_id`, `table_number`),
  KEY `idx_shop_id` (`shop_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='桌台信息表';

-- 插入示例数据（SHOP001店铺的桌台）
INSERT INTO `table_info` (`shop_id`, `table_number`, `table_name`, `seats`, `table_type`, `table_status`, `location`, `is_available`) VALUES
(1, 'A01', '一号桌', 4, 1, 0, '一楼大厅', 1),
(1, 'A02', '二号桌', 4, 1, 0, '一楼大厅', 1),
(1, 'A03', '三号桌', 6, 1, 0, '一楼大厅', 1),
(1, 'B01', '卡座1号', 4, 2, 0, '一楼窗边', 1),
(1, 'B02', '卡座2号', 4, 2, 0, '一楼窗边', 1),
(1, 'V01', 'VIP包厢1', 10, 3, 0, '二楼', 1),
(1, 'V02', 'VIP包厢2', 8, 3, 0, '二楼', 1);

-- 插入示例数据（SHOP002店铺的桌台）
INSERT INTO `table_info` (`shop_id`, `table_number`, `table_name`, `seats`, `table_type`, `table_status`, `location`, `is_available`) VALUES
(2, 'A01', '一号桌', 4, 1, 0, '大厅区域', 1),
(2, 'A02', '二号桌', 4, 1, 0, '大厅区域', 1),
(2, 'B01', '卡座1号', 4, 2, 0, '靠窗位置', 1);

-- ============================================
-- 3. 桌台使用记录表
-- ============================================
DROP TABLE IF EXISTS `table_usage_log`;
CREATE TABLE `table_usage_log` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '记录ID，主键自增',
  `table_id` BIGINT(20) NOT NULL COMMENT '桌台ID，关联table_info.id',
  `shop_id` BIGINT(20) NOT NULL COMMENT '店铺ID，关联shop_info.id',
  `order_id` BIGINT(20) DEFAULT NULL COMMENT '关联订单ID',
  `start_time` DATETIME NOT NULL COMMENT '开始使用时间',
  `end_time` DATETIME DEFAULT NULL COMMENT '结束使用时间',
  `party_size` INT(11) DEFAULT NULL COMMENT '用餐人数',
  `usage_status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '使用状态：1-使用中，2-已结束',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_table_id` (`table_id`),
  KEY `idx_shop_id` (`shop_id`),
  KEY `idx_start_time` (`start_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='桌台使用记录表';

-- ============================================
-- 4. 店铺配置表
-- ============================================
DROP TABLE IF EXISTS `shop_config`;
CREATE TABLE `shop_config` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '配置ID，主键自增',
  `shop_id` BIGINT(20) NOT NULL COMMENT '店铺ID，关联shop_info.id',
  `config_key` VARCHAR(100) NOT NULL COMMENT '配置键名',
  `config_value` TEXT COMMENT '配置值',
  `config_desc` VARCHAR(255) DEFAULT NULL COMMENT '配置说明',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_shop_config` (`shop_id`, `config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='店铺配置表';

-- 插入示例配置数据
INSERT INTO `shop_config` (`shop_id`, `config_key`, `config_value`, `config_desc`) VALUES
(1, 'queue_enabled', 'true', '是否启用排队功能'),
(1, 'max_queue_number', '50', '最大排队号码数'),
(1, 'auto_call_interval', '300', '自动叫号间隔（秒）'),
(1, 'payment_timeout', '900', '支付超时时间（秒）');
