# OnboardAgent Android — 开发交接文档

## 当前状态概述

| 模块 | 状态 | 说明 |
|------|------|------|
| 项目骨架 | ✅ 完成 | Gradle、依赖、AndroidManifest、权限 |
| 数据层 (data/) | ✅ 完成 | 全部 6 个 API 接口 + 6 个 Repository + DTO |
| WebSocket | ✅ 完成 | ChatWebSocket 连接/重连/消息解析 |
| 工具类 (util/) | ✅ 完成 | Token、拦截器、服务器配置、错误解析 |
| 认证 UI | ✅ 完成 | 登录、注册、客户端校验、服务器错误展示 |
| 导航 | ✅ 完成 | Navigation Compose（登录 ↔ 注册 ↔ 聊天） |
| 聊天骨架 | ✅ 完成 | Markdown 渲染、思考过程、来源引用、RAG 评估、文档范围栏 |
| 知识库 UI | ✅ 完成 | KnowledgeSheet（上传/列表/删除/摘要/知识卡片） |
| AI 记忆 UI | ✅ 完成 | MemorySheet（开关/回答风格/语气/画像/统计） |
| 个人资料编辑 | ✅ 完成 | ProfileEditScreen（用户名/邮箱/部门/岗位） |
| 打磨 | ✅ 完成 | 溢出菜单/长按复制/刷新按钮/Release 签名/markdown 渲染优化 |

---

## 已完成功能详解

### 1. 数据层 (data/)

**API 接口** (`data/api/`)：6 个 Retrofit 接口，覆盖后端全部 19 个端点：

| 文件 | 端点 |
|------|------|
| `AuthApi.kt` | POST login, POST register, GET me, PUT me |
| `ChatApi.kt` | POST /api/chat/ask (HTTP fallback) |
| `ConversationApi.kt` | GET list, GET detail, DELETE |
| `FileApi.kt` | POST upload (multipart), GET list, DELETE |
| `ToolApi.kt` | POST summarize, POST extract-knowledge |
| `MemoryApi.kt` | GET memory, PATCH memory, DELETE memory |

**DTO** (`data/dto/`)：所有请求/响应数据类，使用 `@SerializedName` 映射后端 snake_case 字段。

**WebSocket** (`data/websocket/`)：
- `WsMessage.kt`：sealed class 定义 5 种消息类型（Meta/Thinking/Token/Done/Error）
- `ChatWebSocket.kt`：OkHttp WebSocket 封装，支持自动重连（最多 5 次，指数退避）

**Repository** (`data/repository/`)：6 个仓库，薄封装层。`ChatRepository` 比较关键，实现了 WS 优先 + HTTP 回退策略。

### 2. 工具类 (util/)

| 文件 | 功能 |
|------|------|
| `TokenManager.kt` | EncryptedSharedPreferences 存储/读取 JWT |
| `AuthInterceptor.kt` | OkHttp 拦截器，自动附加 `Authorization: Bearer` |
| `ServerConfig.kt` | 服务器地址持久化配置 |
| `ErrorParser.kt` | 解析 FastAPI 422/400 错误为中文提示 |
| `FileUtil.kt` | 文件大小格式化、日期格式化 |

### 3. 认证 (ui/auth/)

- `LoginScreen.kt`：用户名/密码输入、密码显隐切换、⚙️ 服务器配置弹窗
- `RegisterScreen.kt`：5 字段注册表单（用户名/邮箱/密码/部门/岗位）
- `AuthViewModel.kt`：登录/注册/获取用户/登出，含客户端校验（用户名≥3、密码≥6、邮箱格式）
- 服务器地址可在登录页右上角齿轮图标配置，持久化到 SharedPreferences

### 4. 导航 (ui/navigation/)

`AppNavGraph.kt`：3 个路由（login / register / chat），根据 JWT 状态自动选择起始页。

### 5. 聊天骨架 (ui/chat/)

- `ChatViewModel.kt`：核心 ViewModel，管理对话列表/消息/流式状态/文档范围
  - `sendMessage()` → 先尝试 WS，失败自动回退 HTTP
  - `handleWsMessage()` → 处理 Meta/Thinking/Token/Done/Error 五种消息
  - `isStreaming` 标志追踪流式状态，Done/Error 后置为 false
  - 文档计数同步到 `ChatUiState`（`totalDocumentCount` / `selectedDocCount`）
