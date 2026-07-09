# 变更日志

> 每次功能变动或 Bug 修复均在此文件中追加记录。

---

## 2026-07-09

### 1. Catppuccin Latte 全局主题配色

**类型**：功能优化

**文件**：
- `ui/theme/Color.kt`
- `ui/theme/Theme.kt`
- `ui/chat/components/EvaluationPanel.kt`

**说明**：
- 将应用主题从 Google Material 默认色板替换为 Catppuccin Latte（亮色） + Mocha（暗色）配色方案
- Color.kt 新增 26+26 个颜色常量，覆盖 Rosewater → Crust 全色系
- LightColorScheme 映射到 Latte（primary=Blue `#1e66f5`，background=Base `#eff1f5`，text=Text `#4c4f69`）
- DarkColorScheme 映射到 Mocha（primary=BlueDark `#89b4fa`，background=BaseDark `#1e1e2e`）
- 补充此前缺失的 `tertiary` 色槽（Mauve `#8839ef`），修复 KnowledgeSheet/SourcesPanel/MemorySheet 中 `tertiary` 引用无定义的问题
- 状态栏背景色改为跟随 `surface` 而非 `primary`，Catppuccin 风格更柔和
- 亮色模式下状态栏图标改为深色（`isAppearanceLightStatusBars = true`）
- EvaluationPanel 中旧颜色引用 `Green500/Orange500/Red500` → `Green/Peach/Red`

### 2. Markdown 流式渲染性能优化

**类型**：性能优化

**文件**：
- `ui/chat/ChatViewModel.kt`
- `ui/chat/ChatScreen.kt`

**说明**：
- 将流式内容从 `messages` 列表中分离为独立的 `streamingMessage: MessageUiItem?` 字段
- 流式期间仅更新 `streamingMessage.content`（O(1)），不再每 Token 复制整个消息列表（O(n)）
- 移除冗余的 `streamingContent: String` 字段
- LazyColumn 改为两段式：已完成消息（稳定 key）+ 独立 `item(key = "streaming")`，仅流式 item 随 Token 重组
- 滚动优化：流式期间用 `scrollToItem`（即时定位），Done 后用 `animateScrollToItem`（平滑动画）
- WebSocket Flow 添加 `.conflate()` 帧率同步，避免 Token 积压
- HTTP 回退路径同步适配新的 `streamingMessage` 模式

### 3. UI 深度打磨

**类型**：UI 优化

**文件**：
- `ui/chat/ChatScreen.kt`
- `ui/knowledge/SummaryDialog.kt`
- `ui/chat/components/InputBar.kt`
- `ui/theme/Theme.kt`

**说明**：

**3a. Markdown 代码块 Catppuccin 化**
- 代码块背景从 Material 深色默认 → Latte `Surface0`（`#ccd0da`）
- 圆角 8dp，内边距 12dp
- ChatScreen 和 SummaryDialog 两处 `MarkdownDocument` 调用均已更新（v1: boswelja `CodeBlockStyle`，v2: mikepenz `markdownColors`）

**3b. 聊天气泡层次感**
- 非对称圆角：用户气泡 `(16,16,16,4)` 右下角小，assistant `(16,16,4,16)` 左下角小
- 微阴影：`Modifier.shadow(1.dp, spotColor=Black@0.05)`
- 微描边：assistant 气泡 `BorderStroke(0.5.dp, Surface0)`
- 内边距增大：`padding(12.dp)` → `padding(horizontal=16.dp, vertical=12.dp)`

**3c. 胶囊输入框**
- TextField shape 改为 `RoundedCornerShape(24.dp)` 胶囊形
- 边框透明化：`focusedBorderColor/unfocusedBorderColor = Color.Transparent`
- 背景 `surfaceVariant`（Latte `Surface0`）
- 外层 Row 边距调整为 `horizontal=12.dp, vertical=8.dp`

