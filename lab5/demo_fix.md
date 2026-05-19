我来为你生成一份完整的技术文档，说明 Demo 模式的架构设计思路，方便你交给其他 AI 实现。

---

# Demo 模式技术设计文档

## 1. 设计目标

在现有插件化游戏平台中增加 Demo 模式，实现：
- 输入 `demo` 后系统自动按顺序演示所有默认游戏
- 每个游戏通过预设的合法命令序列展示核心玩法
- 演示过程中保持正常渲染，用户可随时中断
- **不破坏插件化架构**：新增游戏无需修改控制器代码

---

## 2. 核心设计原则

| 原则 | 说明 |
|------|------|
| **开闭原则** | 新增游戏时，Demo 调度逻辑自动适配，无需修改 `GameController` |
| **单一职责** | 控制器只负责"何时执行下一步"，游戏类负责"这一步走什么棋" |
| **最小侵入** | 不新增 Demo 专属作弊 API（如 `loadDemoLayout`、`forceWin`），所有演示通过正常 `processCommand` 完成 |
| **向后兼容** | 新增接口必须有默认实现，既有游戏不强制实现 |

---

## 3. 架构概览

```
┌─────────────────┐      ┌──────────────────┐
│   GameController │◄────│  MultiBoardManager│
│  (Demo 通用调度)  │      │   (现有，无改动)   │
└─────────────────┘      └──────────────────┘
         │
         │ 定时调用 processCommand(cmd)
         ▼
┌─────────────────────────────────────────┐
│            BaseGame (抽象类)             │
│  + default List<String> getDemoCommands()│
│  + default int getDemoStepDelayMs()      │
└─────────────────────────────────────────┘
         ▲
    ┌────┘
    │    各游戏覆写 getDemoCommands() 返回剧本
    ▼
PeaceGame ──► ["a1", "b1", "c1", ...]
ReversiGame ──► ["d3", "c3", "c4", "pass", ...]
Minesweeper ──► ["b2", "c2", "f c3", ...]
ChessGame ──► ["m e2 e4", "m e7 e5", ...]
```

---

## 4. 接口设计

### 4.1 BaseGame 新增方法

```java
/**
 * 返回本游戏的 Demo 自动演示命令序列。
 * 每条命令必须是可被 processCommand() 直接消费的合法输入。
 * 空列表表示本游戏不支持 Demo。
 */
public List<String> getDemoCommands() {
    return List.of();
}

/**
 * 单步之间的停顿时长（毫秒）。
 */
public int getDemoStepDelayMs() {
    return 1200;
}
```

**设计要点**：
- 两个方法均为 `default`（或有默认实现），既有游戏不强制覆写
- 返回 `List<String>` 而非数组，便于动态生成（如 Peace 的 64 步填满序列）
- 命令字符串格式与玩家手动输入完全一致，确保合法性

---

## 5. GameController 的 Demo 调度逻辑

### 5.1 状态变量

```java
private boolean demoMode = false;      // 是否处于演示模式
private int demoGameIndex = 0;         // 当前演示到第几个游戏
private int demoCmdIndex = 0;          // 当前游戏执行到第几条命令
```

### 5.2 主循环集成

正常模式下，`GameController.run()` 通过 `ui.readKey()` 阻塞等待用户输入。

Demo 模式下，改为**非阻塞轮询**：每 50ms 检测一次按键，同时检查是否到达下一步执行时间。

```
while (true) {
    渲染当前画面
    
    if (demoMode) {
        if (tickDemo()) {  // 执行一步，返回 true 表示全部结束
            demoMode = false;
            显示"演示结束"
        }
        continue;  // Demo 模式下跳过正常键盘输入处理
    }
    
    // 正常模式：阻塞读键
    int key = ui.readKey();
    ...
}
```

### 5.3 tickDemo() 伪代码

```java
private boolean tickDemo() {
    // 1. 检测用户中断（任意按键）
    if (ui.pollKey() != 无按键) {
        abortDemo();
        return true;  // 标记为"结束"
    }
    
    // 2. 当前局剧本耗尽，切下一局
    if (当前命令索引 >= 当前游戏.getDemoCommands().size()) {
        demoGameIndex++;
        demoCmdIndex = 0;
        
        if (demoGameIndex >= 游戏总数) {
            resetToDefaults();
            return true;  // 全部演示完毕
        }
        
        select(demoGameIndex + 1);  // 切到下一局
        return false;  // 继续演示
    }
    
    // 3. 执行当前命令
    String cmd = 当前游戏.getDemoCommands().get(当前命令索引);
    当前游戏.processCommand(cmd);
    demoCmdIndex++;
    
    // 4. 等待步间延迟（期间持续渲染+检测中断）
    long end = 当前时间 + 当前游戏.getDemoStepDelayMs();
    while (当前时间 < end) {
        渲染画面();
        if (ui.pollKey() != 无按键) {
            abortDemo();
            return true;
        }
        sleep(50ms);
    }
    
    return false;  // 继续下一步
}
```