- `ChatScreen.kt`：ModalNavigationDrawer 布局 + TopAppBar + 消息列表 + 错误 Snackbar
  - `MessageBubble`：用户消息纯文本右对齐，assistant 流式时纯文本、完成后 Markdown（`MarkdownDocument`）
  - 每条完成的 assistant 消息下方显示 ThinkingSteps / SourcesPanel / EvaluationPanel 可折叠面板
  - 流式过程中显示实时思考步骤或加载指示器
- `components/ConversationDrawer.kt`：侧边栏对话列表 + 删除确认对话框
- `components/InputBar.kt`：多行输入框 + 发送按钮
- `components/ThinkingSteps.kt` 🆕：可折叠 Agent 思考过程面板，含工具名中文推断和耗时
- `components/SourcesPanel.kt` 🆕：可折叠来源引用面板，含可信度 chip 和文本预览
- `components/EvaluationPanel.kt` 🆕：可折叠 RAG 评价面板，含 6 指标网格 + 检索/生成详情
- `components/WelcomeCards.kt` 🆕：欢迎页 6 个预设问题 AssistChip 网格
- `components/DocumentScopeBar.kt` 🆕：输入框上方文档范围状态栏

### 6. DI 容器 (AppContainer.kt)

手动依赖注入，按需创建 Retrofit 实例和 Repository。URL 变更后自动重建。

---

## 待完成功能

### Block 1：聊天 UI 完善（优先级：高）✅ 已完成

#### 1.1 Markdown 消息渲染 ✅

已实现。`MessageBubble` 中对完成的 assistant 消息使用 `MarkdownDocument`（boswelja compose-markdown material3）渲染。流式过程中使用纯文本，`Done` 后切换为 Markdown，避免闪烁。

**用到的 API**：`com.boswelja.markdown.material3.MarkdownDocument(markdown = content)`

#### 1.2 Agent 思考过程 (ThinkingSteps) ✅

**文件**：`ui/chat/components/ThinkingSteps.kt`
- 可折叠卡片，标题"Agent 思考过程 (N步)"
- 每步显示：编号徽章、图标、工具名（中文推断）、描述、耗时
- 支持已完成步骤（`List<AgentStepDto>`）和流式步骤（`List<AgentStepMsg>`）
- 工具名推断函数：`inferStepTool()` — 匹配"检索/生成/分析/摘要/提取"等关键词

#### 1.3 来源引用 (SourcesPanel) ✅

**文件**：`ui/chat/components/SourcesPanel.kt`
- 可折叠面板，标题"来源引用 (N)" + 文档数量
- 每个 source 显示：文档名、证据 ID、来源类型、可信度 chip、相似度分数、文本预览(前150字)

#### 1.4 RAG 评估指标 (EvaluationPanel) ✅

**文件**：`ui/chat/components/EvaluationPanel.kt`
- 可折叠面板，标题"RAG 评价指标" + 总分徽章
- 风险等级 banner（低/中/高风险，颜色编码）
- 6 指标网格（检索质量/证据支撑/引用覆盖/引用正确/幻觉风险/上下文重叠）
- 检索详情（Top-K/命中/片段数/最佳相关/平均相关/文档多样性）
- 生成详情（声明数/有支撑/无支撑/有效引用/无效引用/来源利用）
- 备注列表

#### 1.5 欢迎卡片 (WelcomeCards) ✅

**文件**：`ui/chat/components/WelcomeCards.kt` — 已从 ChatScreen 提取，包含 6 个预设问题 AssistChip 的 2 列网格。

#### 1.6 流式标记渲染优化 ✅

已实现。`MessageUiItem.isStreaming` 标志在流式过程中为 `true`，`Done` 后为 `false`。`MessageBubble` 根据此标志选择纯文本或 Markdown 渲染，避免 recompose 闪烁。

---

### Block 2：知识库 UI（优先级：高）✅ 已完成

#### 2.1 KnowledgeSheet（文件列表 + 上传）✅

**文件**：`ui/knowledge/KnowledgeSheet.kt`
- ModalBottomSheet 底部弹出
- 文件列表：每项显示文件名、大小、分块数、状态标签（已就绪/处理中/失败）
- 上传按钮：调用系统文件选择器（`*/*` MIME）
- 删除：长按 + 确认对话框
- 操作菜单：制度速览、知识卡片（点击展开/收起）
- 空状态页面：图标 + 提示文案

**文件**：`ui/knowledge/KnowledgeViewModel.kt`
- `loadDocuments()` — 调用 `FileRepository.list()`
- `uploadFile(uri: Uri)` — 调用 `FileRepository.upload()`
- `deleteFile(id: Long)` — 调用 `FileRepository.delete()`
- `summarize()` / `extractKnowledge()` — 调用 `ToolRepository`
- 刷新文档列表自动同步 `ChatUiState`