**3d. 沉浸式导航栏**
- `window.navigationBarColor` 设为 `colorScheme.surface`，与状态栏一致
- `isAppearanceLightNavigationBars` 跟随亮/暗模式自动切换

### 4. Markdown 库迁移（boswelja → mikepenz）

**类型**：依赖变更

**文件**：
- `gradle/libs.versions.toml`
- `app/build.gradle.kts`
- `ui/chat/components/MarkdownComponents.kt`（新文件）
- `ui/chat/ChatScreen.kt`
- `ui/knowledge/SummaryDialog.kt`

**说明**：
- 从 `io.github.boswelja.markdown` (1.1.5) 迁移到 `com.mikepenz:multiplatform-markdown-renderer` (0.38.0)
- 原因：boswelja 完全不支持 GFM 表格渲染
- mikepenz 通过 `MarkdownComponents` API 暴露了可替换的表格组件（table/tableHeader/tableRow/tableCell）
- 新增 `MarkdownComponents.kt`：自定义 Catppuccin Latte 表格样式
  - 外框 + 内格线：`0.5dp Surface1` 边框
  - 表头：`Surface0` 背景 + 粗体 Text
  - 横向滚动：`Modifier.horizontalScroll()`
  - 斑马纹：偶数行 `Base@0.5` 底色
  - 圆角 6dp 外框
- API 变更：
  - `MarkdownDocument(markdown, codeBlockStyle)` → `Markdown(content, colors=markdownColor(codeBackground=...), components=...)`
  - `CodeBlockStyle` → `markdownColor(codeBackground)`（函数名为单数 `markdownColor`）
  - 新增 `catppuccinMarkdownComponents` 全局单例
- 兼容 Kotlin 2.2.10 + Compose BOM 2026.02.01
- 构建通过 ✅

#### 4.1 实施补充（2026-07-09）

经字节码逆向分析，mikepenz **0.38.0** 版 `MarkdownComponents` 接口仅有 20 个顶层组件覆盖点（text/eol/codeFence/codeBlock/heading1-6/setextHeading1-2/blockQuote/paragraph/orderedList/unorderedList/image/horizontalRule/table/checkbox/custom），**不支持** `tableHeader`/`tableRow`/`tableCell` 子组件覆盖。`tableHeader`/`tableRow`/`tableCell` 需 **mikepenz ≥ 0.39.x**，而 0.39.x 要求 **Kotlin ≥ 2.3.x**（当前为 2.2.10）。

当前状态：
- `MarkdownComponents.kt` 退化为占位文件（含 TODO 注释，待 Kotlin 升级后启用）
- 表格渲染使用 mikepenz 内置默认样式（优于 boswelja 完全不渲染）
- `markdownColor(codeBackground=Surface0)` 已接管代码块背景色
- ChatScreen 和 SummaryDialog 中未传入 `components` 参数

### 5. 底部输入区域深度重构

**类型**：UI 优化

**文件**：
- `ui/chat/components/InputBar.kt`
- `ui/chat/components/DocumentScopeBar.kt`
- `ui/chat/ChatScreen.kt`

**说明**：

**5a. Placeholder 换行修复**
- Placeholder Text 添加 `maxLines = 1` + `overflow = TextOverflow.Ellipsis`，颜色改为 `Subtext0`（`#6c6f85`）
- 移除外层 `Surface` 包装（`shadowElevation=8.dp`），去除冗余阴影
- TextField 容器背景改为 `Color.Transparent`，融入父容器
- `maxLines` 从 4 → 5

**5b. DocumentScopeBar 胶囊 Tag 化**
- 形状：`RoundedCornerShape(percent = 50)` 完全圆角
- 背景：`Surface0`（`#ccd0da`），文字/图标：`Blue`（`#1e66f5`）
- 边框：`BorderStroke(0.5.dp, Surface1)`
- 内边距：`horizontal=10.dp, vertical=4.dp`，图标缩小至 `14.dp`
- 移除全宽布局（`fillMaxWidth` → 自适应宽度）、ChevronRight 箭头、`tonalElevation`

