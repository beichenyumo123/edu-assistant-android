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

### 6. Markdown 表格渲染 — 完整迭代记录

**类型**：混合（性能优化 / UI 优化 / Bug 修复 / 架构重构）

**文件**：
- `ui/chat/components/MarkdownComponents.kt`
- `ui/chat/ChatScreen.kt`
- `ui/knowledge/SummaryDialog.kt`

**问题起点**：mikepenz 0.38.0 默认表格在 LazyColumn 中滚动卡顿，无单元格边框，文字被截断（`maxLines=1, overflow=Ellipsis`）。

---

#### 迭代路线图

```
WebView HTML ──→ 纯 Compose 自建 ──→ 库默认 MarkdownTable ──→ 自定义 headerBlock/rowBlock
     │                    │                    │                        │
     │ ❌ CSS 不生效       │ ❌ 闪退              │ ✅ 不崩但丑             │ ✅ 对齐+边框
     │                    │                    │                        │
     └────────────────────┴────────────────────┴────────────────────────┘
                                                                        │
                                              ┌─────────────────────────┘
                                              ▼
                              IntrinsicSize.Max 自适应宽度 ──→ 嵌套 MarkdownElement
                                              │                        │
                                              │ ✅ 横向延展              │ ✅ 内联语法恢复
                                              │                        │
                                              ▼                        ▼
                              固定列宽 + bodySmall 密度 ──→ 双重解析 (Two-Pass)
                                              │                        │
                                              │ ✅ 信息密度翻倍          │ ✅ 表格 AST 完整
                                              └────────────────────────┘
```

---

#### 第一轮：WebView 路线（6a-6g，已废弃）

**尝试**：用 `HtmlGenerator` 将表格 AST 转为 HTML，`AndroidView` + `WebView` 渲染，CSS 控制样式。

**失败原因**：
- 部分设备/Android 版本上 WebView CSS 完全不生效 → 表格无边框、无横向滚动、文字挤压成单字竖排
- 高度测量依赖 `evaluateJavascript("document.body.scrollHeight")`，`onPageFinished` 回调时布局未完成，5 行以上表格底部截断
- 横向滑动与 `ModalNavigationDrawer` 手势冲突

**教训**：WebView 在不同设备和 WebView 实现间行为不一致，不适合作为 Compose 列表中的嵌入式组件。

---

#### 第二轮：纯 Compose 自建（6g-6h，已废弃）

**尝试**：完全从 AST 解析开始用 Compose Row/Column 构建表格，`buildMarkdownAnnotatedString` + `MarkdownBasicText` 渲染单元格。

**失败原因**：**闪退**。进程 `STARTED → ENDED`，logcat 无任何异常输出。推测 `MarkdownBasicText` 或 `buildMarkdownAnnotatedString` 在特定表格 AST 结构下触发 composition 层崩溃。替换为 `getUnescapedTextInNode` + `BasicText` 后仍闪退。

**教训**：不要越过库的 API 边界直接使用其内部 composable（`MarkdownBasicText`、`annotatorSettings`）——它们在库的 CompositionLocal 树外可能行为异常。

---

#### 第三轮：基于库骨架渐进叠加（6i-6j，部分保留）

**尝试**：回到库的 `MarkdownTable`，通过 `headerBlock`/`rowBlock` 回调只替换样式层。

**成果**（保留到最终版本）：
- Row `height(IntrinsicSize.Max)` → 同行单元格高度统一，横向边框闭合
- Cell `weight(1f).fillMaxHeight()` → 上下行列对齐
- `Surface0` 表头背景 + `Surface1` 单元格边框
- `MarkdownTableBasicText(maxLines=Int.MAX_VALUE)` → 自然换行不截断

**局限**：库按 `columnsCount × 160dp` 固定表宽，内容多的列被压缩换行，行高极大。

**演进**：废弃库的 `MarkdownTable`，自建 `Column(width(IntrinsicSize.Max))` 让内容驱动宽度，`Box(fillMaxWidth().horizontalScroll())` 做视口。加入斑马纹（偶数行 `Base@0.4`）。

---

#### 第四轮：信息密度优化（6j 末-6k，保留到最终版本）