### 5.4 abortDemo() 伪代码

```java
private void abortDemo() {
    demoMode = false;
    demoGameIndex = 0;
    demoCmdIndex = 0;
    resetToDefaults();  // 恢复初始四局
    platformMessage = "演示已终止";
}
```

---

## 6. 各游戏剧本设计规范

### 6.1 剧本必须满足的条件

1. **每条命令必须合法**：能被该游戏的 `processCommand()` 成功识别并处理
2. **不依赖作弊 API**：不能调用 `loadDemoLayout`、`forceWin` 等非公开方法
3. **自然展示玩法**：通过真实对局展示核心机制，而非构造人工局面

### 6.2 各游戏剧本示例

#### PeaceGame

**目标**：展示轮流落子、棋盘填满、终局判定。

```java
@Override
public List<String> getDemoCommands() {
    // 生成 64 步填满棋盘的完整序列
    List<String> cmds = new ArrayList<>();
    for (int r = 0; r < getSize(); r++) {
        for (int c = 0; c < getSize(); c++) {
            cmds.add("" + (char)('a' + c) + (r + 1));
        }
    }
    return cmds;
}

@Override
public int getDemoStepDelayMs() {
    return 300;  // 快速填满
}
```

#### ReversiGame

**目标**：展示夹吃翻转、合法落子高亮（`+`）、Pass 机制、比分变化。

```java
@Override
public List<String> getDemoCommands() {
    return List.of(
        "d3",   // 黑落子，夹吃白棋
        "c3",   // 白落子
        "c4",   // 黑
        "d4",   // 白
        "e3",   // 黑
        "f3",   // 白
        "c5",   // 黑
        "b3",   // 白
        "pass", // 黑无合法落子，演示强制 Pass
        "f4"    // 继续
    );
}

@Override
public int getDemoStepDelayMs() {
    return 800;
}
```

#### Minesweeper

**目标**：展示首开安全、数字提示、插旗、终局（踩雷或胜利）。

**策略**：雷随机生成，Demo 剧本走一条"大概率安全"的路径。若踩雷，本身就是玩法展示的一部分。

```java
@Override
public List<String> getDemoCommands() {
    return List.of(
        "b2",       // 首开安全
        "c2",       // 继续探索
        "d2",       // 继续
        "f c3",     // 插旗
        "f c3",     // 取消插旗（演示 toggle）
        "e2"        // 可能踩雷或安全
    );
}

@Override
public int getDemoStepDelayMs() {
    return 1000;
}
```

#### ChessGame

**目标**：展示兵的直走/吃子、马的日字、象的斜线、后的火力、王车易位、吃过路兵、兵升变、胜负判定。

**策略**：设计一个真实短对局，让各棋子自然走到展示位置。若某机制难以在短对局中自然出现（如兵升变需要走很多步），可单独设计一个"升变剧本"，但**仍通过正常走法达成**，不直接修改棋盘。

```java
@Override
public List<String> getDemoCommands() {
    return List.of(
        // 兵直走
        "m e2 e4", "m e7 e5",
        // 马走日
        "m g1 f3", "m b8 c6",
        // 象斜线
        "m f1 c4", "m f8 c5",
        // 后直线
        "m d1 e2",
        // 王走一步
        "m e8 e7",
        // 王车易位（白方短易位）
        "m e1 g1",
        // 吃过路兵：黑兵 d7-d5（两步），白兵 e4 吃 d5
        "m d7 d5", "m e4 d5",
        // 兵升变：通过真实对局让白兵走到 a8
        // （此处需根据实际对局设计合法路径）
        "m a2 a4", "m a7 a6", "m a4 a5", "m b7 b6",
        "m a5 b6", "m c7 b6", "m b2 b4", "m a6 a5",
        "m b4 a5", "m a8 a6", "m a5 a6", "m b6 a5",
        "m a6 a7", "m a5 a4", "m a7 a8 q"  // 升变为后
    );
}

@Override
public int getDemoStepDelayMs() {
    return 1500;
}
```

