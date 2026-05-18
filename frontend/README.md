# 餐饮点餐排队系统 - Vue3 前端

## 🎨 界面设计

### 布局结构
- **顶部导航栏**: Logo、角色徽章、用户头像、登出按钮
- **左侧菜单**: 根据登录角色动态显示功能菜单
- **内容区域**: 显示具体功能页面

### 角色权限

| 功能 | 用户 | 店员 | 店长 | 超级管理员 |
|------|------|------|------|-----------|
| 取号 | ✅ | ✅ | ✅ | ✅ |
| 查看我的排队 | ✅ | ✅ | ✅ | ✅ |
| 创建订单 | ✅ | ✅ | ✅ | ✅ |
| 查看我的订单 | ✅ | ✅ | ✅ | ✅ |
| 叫号 | ❌ | ✅ | ✅ | ✅ |
| 完成排队 | ❌ | ✅ | ✅ | ✅ |
| 查看所有订单 | ❌ | ✅ | ✅ | ✅ |
| 管理店铺 | ❌ | ❌ | ✅ | ✅ |
| 系统设置 | ❌ | ❌ | ❌ | ✅ |

## 📦 安装依赖

```bash
cd frontend
npm install
```

## 🚀 启动开发服务器

```bash
npm run dev
```

访问: http://localhost:3000

## 📁 项目结构

```
frontend/
├── src/
│   ├── api/
│   │   └── index.js              # API 接口封装
│   ├── stores/
│   │   └── user.js               # 用户状态管理（角色、权限）
│   ├── router/
│   │   └── index.js              # 路由配置（含权限守卫）
│   ├── layouts/
│   │   └── MainLayout.vue        # 主布局（顶部+左侧菜单）
│   ├── views/
│   │   ├── LoginView.vue         # 登录页面
│   │   ├── DashboardView.vue     # 首页仪表盘
│   │   ├── ShopView.vue          # 店铺管理
│   │   ├── QueueView.vue         # 排队取号
│   │   ├── OrderView.vue         # 订单管理
│   │   ├── CallNumberView.vue    # 叫号管理
│   │   └── SettingsView.vue      # 系统设置
│   ├── App.vue                   # 根组件
│   └── main.js                   # 入口文件
├── index.html
├── vite.config.js
└── package.json
```

## 🎯 功能模块

### 1. 店铺管理 (ShopView.vue)
- ✅ 创建店铺
- ✅ 查看店铺列表
- ✅ 删除店铺
- ✅ 显示营业状态

**调用接口**:
- `GET /api/shop/list` - 获取店铺列表
- `POST /api/shop` - 创建店铺
- `DELETE /api/shop/{id}` - 删除店铺

---

### 2. 排队取号 (QueueView.vue)
- ✅ 取号（服务端生成排队号码）
- ✅ 查看我的排队
- ✅ 取消排队
- ✅ 实时等待队列（Redis）
- ✅ WebSocket 通知

**调用接口**:
- `POST /api/queue` - 取号
- `GET /api/queue/list` - 获取排队列表
- `PUT /api/queue/{id}/cancel` - 取消排队
- `GET /api/queue/redis/waiting/{shopId}` - 实时等待队列
- `GET /api/queue/redis/calling/{shopId}` - 实时叫号队列
- `GET /api/queue/redis/position/{shopId}/{queueId}` - 排队位置

**WebSocket**:
- `ws://localhost:8086/ws/notification/{userId}` - 接收通知

---

### 3. 订单管理 (OrderView.vue)
- ✅ 创建订单（可选关联排队ID）
- ✅ 查看订单列表
- ✅ 取消订单
- ✅ 显示订单状态

**调用接口**:
- `POST /api/order` - 创建订单
- `GET /api/order/list` - 获取订单列表
- `PUT /api/order/{id}/cancel` - 取消订单

---

## 🔧 代理配置

vite.config.js 中已配置代理，自动转发请求到后端服务：

