# 推箱子游戏类图

## 系统架构

系统采用MVC架构模式，将用户界面和应用逻辑分离：
- **Model层（logic包）**：负责游戏数据和业务逻辑
- **View层（ui包）**：负责用户界面显示

## 类图

```
┌─────────────────────────────────────────────────────────────────┐
│                         主程序入口                                │
├─────────────────────────────────────────────────────────────────┤
│ MainFrame (JFrame)                                              │
│ - gamePanel: GamePanel                                          │
│ - levelLabel: JLabel                                            │
│ - stepsLabel: JLabel                                            │
│ - timeLabel: JLabel                                             │
│ - gameEngine: GameEngine                                        │
│ - levelManager: LevelManager                                    │
│ - scoreRecord: ScoreRecord                                      │
│ - gameTimer: Timer                                              │
│ + MainFrame()                                                   │
│ - initComponents(): void                                        │
│ - loadLevels(): void                                            │
│ - handleKeyPress(KeyEvent): void                                │
│ - updateInfoPanel(): void                                       │
│ - handleLevelComplete(): void                                   │
│ - previousLevel(): void                                         │
│ - nextLevel(): void                                             │
│ - resetLevel(): void                                            │
│ - undoMove(): void                                              │
│ - openLevelEditor(): void                                       │
│ - openLeaderboard(): void                                       │
│ + main(String[]): void                                          │
└─────────────────────────────────────────────────────────────────┘
                              │
                              │ 使用
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                      游戏引擎（核心逻辑）                         │
├─────────────────────────────────────────────────────────────────┤
│ GameEngine                                                      │
│ - gameMap: GameMap                                              │
│ - steps: int                                                    │
│ - startTime: long                                               │
│ - elapsedTime: long                                             │
│ - gameStarted: boolean                                          │
│ - gameCompleted: boolean                                        │
│ - mapHistory: List<int[][]>                                     │
│ - stepsHistory: List<Integer>                                   │
│ - currentLevel: Level                                           │
│ + GameEngine(Level)                                             │
│ + resetGame(): void                                             │
│ + loadLevel(Level): void                                        │
│ + move(Direction): boolean                                      │
│ + undo(): boolean                                               │
│ + updateTime(): void                                            │
│ + getFormattedTime(): String                                    │
│ + getGameMap(): GameMap                                         │
│ + getSteps(): int                                               │
│ + getElapsedTime(): long                                        │
│ + isGameStarted(): boolean                                      │
│ + isGameCompleted(): boolean                                    │
│ + getCurrentLevel(): Level                                      │
└─────────────────────────────────────────────────────────────────┘
        │                                   │
        │ 使用                              │ 使用
        ▼                                   ▼
┌──────────────────────┐          ┌──────────────────────────────┐
│   Direction (enum)   │          │         GameMap              │
├──────────────────────┤          ├──────────────────────────────┤
│ UP(0, -1)            │          │ - map: int[][]               │
│ DOWN(0, 1)           │          │ - width: int                 │
│ LEFT(-1, 0)          │          │ - height: int                │
│ RIGHT(1, 0)          │          │ - playerX: int               │
│ + getDx(): int       │          │ - playerY: int               │
│ + getDy(): int       │          │ - boxCount: int              │
└──────────────────────┘          │ - targetCount: int           │
                                  │ + GameMap(int[][])           │
                                  │ + getElement(int, int): int  │
                                  │ + setElement(int, int): void │
                                  │ + isPassable(int, int): bool │
                                  │ + isCompleted(): boolean     │
                                  │ + getMapCopy(): int[][]      │
                                  │ + getPlayerX(): int          │
                                  │ + getPlayerY(): int          │
                                  │ + setPlayerPosition(): void  │
                                  │ + refresh(): void            │
                                  └──────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                      关卡管理                                    │
├─────────────────────────────────────────────────────────────────┤
│ LevelManager                                                    │
│ - levels: List<Level>                                           │
│ - currentLevelIndex: int                                        │
│ - levelDirectory: String                                        │
│ + LevelManager(String)                                          │
│ + loadLevels(): void                                            │
│ + saveLevelToFile(Level, File): void                            │
│ + getLevel(int): Level                                          │
│ + getCurrentLevel(): Level                                      │
│ + nextLevel(): boolean                                          │
│ + previousLevel(): boolean                                      │
│ + goToLevel(int): boolean                                       │
│ + addLevel(Level): void                                         │
│ + getLevelCount(): int                                          │
│ + elementToChar(int): char [static]                             │
└─────────────────────────────────────────────────────────────────┘
                              │
                              │ 管理
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│ Level                                                           │
│ - levelNumber: int                                              │
│ - name: String                                                  │
│ - gameMap: GameMap                                              │
│ - bestSteps: int                                                │
│ + Level(int, String, GameMap)                                   │
│ + getInitialMap(): int[][]                                      │
│ + updateBestSteps(int): boolean                                 │
│ + getLevelNumber(): int                                         │
│ + setLevelNumber(int): void                                     │
│ + getName(): String                                             │
│ + setName(String): void                                         │
│ + getGameMap(): GameMap                                         │
│ + setGameMap(GameMap): void                                     │
│ + getBestSteps(): int                                           │
│ + setBestSteps(int): void                                       │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                      分数记录                                    │
├─────────────────────────────────────────────────────────────────┤
│ ScoreRecord                                                     │
│ - scores: Map<Integer, Integer>                                 │
│ - filePath: String                                              │
│ + ScoreRecord(String)                                           │
│ + loadScores(): void                                            │
│ + saveScores(): void                                            │
│ + getBestSteps(int): int                                        │
│ + updateScore(int, int): boolean                                │
│ + getAllScores(): Map<Integer, Integer>                         │
│ + getRecordCount(): int                                         │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                      用户界面                                    │
├─────────────────────────────────────────────────────────────────┤
│ GamePanel (JPanel)                                              │
│ - gameMap: GameMap                                              │
│ + GamePanel()                                                   │
│ + setGameMap(GameMap): void                                     │
│ + paintComponent(Graphics): void                                │
│ - drawTile(Graphics2D, int, int, int): void                     │
│ - drawWallPattern(Graphics2D, int, int): void                   │
│ - drawTarget(Graphics2D, int, int): void                        │
│ - drawBox(Graphics2D, int, int, Color): void                    │
│ - drawPlayer(Graphics2D, int, int): void                        │
│ + getTileSize(): int [static]                                   │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│ LevelEditor (JDialog)                                           │
│ - editorPanel: EditorPanel                                      │
│ - toolComboBox: JComboBox<String>                               │
│ - nameField: JTextField                                         │
│ - levelManager: LevelManager                                    │
│ - levelsDir: String                                             │
│ - parentFrame: MainFrame                                        │
│ + LevelEditor(MainFrame, LevelManager, String)                  │
│ - initComponents(): void                                        │
│ - createToolPanel(): JPanel                                     │
│ - createButtonPanel(): JPanel                                   │
│ - saveLevel(): void                                             │
│ - validateMap(int[][]): String                                  │
└─────────────────────────────────────────────────────────────────┘
                              │
                              │ 包含
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│ EditorPanel (JPanel) [内部类]                                    │
│ - mapData: int[][]                                              │
│ - mapWidth: int                                                 │
│ - mapHeight: int                                                │
│ - currentTool: int                                              │
│ + EditorPanel()                                                 │
│ + createNewMap(int, int): void                                  │
│ + clearMap(): void                                              │
│ + getMapData(): int[][]                                         │
│ + paintComponent(Graphics): void                                │
│ - paintTile(int, int): void                                     │
│ - drawEditorTile(Graphics2D, int, int, int, int): void          │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│ LeaderboardDialog (JDialog)                                     │
│ - scoreRecord: ScoreRecord                                      │
│ - totalLevels: int                                              │
│ + LeaderboardDialog(JFrame, ScoreRecord, int)                   │
│ - initComponents(): void                                        │
└─────────────────────────────────────────────────────────────────┘
```