- `weight(1f).widthIn(min, max)` 均分 → **固定列宽** `width(80/340/160dp)`，按列索引分配，第二列（长文本）获得 340dp 充分展开
- `bodyMedium` → **`bodySmall` + `lineHeight=18.sp`**，字体缩小一号
- `padding(12,8)` → **`padding(8,8)`**，内边距收紧
- `CenterStart` → **`TopStart`**，顶部对齐避免短内容垂直居中
- 单元格内联语法通过 `MarkdownElement` 逐子节点渲染恢复（`**加粗**`、`*斜体*`、`[链接]()`）

---

#### 第五轮：双重解析 — 终极修复（6k 末-6l，最终方案）

**问题**：`<br>` 在表格单元格内本应实现换行。第一次尝试用全局 `U+2028` 替换 `<br>`，期望它"在 Compose Text 中换行但不被 Markdown 解析器当行终止符"。**但 `org.jetbrains.markdown` 解析器仍然将 `U+2028` 视作物理换行符**，切断了 GFM 表格行 → 表格 AST 崩塌 → 边框消失、后续内容被错误解析为独立列表。

**方案 — 双重解析 (Two-Pass Parsing)**：

```
外层 (Pass 1): 原始文本 → Markdown 解析器
  ├─ <br> 原样保留
  ├─ GFM 表格 AST 完整 ✅
  └─ 每格通过 offset 提取 raw text
       │
       ├─ 局部 <br> → \n
       └─ 嵌套 Markdown() (Pass 2)  ← 独立渲染单元格
            ├─ **加粗** ✅
            ├─ - 列表 ✅
            ├─ 多行 <br> ✅
            └─ 不传自定义 components → 无递归风险
```

**关键设计决策**：
1. 全局**不替换** `<br>`：外层解析器看到的是合法的单行 GFM 表格
2. 单元格内局部替换 `<br>` → `\n`：只在嵌套 `Markdown()` 的作用域内生效
3. 嵌套 `Markdown()` 不传入 `catppuccinMarkdownComponents`：避免单元格中的嵌套表格触发无限递归
4. 删除 ChatScreen.kt / SummaryDialog.kt 的全局 `<br>` 预处理逻辑

---

#### 最终架构

```
catppuccinMarkdownComponents (val, 顶层单例)
  └─ table = { model -> CatppuccinMarkdownTable(model.content, model.node) }
       └─ Box(fillMaxWidth + horizontalScroll)          // 视口
            └─ Column(width(IntrinsicSize.Max))         // 内容驱动宽度
                 ├─ Row(height(IntrinsicSize.Max) + Surface0)  // 表头
                 │    └─ Box(width(col).fillMaxHeight.border)  // 单元格
                 │         └─ 局部 <br>→\n + 嵌套 Markdown()   // 双重解析
                 └─ Row(height(IntrinsicSize.Max) + 斑马纹)    // 数据行
                      └─ (同上)
```

**样式常量**：
| 属性 | 值 |
|---|---|
| 列宽 | col0=80dp, col1=340dp, col2+=160dp |
| 单元格边框 | `0.5dp Surface1` |
| 表外框 | `0.5dp Surface1 + RoundedCornerShape(4dp)` |
| 内边距 | `h=8dp, v=8dp` |
| 字体 | `bodySmall + lineHeight=18.sp` |
| 表头 | `Surface0` 背景 + `FontWeight.Bold` |
| 斑马纹 | 偶数行 `Base@0.4` |
| 对齐 | `TopStart` |

**关键教训**：
1. mikepenz 0.38.0 没有 `customTable`/`customTableHeader`/`customTableRow`/`customTableCell` API——只有顶层 `table` 覆盖点。所有定制必须通过替换 `table` 组件实现
2. 不要直接调用库的内部 composable（`MarkdownBasicText` 等）——它们依赖库的 CompositionLocal 树
3. `U+2028` 在 `org.jetbrains.markdown` 中等同于 `\n`——不能用来在表格内换行
4. 双重解析（外层保 AST + 内层嵌套渲染）是 GFM 表格内实现多行内容的唯一可靠方案
5. `adb logcat -s MarkdownTable:D` 可查看表格渲染日志（TAG: `MarkdownTable`）

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

