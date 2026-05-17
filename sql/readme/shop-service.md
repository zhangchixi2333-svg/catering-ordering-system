# 店铺服务数据库说明 (shop-service)

## 数据库概述

**数据库名称**: shop_service  
**功能描述**: 管理店铺基本信息、桌台信息以及桌台使用记录  
**字符集**: utf8mb4  
**排序规则**: utf8mb4_unicode_ci

---

## 数据表清单

### 1. shop_info - 店铺信息表

**功能**: 存储店铺的基本信息，包括名称、地址、联系方式、营业状态等。

| 字段名 | 类型 | 说明 | 备注 |
|--------|------|------|------|
| id | BIGINT(20) | 店铺ID | 主键，自增 |
| shop_name | VARCHAR(100) | 店铺名称 | 不能为空 |
| shop_code | VARCHAR(50) | 店铺编码 | 唯一标识，不能为空 |
| address | VARCHAR(255) | 店铺详细地址 | 可选 |
| phone | VARCHAR(20) | 店铺联系电话 | 可选 |
| business_hours | VARCHAR(100) | 营业时间 | 格式：09:00-22:00 |
| shop_status | TINYINT(1) | 店铺状态 | 0-停业，1-营业，2-装修中，默认1 |
| capacity | INT(11) | 店铺总座位数 | 默认0 |
| description | TEXT | 店铺描述信息 | 可选 |
| logo_url | VARCHAR(255) | 店铺Logo图片URL | 可选 |
| created_at | DATETIME | 创建时间 | 自动生成 |
| updated_at | DATETIME | 更新时间 | 自动更新 |

**索引**:
- PRIMARY KEY: `id`
- UNIQUE KEY: `uk_shop_code` (`shop_code`)

**示例数据**:
- 美味餐厅旗舰店 (SHOP001)
- 美味餐厅万达店 (SHOP002)

---

### 2. table_info - 桌台信息表

**功能**: 管理店铺内的桌台信息，包括桌号、座位数、桌台类型、状态等。

| 字段名 | 类型 | 说明 | 备注 |
|--------|------|------|------|
| id | BIGINT(20) | 桌台ID | 主键，自增 |
| shop_id | BIGINT(20) | 所属店铺ID | 关联shop_info.id，不能为空 |
| table_number | VARCHAR(20) | 桌台编号 | 如：A01、B02，不能为空 |
| table_name | VARCHAR(50) | 桌台名称 | 如：一号桌、VIP包厢 |
| seats | INT(11) | 座位数量 | 默认4 |
| table_type | TINYINT(1) | 桌台类型 | 1-普通桌，2-卡座，3-包厢，4-吧台，默认1 |
| table_status | TINYINT(1) | 桌台状态 | 0-空闲，1-已占用，2-预订，3-清洁中，默认0 |
| qr_code | VARCHAR(255) | 扫码点餐二维码URL | 可选 |
| location | VARCHAR(50) | 位置描述 | 如：一楼大厅、二楼东侧 |
| is_available | TINYINT(1) | 是否可用 | 0-不可用，1-可用，默认1 |
| created_at | DATETIME | 创建时间 | 自动生成 |
| updated_at | DATETIME | 更新时间 | 自动更新 |

**索引**:
- PRIMARY KEY: `id`
- UNIQUE KEY: `uk_shop_table` (`shop_id`, `table_number`)
- KEY: `idx_shop_id` (`shop_id`)

**示例数据**:
- SHOP001店铺: A01-A03（普通桌）、B01-B02（卡座）、V01-V02（VIP包厢）
- SHOP002店铺: A01-A02（普通桌）、B01（卡座）

---

### 3. table_usage_log - 桌台使用记录表

**功能**: 记录桌台的使用历史，包括开始时间、结束时间、用餐人数等。

| 字段名 | 类型 | 说明 | 备注 |
|--------|------|------|------|
| id | BIGINT(20) | 记录ID | 主键，自增 |
| table_id | BIGINT(20) | 桌台ID | 关联table_info.id，不能为空 |
| shop_id | BIGINT(20) | 店铺ID | 关联shop_info.id，不能为空 |
| order_id | BIGINT(20) | 关联订单ID | 可选 |
| start_time | DATETIME | 开始使用时间 | 不能为空 |
| end_time | DATETIME | 结束使用时间 | 可选 |
| party_size | INT(11) | 用餐人数 | 可选 |
| usage_status | TINYINT(1) | 使用状态 | 1-使用中，2-已结束，默认1 |
| created_at | DATETIME | 创建时间 | 自动生成 |

**索引**:
- PRIMARY KEY: `id`
- KEY: `idx_table_id` (`table_id`)
- KEY: `idx_shop_id` (`shop_id`)
- KEY: `idx_start_time` (`start_time`)

---

### 4. shop_config - 店铺配置表

**功能**: 存储店铺的各种配置项，支持动态配置。

| 字段名 | 类型 | 说明 | 备注 |
|--------|------|------|------|
| id | BIGINT(20) | 配置ID | 主键，自增 |
| shop_id | BIGINT(20) | 店铺ID | 关联shop_info.id，不能为空 |
| config_key | VARCHAR(100) | 配置键名 | 不能为空 |
| config_value | TEXT | 配置值 | 可选 |
| config_desc | VARCHAR(255) | 配置说明 | 可选 |
| created_at | DATETIME | 创建时间 | 自动生成 |
| updated_at | DATETIME | 更新时间 | 自动更新 |

**索引**:
- PRIMARY KEY: `id`
- UNIQUE KEY: `uk_shop_config` (`shop_id`, `config_key`)

**示例配置**:
- queue_enabled: 是否启用排队功能
- max_queue_number: 最大排队号码数
- auto_call_interval: 自动叫号间隔（秒）
- payment_timeout: 支付超时时间（秒）

---

## 业务场景

### 1. 店铺管理
- 创建新店铺
- 更新店铺信息
- 查询店铺列表
- 切换店铺营业状态

### 2. 桌台管理
- 添加/删除桌台
- 修改桌台状态（空闲、占用、预订、清洁中）
- 生成桌台二维码
- 查询可用桌台

### 3. 桌台使用追踪
- 记录顾客入座时间
- 记录顾客离座时间
- 统计桌台使用率
- 分析高峰时段

---

## 注意事项

1. **字符集**: 所有表均使用utf8mb4字符集，支持emoji等特殊字符
2. **时间字段**: 所有时间字段使用DATETIME类型，便于时区转换
3. **状态字段**: 使用TINYINT类型存储状态，提高查询效率
4. **索引优化**: 为常用查询字段建立索引，提升查询性能
5. **外键关系**: 未设置物理外键，通过应用层维护数据一致性
6. **软删除**: 使用is_available字段实现软删除，保留历史数据

---

## 扩展建议

1. 可以添加店铺分类表，支持连锁店分类管理
2. 可以添加店铺评价表，记录顾客对店铺的评价
3. 可以添加店铺营业时间表，支持复杂营业时间安排
4. 可以添加桌台区域表，将桌台按区域分组管理