**5c. 一体化输入容器**
- ChatScreen `bottomBar` 内用统一的 `Column` 容器包裹 DocumentScopeBar + InputBar
- 容器样式：`RoundedCornerShape(24.dp)` 大圆角 + `surface` 背景 + `Surface1` 细边框
- 外边距 `horizontal=12.dp, vertical=8.dp`（悬浮间距），内边距 `12.dp`
- InputBar 背景透明化后完美融入容器，无双重边框/阴影

### 6. Markdown 表格 — 滚动卡顿修复 + 边框增强

**类型**：性能优化 + UI 优化

**文件**：
- `ui/chat/components/MarkdownComponents.kt`
- `ui/chat/ChatScreen.kt`
- `ui/knowledge/SummaryDialog.kt`

**说明**：

**6a. 根因分析**
- mikepenz 0.38.0 默认 `MarkdownTable` 使用 `BoxWithConstraints` + `horizontalScroll`，当表格超过容器宽度（`tableCellWidth=160dp`，3 列 = 480dp > 气泡 340dp）时触发水平滚动
- `BoxWithConstraints` 产生子组合测量开销，`horizontalScroll` 与 `LazyColumn` 垂直滚动产生嵌套滚动冲突，导致卡顿
- 默认表格无单元格边框，仅依赖 `tableBackground`（alpha=0.02f），视觉上不明显

**6b. 自定义表格实现**
- 重写 `MarkdownComponents.kt`，提供 `catppuccinMarkdownComponents` 全局单例
- 自定义 `CatppuccinMarkdownTable` composable：
  - 去除 `BoxWithConstraints` + `horizontalScroll`，改用 `Column(Modifier.fillMaxWidth())`
  - 单元格 `Modifier.weight(1f)` 均匀分布宽度，无水平滚动条
  - 单元格 `border(0.5.dp, Surface1)` 可见边框
  - 表头 `Surface0` 背景 + 粗体
  - 外层 `RoundedCornerShape(8.dp)` 圆角边框 + `clip`
  - 使用 `buildMarkdownAnnotatedString` + `MarkdownBasicText` 保持 inline 格式支持
- ChatScreen 和 SummaryDialog 中 `Markdown` 调用传入 `components = catppuccinMarkdownComponents`

**6c. 滑入/滑出卡顿修复**
- 单元格 AnnotatedString 预计算 + `remember(node, content)` 缓存，避免滚动时重复 Markdown 解析
- 使用 `key()` 为每行提供稳定 composition 标识，Compose 可精确跳过未变更的行
- 移除 `height(IntrinsicSize.Min)`，消除昂贵的 intrinsic 测量

**6d. 单元格宽度 + 横向滚动**
- 单元格由 `weight(1f)` 改为固定 `width(dp)`，避免窄列导致 3 个汉字就换行
- 所有行的同一列使用统一宽度（取该列各单元格最大内容宽度，80-240dp 之间），确保列对齐
- 宽度估算：CJK 字符 2× 权重，~7dp/拉丁等效字符
- 外层 Box 添加 `horizontalScroll(rememberScrollState())`，表格超出气泡宽度时可左右滑动
- 单元格文字恢复 `softWrap = false, maxLines = 1, overflow = Ellipsis`

**6e. HTML 表格（WebView）替代 Compose 自定义表格**
- 用 `org.intellij.markdown.html.HtmlGenerator` 将表格 AST 转为 HTML，通过 `AndroidView` + `WebView` 渲染
- 浏览器引擎原生处理列宽计算、文字换行、横向滚动，无 Compose 布局开销
- Catppuccin CSS：表头 `Surface0` 背景、单元格 `Surface1` 边框、`#4c4f69` 文字色
- WebView 高度按 `rowCount * 30dp + 2dp` 估算
- 移除所有旧 Compose 表格代码（`CachedCell`/`CachedRow`/`RowType`/`MarkdownBasicText`/`buildMarkdownAnnotatedString`/AST 遍历/`key()`等）

