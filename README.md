# 智旅旅行旅游推荐预订系统

基于 Java SSM 的旅游推荐与预订系统。系统支持用户注册登录、路线推荐、路线详情、酒店预订、门票预订、订单支付、订单评价、AI 问答、高德地图展示和后台管理。

## 5 分钟运行

你本机已经有可用脚本：

```bat
F:\tourism-system\run-local.bat
```

双击后脚本会自动完成：

1. 设置本机 JDK、Tomcat、MySQL 环境变量。
2. 执行 `mvn clean package -DskipTests`。
3. 复制 `target/tourism-system.war` 到 Tomcat `webapps`。
4. 启动 Tomcat。
5. 打开 `http://localhost:8080/tourism-system/`。

演示账号：

| 角色 | 账号 | 密码 |
| --- | --- | --- |
| 普通用户 | `demo` | `demo123` |
| 管理员 | `admin` | `admin123` |

## GitHub 使用说明

仓库里保留 `run-local.example.bat`，不提交你的本机密码和路径。

你上传 GitHub 前需要注意：

1. `run-local.bat` 已加入 `.gitignore`，用于你本机一键运行。
2. `run-local.example.bat` 可以提交，别人复制后改成本机路径即可。
3. `src/main/resources/application.properties` 使用环境变量读取数据库和 AI 配置。
4. `src/main/webapp/static/js/amap-config.js` 不写真实高德 Key。
5. 不要提交任何 `sk-` 开头的大模型密钥。

## 常规运行步骤

如果不用一键脚本，可以按下面步骤运行：

1. 创建 MySQL 数据库：`tourism_system`。
2. 执行初始化脚本：`src/main/resources/sql/init.sql`。
3. 设置环境变量：`DB_URL`、`DB_USERNAME`、`DB_PASSWORD`。
4. 执行打包：`mvn clean package -DskipTests`。
5. 将 `target/tourism-system.war` 部署到 Tomcat 9。
6. 访问：`http://localhost:8080/tourism-system/`。

## 高德地图配置

打开：

```text
src/main/webapp/static/js/amap-config.js
```

本机演示时填入自己的 Key：

```javascript
window.TOURISM_AMAP_CONFIG = {
    key: "你的高德Key",
    securityJsCode: "你的安全密钥"
};
```

如果不配置 Key，地图页会显示兜底提示。左侧点位列表仍能演示。

## AI 配置

默认关闭外部大模型。系统会使用本地 RAG 检索回答。

如需启用外部模型，设置环境变量：

```bat
set AI_API_ENABLED=true
set AI_API_URL=https://api.deepseek.com/chat/completions
set AI_API_MODEL=deepseek-v4-flash
set AI_API_KEY=你的API密钥
```

## 技术栈

- Spring
- Spring MVC
- MyBatis
- JSP
- MySQL
- Tomcat 9
- 高德地图 Web JavaScript API

## 页面入口

| 页面 | 地址 |
| --- | --- |
| 入口页 | `/index.jsp` |
| 登录/注册页 | `/login.jsp` |
| 用户首页 | `/home.jsp` |
| 路线规划 | `/routes.jsp` |
| 酒店预订 | `/hotels.jsp` |
| 门票活动 | `/tickets.jsp` |
| 旅游地图 | `/map.jsp` |
| AI 行程助手 | `/ai.jsp` |
| 我的订单 | `/orders.jsp` |
| 后台管理 | `/admin.jsp` |

## 核心功能

- 用户注册、登录、退出登录。
- 目的地路线推荐。
- 路线详情、每日安排、费用明细。
- 高德地图景点标记和路线展示。
- 酒店查询、房型查询、酒店下单。
- 景点查询、门票查询、门票下单。
- 订单列表、订单取消、模拟支付、数据库订单评价。
- AI 旅游问答、RAG 检索回答和外部 API 生成。
- 路线、酒店、景点数据库收藏。
- 后台景点、酒店、门票、路线和订单管理。

## 主要接口

### 用户认证

- `POST /api/auth/login`
- `POST /api/auth/register`
- `GET /api/auth/me`
- `POST /api/auth/logout`

### 前台旅游功能

- `GET /api/scenic/list`
- `GET /api/hotel/list?city=杭州`
- `GET /api/hotel/rooms?hotelId=1`
- `GET /api/ticket/list?scenicId=1`
- `POST /api/route/recommend`
- `POST /api/ai/ask`

### 订单功能

- `POST /api/order/hotel/create`
- `POST /api/order/ticket/create`
- `POST /api/order/hotel/cancel?orderId=1`
- `POST /api/order/ticket/cancel?orderId=1`
- `POST /api/order/hotel/pay?orderId=1`
- `POST /api/order/ticket/pay?orderId=1`
- `GET /api/user/orders`

### 后台管理

- `GET /api/admin/dashboard/summary`
- `GET /api/admin/scenic/list`
- `POST /api/admin/scenic/save`
- `PUT /api/admin/scenic/update`
- `DELETE /api/admin/scenic/delete/{id}`
- `GET /api/admin/hotel/list`
- `POST /api/admin/hotel/save`
- `PUT /api/admin/hotel/update`
- `DELETE /api/admin/hotel/delete/{id}`
- `GET /api/admin/ticket/list`
- `POST /api/admin/ticket/save`
- `PUT /api/admin/ticket/update`
- `DELETE /api/admin/ticket/delete/{id}`
- `GET /api/admin/route/list`
- `POST /api/admin/route/save`
- `PUT /api/admin/route/update`
- `DELETE /api/admin/route/delete/{id}`
- `GET /api/admin/order/list`

## 文档

- 开发说明：`docs/开发说明.md`
- 答辩说明：`docs/答辩说明.md`
- 高德地图接入说明：`docs/高德地图接入说明.md`
- AI 大模型接入说明：`docs/AI大模型接入说明.md`
