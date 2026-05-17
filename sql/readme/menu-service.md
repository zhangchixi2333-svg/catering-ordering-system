# 菜单服务数据库说明 (menu-service)

## 数据库概述

**数据库名称**: menu_service  
**功能描述**: 管理菜单分类、菜品信息、规格配料、评价等  
**字符集**: utf8mb4  
**排序规则**: utf8mb4_unicode_ci

---

## 数据表清单

### 1. menu_category - 菜单分类表

**功能**: 管理菜品的分类体系，支持多级分类。

| 字段名 | 类型 | 说明 | 备注 |
|--------|------|------|------|
| id | BIGINT(20) | 分类ID | 主键，自增 |
| shop_id | BIGINT(20) | 所属店铺ID | 关联shop_info.id，不能为空 |
| category_name | VARCHAR(50) | 分类名称 | 如：热菜、凉菜、饮品，不能为空 |
| parent_id | BIGINT(20) | 父分类ID | 0表示一级分类，默认0 |
| sort_order | INT(11) | 排序号 | 数字越小越靠前，默认0 |
| icon_url | VARCHAR(255) | 分类图标URL | 可选 |
| is_visible | TINYINT(1) | 是否可见 | 0-隐藏，1-显示，默认1 |
| created_at | DATETIME | 创建时间 | 自动生成 |
| updated_at | DATETIME | 更新时间 | 自动更新 |

**索引**:
- PRIMARY KEY: `id`
- KEY: `idx_shop_id` (`shop_id`)
- KEY: `idx_parent_id` (`parent_id`)

**示例数据**:
- 热销推荐、主食、热菜、凉菜、汤品、饮品、甜点

---

### 2. menu_item - 菜品信息表

**功能**: 存储菜品的详细信息，包括价格、库存、评分等。

| 字段名 | 类型 | 说明 | 备注 |
|--------|------|------|------|
| id | BIGINT(20) | 菜品ID | 主键，自增 |
| shop_id | BIGINT(20) | 所属店铺ID | 关联shop_info.id，不能为空 |
| category_id | BIGINT(20) | 所属分类ID | 关联menu_category.id，不能为空 |
| item_name | VARCHAR(100) | 菜品名称 | 不能为空 |
| item_code | VARCHAR(50) | 菜品编码 | 唯一标识 |
| description | TEXT | 菜品描述 | 可选 |
| price | DECIMAL(10,2) | 菜品价格（元） | 不能为空 |
| original_price | DECIMAL(10,2) | 原价（元） | 用于显示折扣 |
| cost_price | DECIMAL(10,2) | 成本价（元） | 可选 |
| unit | VARCHAR(20) | 计量单位 | 如：份、个、杯，默认"份" |
| image_url | VARCHAR(255) | 菜品图片URL | 可选 |
| stock | INT(11) | 库存数量 | -1表示无限制，默认-1 |
| sales_count | INT(11) | 销售数量 | 默认0 |
| rating | DECIMAL(3,2) | 评分 | 范围0-5，默认5.00 |
| preparation_time | INT(11) | 预计制作时间（分钟） | 默认15 |
| spicy_level | TINYINT(1) | 辣度等级 | 0-不辣，1-微辣，2-中辣，3-特辣，默认0 |
| is_recommended | TINYINT(1) | 是否推荐 | 0-否，1-是，默认0 |
| is_available | TINYINT(1) | 是否可售 | 0-下架，1-上架，默认1 |
| tags | VARCHAR(255) | 标签 | 多个用逗号分隔，如：招牌,必点 |
| ingredients | TEXT | 主要食材 | 可选 |
| allergens | VARCHAR(255) | 过敏原信息 | 可选 |
| calories | INT(11) | 热量（卡路里） | 可选 |
| sort_order | INT(11) | 排序号 | 默认0 |
| created_at | DATETIME | 创建时间 | 自动生成 |
| updated_at | DATETIME | 更新时间 | 自动更新 |

**索引**:
- PRIMARY KEY: `id`
- UNIQUE KEY: `uk_item_code` (`shop_id`, `item_code`)
- KEY: `idx_shop_id` (`shop_id`)
- KEY: `idx_category_id` (`category_id`)
- KEY: `idx_is_available` (`is_available`)

**示例数据**:
- 宫保鸡丁、麻婆豆腐、扬州炒饭、红烧肉等14道菜品

---

### 3. item_specification - 菜品规格表

**功能**: 支持菜品的多规格设置，如大小份、辣度、温度等。

| 字段名 | 类型 | 说明 | 备注 |
|--------|------|------|------|
| id | BIGINT(20) | 规格ID | 主键，自增 |
| item_id | BIGINT(20) | 菜品ID | 关联menu_item.id，不能为空 |
| spec_name | VARCHAR(50) | 规格名称 | 如：大份、小份、微辣，不能为空 |
| spec_type | VARCHAR(20) | 规格类型 | size-份量，spicy-辣度，temperature-温度 |
| price_adjustment | DECIMAL(10,2) | 价格调整（元） | 可正可负，默认0.00 |
| stock | INT(11) | 该规格库存 | -1表示无限制，默认-1 |
| is_default | TINYINT(1) | 是否默认规格 | 0-否，1-是，默认0 |
| is_available | TINYINT(1) | 是否可用 | 0-不可用，1-可用，默认1 |
| sort_order | INT(11) | 排序号 | 默认0 |
| created_at | DATETIME | 创建时间 | 自动生成 |
| updated_at | DATETIME | 更新时间 | 自动更新 |