### 9. MemorySheet UI 全面重构（Gemini 设计 + Catppuccin 配色）

**类型**：UI 重构

**文件**：`ui/chat/memory/MemorySheet.kt`

**说明**：

**9a. 悬浮面板设计**
- `ModalBottomSheet` 设为 `containerColor = Color.Transparent` + `dragHandle = null`，完全透明
- 自定义 `Column` 容器接管所有视觉：`clip(RoundedCornerShape(24.dp))` + `background(Base)` + `navigationBarsPadding()` + `imePadding()` + `padding(bottom = 12.dp)`
- 顶部 40×4dp 胶囊拖拽条（`Surface1`），右上角 `Close` 图标

**9b. Gemini 风格卡片布局**
- 设置卡片：`Surface0` 背景 + 16dp 圆角，Toggle → 下拉框 → 胶囊保存按钮（`RoundedCornerShape(50)`）
- 统计行：`Surface1` 细边框 16dp 圆角卡片，记录问题数 + 最近更新时间
- 所有记忆条目（常问主题/常用资料/最近问题）均用 `Surface0` 独立卡片 + 图标 + 圆角 12dp
- 基础画像：`Surface0` 卡片 + `HorizontalDivider` 分隔

**9c. 交互优化**
- 保存按钮仅在有未保存更改时启用（`hasChanges` 检测）
- Snackbar → **Toast** 反馈（保存成功/清空成功/错误）
- 下拉框封装 `StyledDropdown`：统一 `Base` 背景、`Surface1` 边框、`Text` 文字色
- 清空按钮改用 `OutlinedButton` + 红色胶囊 + 边框 + loading spinner
- `LazyColumn` + `spacedBy(20.dp)` + `PaddingValues(bottom = 20.dp)`

**9d. 图标语义化**
- 标题：`Icons.Filled.Memory`
- 常问主题：`Icons.Filled.AutoAwesome`
- 常用资料：`Icons.Filled.Description`
- 最近问题：`Icons.Filled.QuestionAnswer`
- 统计：`Icons.Filled.ChatBubbleOutline` + `Icons.Filled.Update`

### 8. 标题响应式排版 + Markdown heading 渲染适配

**类型**：UI 优化

**文件**：
- `ui/theme/Type.kt`
- `ui/chat/components/MarkdownComponents.kt`

**说明**：

**8a. 标题字号响应式收窄**
- headlineLarge: 32.sp → **24.sp**（小屏手机不挤压不换行）
- headlineMedium: 28.sp → **20.sp**
- headlineSmall: 24.sp → **17.sp**
- titleLarge: 22.sp → **18.sp**
- 所有 headline/title 级别显式设置 lineHeight，行间距紧凑不浪费空间

**8b. Markdown heading 渲染适配**
- 在 `MarkdownComponents.kt` 中新增 heading1–heading6 六个覆盖点
- 每个覆盖点显式引用 `MaterialTheme.typography.headlineLarge` 等样式
- 确保标题不使用写死的 `TextStyle`，全局字号配置一键生效

### 10. 自定义 APP 图标

**类型**：资源替换

**文件**：
- `res/mipmap-{mdpi,hdpi,xhdpi,xxhdpi,xxxhdpi}/ic_launcher.png`
- `res/mipmap-{mdpi,hdpi,xhdpi,xxhdpi,xxxhdpi}/ic_launcher_round.png`
- `res/mipmap-anydpi/ic_launcher.xml`（已删除）
- `res/mipmap-anydpi/ic_launcher_round.xml`（已删除）

**说明**：
- 将 Android Studio 默认的绿色机器人图标替换为自定义品牌图标（`图标.png`）
- 源文件 1056×1056 缩放为各密度桶标准尺寸：mdpi 48dp、hdpi 72dp、xhdpi 96dp、xxhdpi 144dp、xxxhdpi 192dp
- 移除 `mipmap-anydpi` 自适应图标 XML，统一使用光栅图标覆盖所有 API 级别
- `ic_launcher` 和 `ic_launcher_round` 使用同一图标

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