## 类关系说明

### 1. 依赖关系（uses）
- **MainFrame** 依赖 **GameEngine**、**LevelManager**、**ScoreRecord**、**GamePanel**
- **GameEngine** 依赖 **GameMap**、**Level**
- **LevelManager** 依赖 **Level**、**GameMap**
- **GamePanel** 依赖 **GameMap**

### 2. 组合关系（contains）
- **MainFrame** 包含 **GamePanel**、**LevelManager**、**ScoreRecord**、**GameEngine**
- **LevelManager** 包含多个 **Level**
- **LevelEditor** 包含 **EditorPanel**

### 3. 关联关系（has-a）
- **Level** 包含一个 **GameMap**
- **GameEngine** 包含一个 **GameMap** 和一个 **Level**

### 4. 继承关系（extends）
- **GamePanel** 继承自 **JPanel**
- **MainFrame** 继承自 **JFrame**
- **LevelEditor** 继承自 **JDialog**
- **LeaderboardDialog** 继承自 **JDialog**
- **EditorPanel** 继承自 **JPanel**

## 包结构

```
src/
├── logic/              # 应用逻辑层
│   ├── GameMap.java        # 游戏地图
│   ├── Level.java          # 关卡
│   ├── LevelManager.java   # 关卡管理器
│   ├── GameEngine.java     # 游戏引擎
│   └── ScoreRecord.java    # 分数记录
└── ui/                 # 用户界面层
    ├── MainFrame.java      # 主窗口
    ├── GamePanel.java      # 游戏面板
    ├── LevelEditor.java    # 关卡编辑器
    └── LeaderboardDialog.java # 排行榜对话框
```

## 设计模式

1. **MVC模式**：将界面（View）和逻辑（Model）分离
2. **观察者模式**：通过事件监听处理用户输入
3. **命令模式**：撤销功能通过保存历史状态实现
4. **工厂模式**：LevelManager负责创建和管理Level对象

## 数据流

1. 用户通过键盘输入 → MainFrame处理事件
2. MainFrame调用GameEngine.move() → 更新GameMap
3. GameEngine通知GamePanel重绘 → 显示最新状态
4. 通关后 → ScoreRecord保存分数 → LevelManager切换关卡

## 持久化

- **关卡文件**：`levels/levelXX.lvl` - 文本格式存储地图数据
- **分数文件**：`scores/scores.dat` - 存储每个关卡的最佳成绩
