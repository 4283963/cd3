# 🎭 沉浸式密室剧本杀全栈平台

一套完整的沉浸式密室剧本杀剧本编排和玩家互动平台，包含导演端后台管理系统和玩家端 H5 响应式应用。

## 📋 项目概述

本项目为沉浸式密室剧本杀店提供完整的数字化解决方案，支持：
- **剧本管理**：剧本信息、角色设定、线索树编排
- **导演控场**：游戏场次管理、角色分配、线索分发、消息推送
- **玩家互动**：个人剧本查看、线索解密、进度同步、实时消息

## 🏗️ 技术架构

### 后端
- **框架**: Spring Boot 2.7.x
- **ORM**: MyBatis Plus 3.5.x
- **数据库**: MySQL 8.0+
- **连接池**: Druid
- **认证**: JWT
- **工具库**: Hutool

### 前端 - 导演端
- **框架**: Vue 3 (CDN 方式)
- **UI 组件**: Element Plus
- **路由**: Vue Router 4
- **HTTP**: Axios

### 前端 - 玩家端
- **框架**: Vue 3 (CDN 方式)
- **UI 组件**: Vant 4
- **路由**: Vue Router 4
- **HTTP**: Axios

> 前端采用 CDN 引入方式，**无需安装依赖**，开箱即用，直接打开 HTML 即可运行。

## 📁 目录结构

```
cd3/
├── database/                    # 数据库脚本
│   └── init.sql                # 数据库初始化脚本（含示例数据）
├── backend/                     # Java 后端
│   ├── pom.xml                 # Maven 配置
│   └── src/main/
│       ├── java/com/scriptkill/
│       │   ├── ScriptKillApplication.java   # 启动类
│       │   ├── common/         # 通用类（返回结果、异常、JWT等）
│       │   ├── config/         # 配置类
│       │   ├── entity/         # 实体类（9张表）
│       │   ├── mapper/         # Mapper 接口
│       │   ├── dto/            # 请求 DTO
│       │   ├── vo/             # 返回 VO
│       │   ├── service/        # 业务逻辑层
│       │   └── controller/     # 控制层
│       └── resources/
│           ├── application.yml # 应用配置
│           └── mapper/         # MyBatis XML
├── frontend/                    # 前端
│   ├── director/               # 导演端后台管理
│   │   ├── index.html
│   │   ├── css/style.css
│   │   └── js/
│   │       ├── api.js
│   │       └── app.js
│   └── player/                 # 玩家端 H5
│       ├── index.html
│       ├── css/style.css
│       └── js/
│           ├── api.js
│           └── app.js
```

## 🗄️ 数据库设计

### 核心表

| 表名 | 说明 |
|------|------|
| `sys_admin` | 管理员/导演表 |
| `script` | 剧本表 |
| `script_role` | 角色表 |
| `clue` | 线索表（树形结构） |
| `game_session` | 游戏场次表 |
| `player` | 玩家表 |
| `player_clue` | 玩家线索表 |
| `player_progress` | 玩家进度表 |
| `director_message` | 导演消息表 |

### 线索树设计
线索采用树形结构，支持多级线索嵌套。每条线索可设置：
- 类型：文字、图片、音频、视频
- 解锁密码：玩家需输入正确密码才能查看内容
- 公开/私有：公开线索自动发放给所有玩家
- 指定角色：仅特定角色可见

## 🚀 快速开始

### 1. 数据库初始化

```bash
# 连接 MySQL 并执行初始化脚本
mysql -u root -p < database/init.sql
```

默认账号密码：
- 管理员：`admin` / `123456`
- 导演：`director` / `123456`

预置示例数据：
- 剧本：《古宅惊魂》、《星际迷航》
- 角色：4个角色（古宅惊魂）
- 线索：5条线索（含多级、密码解锁）

### 2. 后端启动

```bash
cd backend

# 修改数据库配置（如需要）
# 编辑 src/main/resources/application.yml

# 编译运行
mvn spring-boot:run
```

后端服务地址: `http://localhost:8080/api`

### 3. 导演端访问

直接在浏览器打开: `frontend/director/index.html`

或使用本地服务器（推荐）：

```bash
# 使用 Python 启动简单 HTTP 服务器
cd frontend/director
python3 -m http.server 8081

# 或使用 Node.js
npx serve frontend/director -l 8081
```

访问地址: `http://localhost:8081`

### 4. 玩家端访问

直接在手机浏览器打开: `frontend/player/index.html`

或使用本地服务器：

```bash
cd frontend/player
python3 -m http.server 8082
```

访问地址: `http://localhost:8082`

## 📱 功能说明

### 导演端功能

1. **登录认证**
   - 账号密码登录
   - JWT Token 鉴权

2. **控制台概览**
   - 数据统计（剧本数、场次、在线玩家）
   - 快速创建场次
   - 最近场次列表

3. **剧本管理**
   - 剧本列表（分页、搜索）
   - 剧本详情（角色列表、线索树）
   - 角色剧本查看