**6f. WebView 表格 — 横向滑动 + 抽屉冲突 + 底部截断修复**
- 表格 CSS body 新增 `overflow-x: auto` + `-webkit-overflow-scrolling: touch`，table 改为 `width: max-content`，表宽超出视口时浏览器原生处理横向滚动
- 新增 `.pointerInput { detectHorizontalDragGestures { _, _ -> } }`，消费表格区域横向滑动事件，防止传播到 ModalNavigationDrawer 触发侧边栏
- WebView 新增 `overScrollMode = OVER_SCROLL_NEVER` + `isVerticalFadingEdgeEnabled = false`，消除边缘光晕效果
- 高度测量从 `onPageFinished` 内直接调用 `evaluateJavascript` 改为 `view.post {}` 延迟到首次 layout 完成后，追加 `postDelayed(300ms)` 二次保险（慢渲染表格场景）
- JS 高度测量回退链：`body.scrollHeight` → `documentElement.scrollHeight` → `firstElementChild.scrollHeight`
- 初始高度 120dp → 200dp

### 7. 对话历史 UI 深度重构（左侧抽屉）

**类型**：UI 重构

**文件**：
- `ui/chat/ChatScreen.kt`
- `ui/chat/components/ConversationDrawer.kt`

**说明**：

**7a. 抽屉容器优化**
- 保留 `ModalNavigationDrawer`（左侧滑出），抽屉内容包裹在 `ModalDrawerSheet` 中
- 抽屉面板背景色设为 `MaterialTheme.colorScheme.surface`（Latte Base `#eff1f5`），与整体主题一致

**7b. 胶囊"新建对话"按钮**
- 放置在抽屉顶部吸顶位置
- 外形：`RoundedCornerShape(percent = 50)` 完全胶囊形
- 背景：`Blue`（`#1e66f5`），文字 + 图标：`Color.White`
- 内部：`Icons.Filled.Add` + "新建对话" 文字，`PaddingValues(vertical = 14.dp)`
- 点击后创建新对话并自动关闭抽屉

**7d. 现代化对话 Item**
- 形状：`RoundedCornerShape(12.dp)` 圆角卡片
- 未选中：透明背景；选中：`Surface1.copy(alpha = 0.6f)` 半透明 Latte 表面色
- 布局：`ChatBubbleOutline` 图标（`Subtext0` / Primary 色）+ 标题（单行省略号，`Text` / Primary 色）+ 日期副标题（`labelSmall`，`Subtext0`）+ 右侧隐藏式删除按钮（`Overlay0`）
- `LazyColumn` 使用 `Arrangement.spacedBy(8.dp)` 垂直间距，内容区内边距 `horizontal = 16.dp, vertical = 12.dp`
- 提取独立 `ConversationItem` composable，提高可维护性
- 选中对话时自动关闭抽屉

**7e. 空状态**
- 无对话时居中显示 `ChatBubbleOutline` 大图标 + "暂无对话" 提示文字
- 图标透明度 0.5，视觉上柔和低调

**7f. 删除确认**
- AlertDialog 逻辑保持不变，移至面板底部

---

## 2026-07-08

### 初始版本

**说明**：
- Android 原生客户端初版完成
- 43 个 Kotlin 源文件
- 数据层 6 个 API + 6 个 Repository
- WebSocket 自动重连
- 认证（登录/注册/个人资料编辑）
- 聊天 UI（Markdown/思考过程/来源引用/RAG 评估）
- 知识库 UI（上传/列表/速览/卡片）
- AI 记忆 UI（面板/设置/画像）
- 打磨（溢出菜单/长按复制/刷新/Release 签名）
- 详见 PROGRESS.md 和 HANDOFF.md
