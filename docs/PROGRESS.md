# CorpKnow Compass Android — 开发进度总结

> 最后更新：2026-07-08 · **全部功能已完成** 🎉

## 总体进度

**43 个 Kotlin 源文件** · 构建状态：✅ `BUILD SUCCESSFUL` · 真机连通：✅ 已验证

| 模块 | 状态 | 文件数 |
|------|------|--------|
| 数据层（API + DTO + Repository） | ✅ 完成 | 13 |
| WebSocket | ✅ 完成 | 2 |
| 工具类（Token、配置、拦截器） | ✅ 完成 | 5 |
| 认证 UI（登录/注册/个人资料） | ✅ 完成 | 4 |
| 导航 | ✅ 完成 | 1 |
| 聊天 UI（含 Markdown/思考/来源/评估） | ✅ 完成 | 10 |
| 知识库 UI（上传/列表/速览/卡片） | ✅ 完成 | 3 |
| AI 记忆 UI | ✅ 完成 | 2 |
| 主题 | ✅ 完成 | 3 |
| 打磨（菜单/长按复制/刷新/签名） | ✅ 完成 | — |

---

## 已完成详情

### 1. 项目骨架

- **AGP 9.2.1 + Kotlin 2.2.10 + Jetpack Compose + Material 3**
- 手动 DI（`AppContainer`），AGP 9.x 内置 Kotlin 不支持 kapt/Hilt
- Android 8.0+（API 26），`INTERNET` + `ACCESS_NETWORK_STATE` 权限
- `android:usesCleartextTraffic="true"` 允许 HTTP 明文

### 2. 数据层（data/）

**API 接口** — 6 个 Retrofit 接口，覆盖后端全部 19 个端点：

| 接口 | 端点 | 说明 |
|------|------|------|
| `AuthApi` | POST login/register, GET/PUT me | 认证 + 个人资料 |
| `ChatApi` | POST /api/chat/ask | HTTP 回退问答 |
| `ConversationApi` | GET list/{id}, DELETE | 对话管理 |
| `FileApi` | POST upload, GET list, DELETE | 文件管理 |
| `ToolApi` | POST summarize/extract-knowledge | 制度速览/知识卡片 |
| `MemoryApi` | GET/PATCH/DELETE memory/me | AI 记忆 |

**DTO** — 7 个数据类文件，全部使用 `@SerializedName` 映射后端 snake_case。

**Repository** — 6 个仓库，使用 **lambda 延迟求值** 获取 API 实例，确保齿轮修改 URL 后立即生效。

**WebSocket**：
- `WsMessage` — sealed class，5 种消息（Meta/Thinking/Token/Done/Error）
- `ChatWebSocket` — OkHttp WebSocket 封装，自动重连（最多 5 次，指数退避 2s×N）

### 3. 工具类（util/）

| 类 | 功能 |
|------|------|
| `TokenManager` | EncryptedSharedPreferences（AES256）存储 JWT |
| `AuthInterceptor` | OkHttp 拦截器，自动附加 `Authorization: Bearer` |
| `ServerConfig` | SharedPreferences 持久化服务器 URL |
| `ErrorParser` | 解析 FastAPI 422 校验错误和 400 错误为中文消息 |
| `FileUtil` | 文件大小格式化、ISO 日期格式化 |

### 4. 认证 UI（ui/auth/）

| 文件 | 功能 |
|------|------|
| `LoginScreen.kt` | 用户名/密码输入、密码显隐切换、⚙️ 服务器地址配置弹窗、注册跳转 |
| `RegisterScreen.kt` | 5 字段表单（用户名/邮箱/密码/部门/岗位），客户端校验（≥3字符/含@/≥6位） |
| `ProfileEditScreen.kt` | 编辑个人资料（用户名/邮箱/部门/岗位），客户端校验，自动刷新 |
| `AuthViewModel.kt` | 登录/注册/获取用户/更新资料/登出，`ErrorParser` 中文错误展示 |

### 5. 导航（ui/navigation/）

`AppNavGraph` — 4 路由（login/register/chat/profile），LaunchedEffect 触发导航（避免闪烁 bug），JWT 自动选起始页，profile 路由支持返回刷新。

### 6. 聊天 UI（ui/chat/）