4. **场次管理**
   - 创建/开始/暂停/恢复/结束场次
   - 房间码自动生成
   - 场次列表管理

5. **场次控制台（核心）**
   - 玩家管理：查看在线玩家、分配角色
   - 线索分发：选择线索树节点，分发给指定玩家
   - 消息推送：向全体或指定玩家发送剧情提示、系统消息
   - 实时刷新：玩家状态、消息自动刷新

### 玩家端功能

1. **加入游戏**
   - 输入房间码 + 昵称加入
   - 自动获取公开线索

2. **首页大厅**
   - 剧本信息展示
   - 角色信息卡片
   - 游戏进度统计
   - 快捷功能入口

3. **个人剧本**
   - 角色信息展示
   - 完整剧本内容阅读

4. **线索库**
   - 线索列表（分类展示）
   - 密码解锁机制
   - 线索详情查看
   - 图片线索预览

5. **消息中心**
   - 导演推送消息
   - 消息分类（系统/剧情/线索）
   - 自动刷新

## 🔌 API 接口

### 认证接口
- `POST /api/auth/login` - 导演登录

### 导演端 - 剧本管理
- `GET /api/director/script/page` - 剧本分页列表
- `GET /api/director/script/{id}` - 剧本详情
- `GET /api/director/script/{scriptId}/roles` - 角色列表
- `GET /api/director/script/{scriptId}/clue-tree` - 线索树
- `POST /api/director/script` - 新增剧本
- `PUT /api/director/script` - 更新剧本
- `DELETE /api/director/script/{id}` - 删除剧本

### 导演端 - 线索管理
- `GET /api/director/clue/script/{scriptId}` - 线索列表
- `GET /api/director/clue/{id}` - 线索详情
- `POST /api/director/clue` - 新增线索
- `PUT /api/director/clue` - 更新线索
- `DELETE /api/director/clue/{id}` - 删除线索

### 导演端 - 场次管理
- `GET /api/director/session/page` - 场次分页列表
- `GET /api/director/session/{id}` - 场次详情
- `POST /api/director/session/create` - 创建场次
- `POST /api/director/session/{id}/start` - 开始游戏
- `POST /api/director/session/{id}/pause` - 暂停游戏
- `POST /api/director/session/{id}/resume` - 恢复游戏
- `POST /api/director/session/{id}/end` - 结束游戏
- `GET /api/director/session/{sessionId}/players` - 玩家列表
- `POST /api/director/session/{sessionId}/player/{playerId}/assign-role` - 分配角色
- `POST /api/director/session/distribute-clue` - 分发线索
- `POST /api/director/session/send-message` - 发送消息
- `GET /api/director/session/{sessionId}/messages` - 消息列表

### 玩家端接口
- `POST /api/player/join` - 加入游戏
- `GET /api/player/session/info` - 获取场次信息
- `GET /api/player/script` - 获取我的剧本
- `GET /api/player/clues` - 获取我的线索
- `GET /api/player/clue/{clueId}` - 线索详情
- `POST /api/player/clue/unlock` - 解锁线索
- `GET /api/player/messages` - 消息列表
- `GET /api/player/progress` - 获取进度
- `POST /api/player/progress` - 更新进度
- `POST /api/player/heartbeat` - 心跳

## 🎮 示例玩法流程

1. **导演准备**
   - 登录导演后台
   - 选择剧本，创建场次
   - 生成房间码

2. **玩家加入**
   - 玩家打开玩家端
   - 输入房间码和昵称
   - 加入游戏

3. **开始游戏**
   - 导演分配角色
   - 导演开始游戏
   - 玩家查看自己的角色剧本

4. **探索线索**
   - 导演向玩家分发线索
   - 玩家查看线索
   - 需要密码的线索输入密码解锁
   - 解锁后获得更深入的线索

5. **剧情推进**
   - 导演通过消息推送剧情提示
   - 玩家根据线索推理
   - 逐步揭开真相

6. **游戏结束**
   - 导演结束游戏
   - 公布真相

## 🔧 配置说明

### 后端配置 (application.yml)

```yaml
server:
  port: 8080                  # 服务端口

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/script_kill  # 数据库地址
    username: root            # 数据库用户名
    password: root            # 数据库密码

scriptkill:
  jwt:
    secret: script-kill-secret-key-2024   # JWT 密钥
    expire: 86400              # Token 有效期（秒）
```

### 前端 API 地址配置

修改对应目录下的 `js/api.js`：

```javascript
const API_BASE = 'http://localhost:8080/api';
```

## 📝 开发计划

- [x] 数据库设计与初始化脚本
- [x] 后端基础架构搭建
- [x] 剧本管理模块
- [x] 线索树管理模块
- [x] 游戏场次管理
- [x] 导演控制台（玩家管理、线索分发、消息推送）
- [x] 玩家端 H5 页面
- [x] 线索密码解锁机制
- [ ] WebSocket 实时通讯（当前为轮询）
- [ ] 音频/视频线索播放
- [ ] 投票指凶功能
- [ ] 剧本编辑器可视化
- [ ] 数据统计分析

## 📄 License

MIT License