#### 2.2 DocumentScopeBar（文档范围选择）✅

已接入 KnowledgeSheet，点击自动弹出。

#### 2.3 SummaryDialog（制度速览）✅

**文件**：`ui/knowledge/SummaryDialog.kt`
- AlertDialog，Markdown 渲染摘要
- 底部"复制"按钮 → `ClipboardManager`
- 加载状态指示器

#### 2.4 KnowledgeCardsDialog（知识卡片）✅

**文件**：`ui/knowledge/KnowledgeCardsDialog.kt`
- AlertDialog，按 category 分组展示
- 每个卡片：标题、描述、要点、示例、来源摘录
- 点击卡片 → 关闭弹窗 → 自动发送追问
- "导出 Markdown"按钮 → 复制到剪贴板

---

### Block 3：AI 记忆 UI（优先级：中）✅ 已完成

#### 3.1 MemorySheet ✅

**文件**：`ui/chat/memory/MemorySheet.kt`
- ModalBottomSheet，标题"AI 记忆" + 副标题"个性化使用画像"
- 统计区域：问题计数、记忆启用/禁用状态
- 设置区域：开关（启用/禁用）+ 回答风格选择（ExposedDropdownMenu：详细全面/简洁精炼/分点概括/引导式）+ 沟通语气选择（正式专业/轻松友好/鼓励激励/严谨学术）
- 画像区域：部门 chip、岗位 chip、热门话题标签、最近问题、常用文档列表
- "保存记忆设置"按钮 + "清空 AI 记忆"按钮（带确认对话框）

**文件**：`ui/chat/memory/MemoryViewModel.kt`
- `loadMemory()` — `MemoryRepository.getMemory()`
- `saveSettings()` — `MemoryRepository.updateMemory()`
- `clearMemory()` — `MemoryRepository.clearMemory()`

**接入**：ChatScreen TopAppBar 溢出菜单 → "AI 记忆"

---

### Block 4：个人资料编辑（优先级：中）✅ 已完成

#### 4.1 ProfileEditScreen ✅

**文件**：`ui/auth/ProfileEditScreen.kt`
- 编辑表单：用户名、邮箱、部门、岗位
- 头像占位区
- 客户端校验（用户名≥3、邮箱格式）
- 调用 `AuthRepository.updateProfile()` → `UpdateProfileRequest`
- 回到前页自动刷新

**AuthViewModel 扩展**：新增 `updateProfile()` 方法和 `isProfileUpdated` 状态
**导航**：`AppNavGraph` 新增 `profile` 路由
**接入**：ChatScreen TopAppBar 溢出菜单 → "个人资料"

---

### Block 5：打磨（优先级：低）✅ 已完成

#### 5.1 对话列表刷新 ✅
- `ConversationDrawerContent` 新增刷新按钮（Refresh 图标）
- 点击重新加载对话列表

#### 5.2 长按复制 ✅
- `MessageBubble` 添加 `combinedClickable` + `onLongClick`
- 长按 → `ClipboardManager` 复制 → Toast 提示"已复制到剪贴板"

#### 5.3 错误处理完善 ✅
- 错误消息通过 Snackbar 展示
- 知识库空状态页面（图标 + 提示文案）
- 对话空状态页面（暂无对话提示）

#### 5.4 Release 签名 ✅
- `app/build.gradle.kts` 已添加 Release signingConfigs 模板（注释状态，使用环境变量）
- Proguard 已启用（`proguard-android-optimize.txt`）
- Release 构建时需取消注释 signingConfig 并替换 keystore 路径

#### 5.5 UI 打磨 ✅
- TopAppBar 使用溢出菜单（MoreVert），避免标题被挤压
- 知识库文件列表支持展开/收起操作按钮

---

## 新增文件清单

| 文件 | 所属 Block |
|------|-----------|
| `ui/knowledge/KnowledgeViewModel.kt` | Block 2 |
| `ui/knowledge/KnowledgeSheet.kt` | Block 2 |
| `ui/knowledge/SummaryDialog.kt` | Block 2 |
| `ui/knowledge/KnowledgeCardsDialog.kt` | Block 2 |
| `ui/chat/memory/MemoryViewModel.kt` | Block 3 |
| `ui/chat/memory/MemorySheet.kt` | Block 3 |
| `ui/auth/ProfileEditScreen.kt` | Block 4 |