---

## 7. 关键设计决策

### 7.1 为什么不使用 `loadDemoLayout` 等作弊 API？

| 方案 | 问题 |
|------|------|
| 控制器直接调用 `game.loadDemoLayout(...)` | 控制器必须 `instanceof` 具体游戏类，破坏插件化 |
| 游戏类暴露 `forceWinForDemo()` | 污染领域模型，正常游戏逻辑与 Demo 逻辑混杂 |
| **通过 `processCommand` 自然走棋** | 控制器只依赖 `BaseGame` 接口，游戏内部无 Demo 专属代码 |

### 7.2 为什么 `getDemoCommands()` 返回 `List<String>` 而不是 `List<Command>`？

- `String` 与玩家手动输入格式一致，无需新增命令对象层次
- 现有 `processCommand(String)` 签名直接消费，无需适配层
- 简单、可序列化、易于调试

### 7.3 如何处理命令执行失败？

Demo 执行某条命令时，若 `processCommand` 返回 `false`（命令未被识别）或游戏状态异常（如终局后继续输入），**静默跳过并继续下一条**。

理由：Demo 是展示性质，不应因单步失败而中断。跳过机制由 `tickDemo` 自然保证（`processCommand` 返回 `true` 仅表示"已处理"，失败提示不影响后续执行）。

---

## 8. 实现 checklist

- [ ] `BaseGame` 新增 `getDemoCommands()` 和 `getDemoStepDelayMs()` 默认方法
- [ ] `PeaceGame` 覆写 `getDemoCommands()`，生成 64 步填满序列
- [ ] `ReversiGame` 覆写 `getDemoCommands()`，包含 `pass` 命令
- [ ] `Minesweeper` 覆写 `getDemoCommands()`，展示首开/插旗
- [ ] `ChessGame` 覆写 `getDemoCommands()`，设计真实短对局
- [ ] `GameController` 新增 `demoMode`、`demoGameIndex`、`demoCmdIndex` 状态
- [ ] `GameController.run()` 主循环集成 Demo 分支
- [ ] `GameController` 实现 `startDemo()`、`tickDemo()`、`abortDemo()`
- [ ] `handleEnter()` 识别 `demo` 命令，调用 `startDemo()`
- [ ] `GameTexts` 确认已有 `COMMAND_DEMO`、`DEMO_START`、`DEMO_ABORT_HINT`、`DEMO_ABORTED`、`DEMO_FINISHED`
- [ ] `TerminalUI` 已有 `pollKey()` 方法（非阻塞读键）
- [ ] **移除**所有 `instanceof` 具体游戏类的代码
- [ ] **移除**所有 Demo 专属作弊 API（`loadDemoLayout`、`forceWinForDemo`、`resetForDemo`、`fillBoardForDemo` 等）

---

## 9. 验收标准

| 场景 | 预期行为 |
|------|----------|
| 输入 `demo` | 进入演示模式，显示"演示模式启动"，自动切到第 1 局 |
| Peace 演示 | 棋盘快速填满，显示终局信息 |
| Reversi 演示 | 看到夹吃翻转、`+` 高亮、至少一次 `pass` |
| Minesweeper 演示 | 首开安全格、数字提示、插旗/取消、可能踩雷或胜利 |
| Chess 演示 | 兵/马/象/后/王各走法、王车易位、吃过路兵、兵升变、终局胜负 |
| 演示中按任意键 | 立即中断，重置为初始四局，显示"演示已终止" |
| 演示自然结束 | 重置为初始四局，显示"演示结束" |
| 新增第五款游戏 | 只需该游戏覆写 `getDemoCommands()`，控制器零改动 |

---

## 10. 常见陷阱

1. **不要 `instanceof`**：控制器里写 `if (game instanceof ChessGame)` 就输了，这是架构倒退。
2. **不要作弊 API**：`loadDemoLayout` 看似方便，实则让控制器与具体游戏深度耦合。
3. **步间延迟要可配置**：不同游戏节奏不同（Peace 快、Chess 慢），通过 `getDemoStepDelayMs()` 让游戏自己决定。
4. **剧本要真实合法**：`getDemoCommands()` 里的每条命令，手动输入到游戏里也必须能执行成功。实现后务必逐条验证。