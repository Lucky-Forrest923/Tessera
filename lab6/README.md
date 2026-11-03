
# Lab 6：JavaFX 图形游戏大厅

本项目是基于 Lab 5 核心业务逻辑，使用 **JavaFX** 重构的图形界面（GUI）多游戏平台，支持 4 种游戏：

- `peace`：和平棋
- `reversi`：黑白棋 / 翻转棋
- `minesweeper`：扫雷
- `chess`：国际象棋

程序采用 **左-中-右三栏布局**，带分隔线：

- **左侧**：当前游戏棋盘（可交互点击）
- **中间**：游戏状态、信息、消息反馈
- **右侧**：游戏列表（点击切换）、新建游戏按钮

底部区域包含 **快捷控制按钮**（Pass / Flag / Quit）和 **指令输入栏**。

---

## 1. 环境要求

- **JDK 23** 或更高版本
- **Maven**
- JavaFX 23.0.2（Maven 自动下载）
- 支持图形窗口的桌面环境

---

## 2. 运行方式

在项目根目录执行：

```bash
mvn -q exec:java
```

主入口类是 `com.gameplatform.Main`。

---

## 3. 项目概览

项目采用 **MVC 架构 + SPI 插件式加载**：

- **Model**：核心业务逻辑（棋盘数据、游戏规则、回合管理）
- **View**：JavaFX 图形界面（JavaFXAPP.java）
- **Controller**：事件驱动控制器（GamePlatformController.java）
- **Plugin**：通过 SPI 自动注册各游戏插件

启动后默认创建 4 个对局：Peace、Reversi、Minesweeper、Chess 各一局。

---

## 4. 交互方式

支持 **鼠标交互** 和 **键盘指令输入** 两种操作方式。

### 4.1 鼠标操作

| 游戏 | 操作方式 |
|------|---------|
| **和平棋** | 左键点击空格落子 |
| **黑白棋** | 左键点击高亮（黄色）格子落子 |
| **扫雷** | 左键翻开格子；右键插旗/取消插旗 |
| **国际象棋** | 第一次点击选中己方棋子（绿色高亮），第二次点击目标格移动 |

### 4.2 快捷按钮

| 按钮 | 功能 | 适用游戏 |
|------|------|---------|
| `Pass` | 跳过当前回合 | 黑白棋（Reversi） |
| `Flag` | 切换插旗模式 | 扫雷（Minesweeper） |
| `Quit` | 退出程序 | 通用 |

### 4.3 指令输入

底部输入栏支持输入以下指令（按 Enter 或点击 Send 执行）：

| 命令 | 说明 | 示例 |
|------|------|------|
| `a1` 等坐标 | 落子 / 翻开格子 | `a1` |
| `f a1` / `flag a1` | 扫雷插旗 | `f a1` |
| `pass` | 黑白棋跳过 | `pass` |
| `数字` | 切换对局 | `2` |
| `s 数字` / `switch 数字` | 切换对局 | `s 3` |
| `游戏类型` | 新建对局 | `chess` |
| `quit` | 退出程序 | `quit` |

国际象棋走子输入：`m e2 e4`，升变可选：`m b2 c1 q`。

---

## 5. 游戏说明

### 5.1 和平棋（Peace）

- 8×8 棋盘，黑白双方轮流落子
- 只能下在空位，不翻子
- 棋盘被下满后结束

### 5.2 黑白棋（Reversi）

- 标准黑白棋规则
- 黄色高亮显示合法落子位置
- 无合法落子时可点击 Pass 按钮或输入 `pass`
- 双方无合法落子或棋盘满时结束，显示比分

### 5.3 扫雷（Minesweeper）

- 8×8 棋盘，10 颗地雷
- 首次点击位置保证安全
- 左键翻开，右键插旗
- 踩雷游戏结束

### 5.4 国际象棋（Chess）

- 标准国际象棋规则
- 支持兵的升变、吃过路兵、王车易位
- 两段式点击：先选己方棋子，再点击目标格移动
- 自动检测非法走法（王被将军等）

---

## 6. 多对局管理

- 启动时默认创建 4 个对局
- 右侧列表显示所有对局编号、类型、状态（`running` / `over`）
- 点击列表项可无缝切换，各局状态完整保留
- 点击右侧 `Peace` / `Reversi` / `Mine` / `Chess` 按钮可新建对局
- 也可在输入栏输入游戏类型名新建

---

## 7. 代码结构

```
src/main/java/com/gameplatform/
├── Main.java                          # 主入口
├── controller/
│   └── GamePlatformController.java    # 控制器
├── model/
│   ├── BaseGame.java                 # 游戏基类
│   ├── TurnBasedGame.java            # 回合制游戏接口
│   ├── HintProvider.java             # 提示接口（黑白棋合法落子高亮）
│   ├── MultiBoardManager.java        # 多对局管理器
│   └── Position.java                 # 坐标值对象
├── view/
│   └── JavaFXAPP.java                # JavaFX 图形界面（主视图）
├── renderer/
│   ├── GamePanelRenderer.java        # 渲染器接口
│   ├── CellClickContext.java         # 点击事件桥接
│   ├── DefaultGamePanelRenderer.java # 默认渲染器
│   ├── ChessRenderer.java            # 国际象棋渲染器
│   ├── MinesweeperRenderer.java      # 扫雷渲染器
│   └── OthelloStyleRenderer.java     # 黑白棋/和平棋渲染器
├── plugin/
│   ├── GamePlugin.java              # 插件接口
│   ├── GamePluginRegistry.java      # 插件注册器
│   ├── PeacePlugin.java
│   ├── ReversiPlugin.java
│   ├── MinesweeperPlugin.java
│   └── ChessPlugin.java
└── games/
    ├── othello/                      # 黑白棋/和平棋核心逻辑
    ├── minesweeper/                  # 扫雷核心逻辑
    └── chess/                        # 国际象棋核心逻辑
```

---

## 8. 架构要点

### 低耦合设计

- **业务逻辑层**与**视图层**完全解耦，游戏核心规则与 TUI/GUI 无关
- 通过 SPI 插件机制动态注册游戏，新增游戏只需实现 `GamePlugin` 接口
- `MultiBoardManager` 统一管理多对局，游戏切换时各局状态完整保持

### MVC 模式

- **Model**：`BaseGame` 及子类、`Board`、`MineField`、`ChessBoard` 等
- **View**：`JavaFXAPP` 负责 JavaFX 场景构建和渲染
- **Controller**：事件处理器内嵌在 JavaFXAPP 中，处理鼠标和键盘输入