```javascript
proxy: {
  '/api/shop': 'http://localhost:8081',    // shop-service
  '/api/queue': 'http://localhost:8085',   // queue-service
  '/api/order': 'http://localhost:8083',   // order-service
  '/api/notification': 'http://localhost:8086' // notification-service
}
```

## 📊 WebSocket 通知

前端会自动连接 WebSocket 并接收以下通知：

1. **QUEUE_CREATED** - 取号成功
2. **QUEUE_CALLED** - 叫号通知
3. **ORDER_CREATED** - 订单创建成功

通知会以 alert 弹窗形式显示。

## 🎨 UI 特点

- 简洁的卡片式设计
- 渐变色导航栏
- 响应式布局
- 状态颜色区分
- Emoji 图标增强可读性

## 🔍 测试流程

### 1. 登录系统

访问 http://localhost:3000，会自动跳转到登录页。

**测试账号**（点击快速填充）：
- 👤 普通用户: user / 123456
- 🧑‍💼 店员: staff / 123456
- 👨‍💼 店长: manager / 123456
- ⚙️ 管理员: admin / 123456

### 2. 不同角色看到的菜单

**普通用户 (USER)**:
- 🎫 取号排队
- 📦 我的订单

**店员 (STAFF)**:
- 🎫 取号排队
- 🔔 叫号管理
- 📦 订单管理

**店长 (MANAGER)**:
- 🎫 取号排队
- 🔔 叫号管理
- 📦 订单管理
- 🏪 店铺管理

**超级管理员 (ADMIN)**:
- 🎫 取号排队
- 🔔 叫号管理
- 📦 订单管理
- 🏪 店铺管理
- ⚙️ 系统设置

### 完整业务流程测试：

1. **创建店铺**
   - 进入"店铺管理"
   - 填写店铺信息
   - 点击"创建"

2. **排队取号**
   - 进入"排队取号"
   - 填写取号信息（店铺ID、用户ID等）
   - 点击"取号"
   - 收到 WebSocket 通知："🎉 取号成功！排队号码: A20260518001"

3. **叫号**（需要在后端或 Knife4j 操作）
   - 访问 http://localhost:8085/doc.html
   - 找到"叫号"接口
   - 传入排队ID进行叫号
   - 前端收到通知："🔔 叫号通知，请前往就餐！"

4. **创建订单**
   - 进入"订单管理"
   - 填写订单信息（可填入刚才的排队ID）
   - 点击"创建订单"
   - 收到通知："✅ 订单创建成功"

5. **查看实时队列**
   - 在"排队取号"页面
   - 输入店铺ID
   - 点击"查询"
   - 查看 Redis 中的等待人数和排队ID列表

## 💡 注意事项

1. **后端服务必须启动**：
   - eureka-server (8761)
   - shop-service (8081)
   - queue-service (8085)
   - order-service (8083)
   - notification-service (8086)

2. **数据库需要初始化**：
   - 执行 SQL 建表脚本

3. **Redis 需要启动**：
   - 用于实时排队队列

4. **WebSocket 连接**：
   - 默认连接 userId=1001
   - 可以在代码中修改

## 🛠️ 技术栈

- Vue 3 (Composition API)
- Vue Router 4 (路由管理)
- Pinia (状态管理)
- Vite 5
- Axios
- WebSocket

## 📝 扩展建议

如需添加更多功能：

1. **真实登录 API**: 替换 LoginView 中的模拟登录，调用后端认证接口
2. **JWT Token**: 实现真实的 Token 验证和刷新机制
3. **UI 库**: 集成 Element Plus 或 Ant Design Vue
4. **错误处理**: 更友好的错误提示和加载状态
5. **数据可视化**: 使用 ECharts 展示统计图表
6. **消息通知**: 集成 WebSocket 实时消息中心

---

**开发者**: Lingma AI Assistant  
**日期**: 2026-05-18
