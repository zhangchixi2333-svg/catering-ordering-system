-- ============================================
-- 菜单服务数据库 (menu-service)
-- 功能：菜单分类管理、菜品信息管理
-- ============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS menu_service DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE menu_service;

-- ============================================
-- 1. 菜单分类表
-- ============================================
DROP TABLE IF EXISTS `menu_category`;
CREATE TABLE `menu_category` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '分类ID，主键自增',
  `shop_id` BIGINT(20) NOT NULL COMMENT '所属店铺ID，关联shop_info.id',
  `category_name` VARCHAR(50) NOT NULL COMMENT '分类名称，如：热菜、凉菜、饮品',
  `parent_id` BIGINT(20) DEFAULT 0 COMMENT '父分类ID，0表示一级分类',
  `sort_order` INT(11) NOT NULL DEFAULT 0 COMMENT '排序号，数字越小越靠前',
  `icon_url` VARCHAR(255) DEFAULT NULL COMMENT '分类图标URL',
  `is_visible` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否可见：0-隐藏，1-显示',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_shop_category` (`shop_id`, `category_name`, `parent_id`),
  KEY `idx_shop_id` (`shop_id`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单分类表';

-- 插入示例数据（SHOP001店铺的分类）
INSERT INTO `menu_category` (`shop_id`, `category_name`, `parent_id`, `sort_order`, `is_visible`) VALUES
(1, '热销推荐', 0, 1, 1),
(1, '主食', 0, 2, 1),
(1, '热菜', 0, 3, 1),
(1, '凉菜', 0, 4, 1),
(1, '汤品', 0, 5, 1),
(1, '饮品', 0, 6, 1),
(1, '甜点', 0, 7, 1);

-- ============================================
-- 2. 菜品信息表
-- ============================================
DROP TABLE IF EXISTS `menu_item`;
CREATE TABLE `menu_item` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '菜品ID，主键自增',
  `shop_id` BIGINT(20) NOT NULL COMMENT '所属店铺ID，关联shop_info.id',
  `category_id` BIGINT(20) NOT NULL COMMENT '所属分类ID，关联menu_category.id',
  `item_name` VARCHAR(100) NOT NULL COMMENT '菜品名称',
  `item_code` VARCHAR(50) DEFAULT NULL COMMENT '菜品编码，唯一标识',
  `description` TEXT COMMENT '菜品描述',
  `price` DECIMAL(10, 2) NOT NULL COMMENT '菜品价格（元）',
  `original_price` DECIMAL(10, 2) DEFAULT NULL COMMENT '原价（元），用于显示折扣',
  `cost_price` DECIMAL(10, 2) DEFAULT NULL COMMENT '成本价（元）',
  `unit` VARCHAR(20) DEFAULT '份' COMMENT '计量单位，如：份、个、杯',
  `image_url` VARCHAR(255) DEFAULT NULL COMMENT '菜品图片URL',
  `stock` INT(11) DEFAULT -1 COMMENT '库存数量，-1表示无限制',
  `sales_count` INT(11) DEFAULT 0 COMMENT '销售数量',
  `rating` DECIMAL(3, 2) DEFAULT 5.00 COMMENT '评分，范围0-5',
  `preparation_time` INT(11) DEFAULT 15 COMMENT '预计制作时间（分钟）',
  `spicy_level` TINYINT(1) DEFAULT 0 COMMENT '辣度等级：0-不辣，1-微辣，2-中辣，3-特辣',
  `is_recommended` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否推荐：0-否，1-是',
  `is_available` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否可售：0-下架，1-上架',
  `tags` VARCHAR(255) DEFAULT NULL COMMENT '标签，多个用逗号分隔，如：招牌,必点',
  `ingredients` TEXT COMMENT '主要食材',
  `allergens` VARCHAR(255) DEFAULT NULL COMMENT '过敏原信息',
  `calories` INT(11) DEFAULT NULL COMMENT '热量（卡路里）',
  `sort_order` INT(11) NOT NULL DEFAULT 0 COMMENT '排序号',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_item_code` (`shop_id`, `item_code`),
  KEY `idx_shop_id` (`shop_id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_is_available` (`is_available`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜品信息表';

-- 插入示例数据（SHOP001店铺的菜品）
INSERT INTO `menu_item` (`shop_id`, `category_id`, `item_name`, `item_code`, `description`, `price`, `original_price`, `unit`, `stock`, `sales_count`, `rating`, `preparation_time`, `spicy_level`, `is_recommended`, `is_available`, `tags`) VALUES
(1, 1, '宫保鸡丁', 'ITEM001', '经典川菜，鸡肉鲜嫩，花生酥脆', 38.00, 45.00, '份', -1, 1250, 4.8, 15, 2, 1, 1, '招牌,必点'),
(1, 1, '麻婆豆腐', 'ITEM002', '正宗川味，麻辣鲜香', 28.00, NULL, '份', -1, 980, 4.7, 12, 3, 1, 1, '热销'),
(1, 2, '扬州炒饭', 'ITEM003', '粒粒分明，配料丰富', 32.00, NULL, '份', -1, 856, 4.6, 10, 0, 0, 1, NULL),
(1, 2, '手工水饺', 'ITEM004', '现包现煮，鲜美多汁', 25.00, NULL, '份', -1, 723, 4.5, 15, 0, 0, 1, NULL),
(1, 3, '红烧肉', 'ITEM005', '肥而不腻，入口即化', 58.00, 68.00, '份', -1, 645, 4.9, 20, 0, 1, 1, '招牌'),
(1, 3, '糖醋里脊', 'ITEM006', '酸甜可口，外酥里嫩', 42.00, NULL, '份', -1, 534, 4.6, 15, 0, 0, 1, NULL),
(1, 4, '凉拌黄瓜', 'ITEM007', '清爽开胃，夏日必备', 15.00, NULL, '份', -1, 423, 4.4, 5, 0, 0, 1, NULL),
(1, 4, '口水鸡', 'ITEM008', '麻辣鲜香，肉质细嫩', 35.00, NULL, '份', -1, 389, 4.7, 10, 2, 1, 1, '推荐'),
(1, 5, '酸辣汤', 'ITEM009', '酸辣开胃，暖胃佳品', 18.00, NULL, '碗', -1, 567, 4.5, 8, 1, 0, 1, NULL),
(1, 5, '紫菜蛋花汤', 'ITEM010', '清淡营养，家常味道', 12.00, NULL, '碗', -1, 445, 4.3, 5, 0, 0, 1, NULL),
(1, 6, '酸梅汤', 'ITEM011', '自制酸梅汤，消暑解渴', 15.00, NULL, '杯', -1, 678, 4.6, 3, 0, 0, 1, NULL),
(1, 6, '鲜榨橙汁', 'ITEM012', '新鲜橙子现榨，维C满满', 22.00, NULL, '杯', -1, 345, 4.7, 5, 0, 0, 1, NULL),
(1, 7, '红豆沙', 'ITEM013', '甜而不腻，细腻顺滑', 16.00, NULL, '碗', -1, 234, 4.5, 5, 0, 0, 1, NULL),
(1, 7, '芒果布丁', 'ITEM014', '新鲜芒果制作，香甜可口', 20.00, NULL, '份', -1, 189, 4.6, 5, 0, 0, 1, NULL);

-- ============================================
-- 3. 菜品规格表（多规格支持）
-- ============================================
DROP TABLE IF EXISTS `item_specification`;
CREATE TABLE `item_specification` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '规格ID，主键自增',
  `item_id` BIGINT(20) NOT NULL COMMENT '菜品ID，关联menu_item.id',
  `spec_name` VARCHAR(50) NOT NULL COMMENT '规格名称，如：大份、小份、微辣、中辣',
  `spec_type` VARCHAR(20) NOT NULL COMMENT '规格类型，如：size-份量，spicy-辣度，temperature-温度',
  `price_adjustment` DECIMAL(10, 2) DEFAULT 0.00 COMMENT '价格调整（元），可正可负',
  `stock` INT(11) DEFAULT -1 COMMENT '该规格库存，-1表示无限制',
  `is_default` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否默认规格：0-否，1-是',
  `is_available` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否可用：0-不可用，1-可用',
  `sort_order` INT(11) NOT NULL DEFAULT 0 COMMENT '排序号',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_item_id` (`item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜品规格表';

-- 插入示例数据
INSERT INTO `item_specification` (`item_id`, `spec_name`, `spec_type`, `price_adjustment`, `is_default`, `is_available`) VALUES
(1, '标准份', 'size', 0.00, 1, 1),
(1, '大份', 'size', 10.00, 0, 1),
(2, '微辣', 'spicy', 0.00, 1, 1),
(2, '中辣', 'spicy', 0.00, 0, 1),
(2, '特辣', 'spicy', 0.00, 0, 1),
(11, '常温', 'temperature', 0.00, 1, 1),
(11, '加冰', 'temperature', 0.00, 0, 1);

-- ============================================
-- 4. 菜品配料表（可选加料）
-- ============================================
DROP TABLE IF EXISTS `item_topping`;
CREATE TABLE `item_topping` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '配料ID，主键自增',
  `item_id` BIGINT(20) NOT NULL COMMENT '菜品ID，关联menu_item.id',
  `topping_name` VARCHAR(50) NOT NULL COMMENT '配料名称，如：加蛋、加面、加辣',
  `price` DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '配料价格（元）',
  `is_required` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否必选：0-可选，1-必选',
  `max_select` INT(11) DEFAULT 1 COMMENT '最多可选数量，0表示不限制',
  `is_available` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否可用：0-不可用，1-可用',
  `sort_order` INT(11) NOT NULL DEFAULT 0 COMMENT '排序号',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_item_id` (`item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜品配料表';

-- 插入示例数据
INSERT INTO `item_topping` (`item_id`, `topping_name`, `price`, `is_required`, `max_select`, `is_available`) VALUES
(3, '加蛋', 3.00, 0, 2, 1),
(3, '加火腿', 5.00, 0, 1, 1),
(4, '加醋', 0.00, 0, 1, 1),
(4, '加辣椒油', 0.00, 0, 1, 1);

-- ============================================
-- 5. 菜品评价表
-- ============================================
DROP TABLE IF EXISTS `item_review`;
CREATE TABLE `item_review` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '评价ID，主键自增',
  `item_id` BIGINT(20) NOT NULL COMMENT '菜品ID，关联menu_item.id',
  `order_id` BIGINT(20) DEFAULT NULL COMMENT '关联订单ID',
  `user_id` BIGINT(20) DEFAULT NULL COMMENT '用户ID',
  `rating` TINYINT(1) NOT NULL COMMENT '评分，1-5星',
  `comment` TEXT COMMENT '评价内容',
  `images` TEXT COMMENT '评价图片URL，多个用逗号分隔',
  `is_anonymous` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否匿名：0-否，1-是',
  `like_count` INT(11) DEFAULT 0 COMMENT '点赞数',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_item_id` (`item_id`),
  KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜品评价表';

-- ============================================
-- 6. 菜单上下架记录表
-- ============================================
DROP TABLE IF EXISTS `menu_change_log`;
CREATE TABLE `menu_change_log` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '记录ID，主键自增',
  `item_id` BIGINT(20) NOT NULL COMMENT '菜品ID，关联menu_item.id',
  `shop_id` BIGINT(20) NOT NULL COMMENT '店铺ID，关联shop_info.id',
  `change_type` TINYINT(1) NOT NULL COMMENT '变更类型：1-上架，2-下架，3-价格调整，4-库存调整',
  `old_value` VARCHAR(255) DEFAULT NULL COMMENT '变更前的值',
  `new_value` VARCHAR(255) DEFAULT NULL COMMENT '变更后的值',
  `operator_id` BIGINT(20) DEFAULT NULL COMMENT '操作人ID',
  `operator_name` VARCHAR(50) DEFAULT NULL COMMENT '操作人姓名',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注说明',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_item_id` (`item_id`),
  KEY `idx_shop_id` (`shop_id`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单上下架记录表';

-- ============================================
-- 数据迁移脚本（如果表已存在，添加唯一约束）
-- ============================================
-- 如果 menu_category 表已经存在但没有唯一约束，执行以下语句添加：
-- ALTER TABLE `menu_category` ADD UNIQUE KEY `uk_shop_category` (`shop_id`, `category_name`, `parent_id`);
