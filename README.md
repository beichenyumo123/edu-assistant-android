# OnboardAgent Android

企业入职培训助手的 Android 原生客户端，基于 Kotlin + Jetpack Compose。

## 简介

OnboardAgent 是一个 RAG 驱动的企业入职培训助手，新员工可以上传公司手册、制度文档、培训材料，然后通过自然语言提问获取答案。本仓库是 Android 原生客户端，复刻 Web 前端的全部功能。

后端仓库：[edu-assistant](https://github.com/beichenyumo123/edu-assistant)

## 技术栈

| 层面 | 技术 |
|------|------|
| 语言 | Kotlin 2.2.10 |
| UI | Jetpack Compose + Material 3 |
| 架构 | MVVM + Repository（手动 DI） |
| 网络 | Retrofit 2.11 + OkHttp 4.12 |
| WebSocket | OkHttp WebSocket |
| 导航 | Navigation Compose 2.8 |
| Markdown | boswelja/compose-markdown 1.1.5 |
| 最低 SDK | Android 8.0 (API 26) |

## 依赖注入说明

AGP 9.x + Kotlin 2.2.x 的 KSP/Hilt 生态暂不兼容，当前使用**手动 AppContainer**（`AppContainer.kt`）。所有 Repository 和 API 接口通过 AppContainer 创建，ViewModel 通过 ViewModelProvider.Factory 注入。

后续 Kotlin 2.2.x 对应的 KSP 版本发布后，可以迁移到 Hilt。

## 项目结构

```
app/src/main/java/com/zxxf/assistant/
├── MainActivity.kt              # 唯一 Activity
├── AppContainer.kt              # 手动 DI 容器
├── data/
│   ├── api/                     # Retrofit API 接口（6 个）
│   ├── dto/                     # 数据传输对象（请求/响应）
│   ├── websocket/               # WebSocket 连接 + 消息类型
│   └── repository/              # 数据仓库层（6 个）
├── ui/
│   ├── navigation/              # 导航图
│   ├── theme/                   # Material3 主题
│   ├── auth/                    # 登录/注册
│   ├── chat/                    # 聊天主界面 + 组件
│   └── knowledge/               # 知识库管理（待实现）
└── util/                        # 工具类（Token、拦截器、配置）
```

## 快速开始

### 前提条件

- Android Studio 最新版
- JDK 17+
- 后端服务运行中（`http://<host>:8000`）

### 导入项目

1. 用 Android Studio 打开本目录
2. 等待 Gradle 同步完成
3. 选择 Run → Run 'app'

### 连接后端

- **模拟器**：默认使用 `http://10.0.2.2:8000`
- **真机**：登录页右上角 ⚙️ → 输入电脑局域网 IP + 端口（如 `http://192.168.1.100:8000`）

## 开发状态

当前完成了认证 + 导航 + 数据层 + 聊天骨架。详见 [docs/HANDOFF.md](docs/HANDOFF.md)。

## License

MIT