| 组件 | 文件 | 功能 |
|------|------|------|
| **ChatScreen** | `ChatScreen.kt` | ModalNavigationDrawer 布局、溢出菜单（个人资料/AI记忆/退出）、错误 Snackbar、KnowledgeSheet + MemorySheet 集成 |
| **MessageBubble** | 内联 | 用户消息纯文本右对齐；assistant 流式时纯文本、完成后 Markdown（`MarkdownDocument`）；长按复制到剪贴板 |
| **ChatViewModel** | `ChatViewModel.kt` | WS 优先 + HTTP 回退、`isStreaming` 追踪、文档计数同步 UI 状态 |
| **ConversationDrawer** | `components/` | 侧边栏对话列表、新建/切换/删除 + 刷新按钮 |
| **InputBar** | `components/` | 多行输入框（1-4行）+ 发送按钮 |
| **ThinkingSteps** | `components/` | 可折叠 Agent 思考过程面板，工具名中文推断 + 耗时 |
| **SourcesPanel** | `components/` | 可折叠来源引用面板，可信度 chip、相似度分数、文本预览 |
| **EvaluationPanel** | `components/` | 可折叠 RAG 评估面板，风险等级、6 指标网格、检索/生成详情 |
| **WelcomeCards** | `components/` | 欢迎页 6 个预设问题 2 列网格 |
| **DocumentScopeBar** | `components/` | 输入框上方文档范围状态栏，点击弹出 KnowledgeSheet |

### 7. 知识库 UI（ui/knowledge/）

| 组件 | 功能 |
|------|------|
| `KnowledgeSheet.kt` | ModalBottomSheet：文件列表（文件名/大小/分块数/状态标签）、系统文件选择器上传、长按删除 + 确认、操作菜单（制度速览/知识卡片） |
| `KnowledgeViewModel.kt` | 上传/列表/删除/摘要/知识提取，刷新后自动同步 ChatUiState |
| `SummaryDialog.kt` | AlertDialog + Markdown 渲染摘要 + 复制按钮 |
| `KnowledgeCardsDialog.kt` | AlertDialog 按 category 分组、点击追问、导出 Markdown |

### 8. AI 记忆 UI（ui/chat/memory/）

| 组件 | 功能 |
|------|------|
| `MemorySheet.kt` | ModalBottomSheet：统计区（问题计数/启用状态）、设置区（开关 + 回答风格下拉 + 沟通语气下拉）、画像区（部门/岗位/话题/最近问题/常用文档）、保存 + 清空确认 |
| `MemoryViewModel.kt` | 加载/保存/清空记忆，调用 MemoryRepository |

### 9. 打磨

- 溢出菜单（MoreVert）整合个人资料/AI记忆/退出，避免标题挤压
- 消息长按复制 + Toast 提示
- 对话列表刷新按钮
- 知识库/对话空状态页面
- Release 签名配置（环境变量占位）+ ProGuard 启用
- 导航闪烁修复（LaunchedEffect 副作用）

### 10. DI 容器（AppContainer）

- `baseUrl` setter 自动补尾 `/`，getter 从 `ServerConfig` 读取
- 所有 API 接口和 Repository 为 `get()` 计算属性，URL 变更后自动重建
- Repository 使用 `{ api }` lambda 延迟获取 API 实例

### 11. 已修复的关键 Bug

| Bug | 修复 |
|-----|------|
| URL 修改不生效 | Repository 改为 lambda 延迟求值 `{ authApi }`，每次调用动态获取最新 Retrofit |
| 登录后页面闪烁 | 导航从 composition 直接调用改为 `LaunchedEffect` 副作用触发 |
| LaunchedEffect 崩溃循环 | `getCurrentUser()` 加 try-catch 兜底 |

---

## 部署说明

### 真机连接

1. 手机和服务器连同一网络
2. 登录页 ⚙️ → 输入服务器地址
   - Docker 部署（nginx 代理）：`http://<IP>`（默认 80 端口）
   - 直连后端：`http://<IP>:8000`
3. 注册/登录即可使用

### Linux Docker 部署场景

```
手机 → 热点 → Linux(192.168.x.x) → nginx容器:80 → backend容器:8000
```

### 构建

```bash
./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

---

## 技术债务

- 无测试用例（JUnit 依赖已配但未实现）
- Release 构建需关闭 `HttpLoggingInterceptor.Level.BODY`
- Release 签名需替换 keystore 路径（当前占位环境变量）
- 后续 Kotlin 2.2.x KSP 发布后可迁移到 Hilt（当前手动 DI 正常）