**索引**:
- PRIMARY KEY: `id`
- KEY: `idx_item_id` (`item_id`)

**示例数据**:
- 宫保鸡丁：标准份、大份（+10元）
- 麻婆豆腐：微辣、中辣、特辣
- 酸梅汤：常温、加冰

---

### 4. item_topping - 菜品配料表

**功能**: 支持菜品的可选加料，如加蛋、加面等。

| 字段名 | 类型 | 说明 | 备注 |
|--------|------|------|------|
| id | BIGINT(20) | 配料ID | 主键，自增 |
| item_id | BIGINT(20) | 菜品ID | 关联menu_item.id，不能为空 |
| topping_name | VARCHAR(50) | 配料名称 | 如：加蛋、加面、加辣，不能为空 |
| price | DECIMAL(10,2) | 配料价格（元） | 默认0.00 |
| is_required | TINYINT(1) | 是否必选 | 0-可选，1-必选，默认0 |
| max_select | INT(11) | 最多可选数量 | 0表示不限制，默认1 |
| is_available | TINYINT(1) | 是否可用 | 0-不可用，1-可用，默认1 |
| sort_order | INT(11) | 排序号 | 默认0 |
| created_at | DATETIME | 创建时间 | 自动生成 |
| updated_at | DATETIME | 更新时间 | 自动更新 |

**索引**:
- PRIMARY KEY: `id`
- KEY: `idx_item_id` (`item_id`)

**示例数据**:
- 扬州炒饭：加蛋（3元）、加火腿（5元）
- 手工水饺：加醋、加辣椒油

---

### 5. item_review - 菜品评价表

**功能**: 存储顾客对菜品的评价和评分。

| 字段名 | 类型 | 说明 | 备注 |
|--------|------|------|------|
| id | BIGINT(20) | 评价ID | 主键，自增 |
| item_id | BIGINT(20) | 菜品ID | 关联menu_item.id，不能为空 |
| order_id | BIGINT(20) | 关联订单ID | 可选 |
| user_id | BIGINT(20) | 用户ID | 可选 |
| rating | TINYINT(1) | 评分 | 1-5星，不能为空 |
| comment | TEXT | 评价内容 | 可选 |
| images | TEXT | 评价图片URL | 多个用逗号分隔 |
| is_anonymous | TINYINT(1) | 是否匿名 | 0-否，1-是，默认0 |
| like_count | INT(11) | 点赞数 | 默认0 |
| created_at | DATETIME | 创建时间 | 自动生成 |

**索引**:
- PRIMARY KEY: `id`
- KEY: `idx_item_id` (`item_id`)
- KEY: `idx_order_id` (`order_id`)

---

### 6. menu_change_log - 菜单上下架记录表

**功能**: 记录菜品的上下架、价格调整等操作历史。

| 字段名 | 类型 | 说明 | 备注 |
|--------|------|------|------|
| id | BIGINT(20) | 记录ID | 主键，自增 |
| item_id | BIGINT(20) | 菜品ID | 关联menu_item.id，不能为空 |
| shop_id | BIGINT(20) | 店铺ID | 关联shop_info.id，不能为空 |
| change_type | TINYINT(1) | 变更类型 | 1-上架，2-下架，3-价格调整，4-库存调整 |
| old_value | VARCHAR(255) | 变更前的值 | 可选 |
| new_value | VARCHAR(255) | 变更后的值 | 可选 |
| operator_id | BIGINT(20) | 操作人ID | 可选 |
| operator_name | VARCHAR(50) | 操作人姓名 | 可选 |
| remark | VARCHAR(255) | 备注说明 | 可选 |
| created_at | DATETIME | 创建时间 | 自动生成 |

**索引**:
- PRIMARY KEY: `id`
- KEY: `idx_item_id` (`item_id`)
- KEY: `idx_shop_id` (`shop_id`)
- KEY: `idx_created_at` (`created_at`)

---

## 业务场景

### 1. 菜单管理
- 创建/编辑/删除菜品
- 菜品上下架
- 价格调整
- 库存管理

### 2. 分类管理
- 创建多级分类
- 分类排序
- 分类显示/隐藏

### 3. 规格与配料
- 为菜品添加多规格（大小份、辣度等）
- 配置可选配料（加蛋、加面等）
- 规格价格调整

### 4. 菜品推荐
- 设置推荐菜品
- 根据销量、评分排序
- 热门标签管理

### 5. 评价管理
- 查看菜品评价
- 评价审核
- 评价统计分析

---

## 注意事项

1. **价格精度**: 使用DECIMAL(10,2)存储价格，避免浮点数精度问题
2. **库存管理**: stock=-1表示无限制库存，适用于大部分菜品
3. **评分计算**: rating字段需要定期根据评价重新计算平均值
4. **销量统计**: sales_count需要在订单完成时自动累加
5. **软删除**: 使用is_available实现菜品上下架，保留历史数据
6. **图片存储**: image_url建议配合CDN使用，提高加载速度

---

## 扩展建议

1. 可以添加菜品套餐表，支持组合销售
2. 可以添加菜品定时上下架功能
3. 可以添加菜品限时折扣表
4. 可以添加菜品营养成分详细表
5. 可以添加菜品素材库，统一管理图片资源