## 修改文件清单

| 文件 | 改动说明 |
|------|---------|
| `ChatScreen.kt` | 接入 KnowledgeSheet + MemorySheet + 长按复制 + 溢出菜单 |
| `ChatViewModel.kt` | 无改动 |
| `ConversationDrawer.kt` | 新增刷新按钮 |
| `AuthViewModel.kt` | 新增 `updateProfile()` + `isProfileUpdated` |
| `AppNavGraph.kt` | 新增 `profile` 路由 |
| `MainActivity.kt` | 添加 `systemBarsPadding()` + `imePadding()` 修复键盘遮挡 |
| `app/build.gradle.kts` | Release 签名配置模板 + Proguard

---

## 后端 API 参考

后端项目：`/Users/db/Documents/Project/edu-assistant`

### 环境变量 (.env)

```bash
LLM_PROVIDER=deepseek          # 或 siliconflow
DEEPSEEK_API_KEY=sk-xxx
DEEPSEEK_BASE_URL=https://api.deepseek.com/v1
DEEPSEEK_MODEL=deepseek-chat
```

### 启动后端

```bash
cd /path/to/edu-assistant
python start.py backend          # 启动在 :8000
```

### 完整 API 列表

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| POST | `/api/auth/register` | 否 | 注册 |
| POST | `/api/auth/login` | 否 | 登录 |
| GET | `/api/auth/me` | JWT | 获取用户 |
| PUT | `/api/auth/me` | JWT | 更新资料 |
| POST | `/api/chat/ask` | JWT | HTTP 同步问答 |
| WS | `/ws/chat/{user_id}` | 路径 | WebSocket 流式 |
| GET | `/api/conversations` | JWT | 对话列表 |
| GET | `/api/conversations/{id}` | JWT | 对话详情 |
| DELETE | `/api/conversations/{id}` | JWT | 删除对话 |
| POST | `/api/files/upload` | JWT | 上传文件 |
| GET | `/api/files` | JWT | 文件列表 |
| DELETE | `/api/files/{id}` | JWT | 删除文件 |
| POST | `/api/tools/summarize` | JWT | 制度速览 |
| POST | `/api/tools/extract-knowledge` | JWT | 知识卡片 |
| GET | `/api/memory/me` | JWT | 获取记忆 |
| PATCH | `/api/memory/me` | JWT | 更新记忆 |
| DELETE | `/api/memory/me` | JWT | 清空记忆 |

### WebSocket 消息协议

客户端 → 服务器：
```json
{"message": "...", "conversation_id": null, "agent_type": "edu", "selected_document_ids": null}
```

服务器 → 客户端（按顺序）：
1. `{"type": "meta", "conversation_id": 1}`
2. `{"type": "thinking", "step": "...", "text": "...", "tool_name": "...", "elapsed_ms": 123}`（0-N 次）
3. `{"type": "token", "content": "..."}`（0-N 次）
4. `{"type": "done", "sources": [...], "agent_steps": [...], "evaluation": {...}}`
5. `{"type": "error", "message": "..."}`（出错时）

---

## 如何构建和测试

### 调试版本

```bash
./gradlew assembleDebug
# APK 输出：app/build/outputs/apk/debug/app-debug.apk
```

### 真机测试

1. 确保手机和电脑在同一 Wi-Fi
2. 电脑上启动后端：`python start.py backend`
3. 手机上打开 App → ⚙️ → 输入 `http://<电脑IP>:8000`
4. 注册新账号或登录

### 模拟器测试

模拟器默认使用 `http://10.0.2.2:8000`（自动映射到 host 的 localhost），无需修改地址。

---

## 注意事项

1. **不要引入 Hilt/KSP**：AGP 9.x + Kotlin 2.2.x 的 KSP 版本尚未发布，当前手动 DI 正常工作，不要尝试迁移
2. **Retrofit baseUrl 必须以 / 结尾**：`AppContainer` 已自动处理
3. **ServerConfig 默认地址**：已改为 `http://192.168.100.134:8000`（电脑热点 IP），如需切回模拟器改为 `http://10.0.2.2:8000`
4. **WebSocket 认证**：当前通过 URL 路径 `user_id` 识别用户（无 Token 校验），这是后端设计，不要改
5. **OkHttp logging**：release 构建时记得把 `HttpLoggingInterceptor.Level.BODY` 改为 `NONE`
6. **Proguard/R8**：已配置 `proguard-android-optimize.txt`，keep rules 见 `app/src/main/keepRules/rules.keep`
