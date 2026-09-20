package logic;

import java.util.ArrayList;
import java.util.List;

/**
 * 游戏引擎类，负责游戏逻辑控制和状态管理
 */
public class GameEngine {
    /**
     * 移动方向枚举
     */
    public enum Direction {
        UP(0, -1),
        DOWN(0, 1),
        LEFT(-1, 0),
        RIGHT(1, 0);

        private final int dx;
        private final int dy;

        Direction(int dx, int dy) {
            this.dx = dx;
            this.dy = dy;
        }

        public int getDx() {
            return dx;
        }

        public int getDy() {
            return dy;
        }
    }

    private GameMap gameMap; // 当前游戏地图
    private int steps; // 当前步数
    private long startTime; // 游戏开始时间
    private long elapsedTime; // 已用时间（毫秒）
    private boolean gameStarted; // 游戏是否开始
    private boolean gameCompleted; // 游戏是否完成
    private boolean deadlockDetected; // 是否检测到死局
    private List<int[][]> mapHistory; // 地图状态历史（用于撤销）
    private List<Integer> stepsHistory; // 步数历史
    private Level currentLevel; // 当前关卡

    /**
     * 构造函数
     * 
     * @param level 初始关卡
     */
    public GameEngine(Level level) {
        this.currentLevel = level;
        this.mapHistory = new ArrayList<>();
        this.stepsHistory = new ArrayList<>();
        resetGame();
    }

    /**
     * 重置游戏到初始状态
     */
    public void resetGame() {
        int[][] initialMap = currentLevel.getInitialMap();
        this.gameMap = new GameMap(initialMap);
        this.steps = 0;
        this.startTime = 0;
        this.elapsedTime = 0;
        this.gameStarted = false;
        this.gameCompleted = gameMap.isCompleted();
        this.deadlockDetected = false;
        this.mapHistory.clear();
        this.stepsHistory.clear();
    }

    /**
     * 加载新关卡
     * 
     * @param level 新关卡
     */
    public void loadLevel(Level level) {
        this.currentLevel = level;
        resetGame();
    }

    /**
     * 移动玩家
     * 
     * @param direction 移动方向
     * @return 是否移动成功
     */
    public boolean move(Direction direction) {
        if (gameCompleted) {
            return false;
        }

        if (!gameStarted) {
            startTime = System.currentTimeMillis();
            gameStarted = true;
        }

        int playerX = gameMap.getPlayerX();
        int playerY = gameMap.getPlayerY();
        int newX = playerX + direction.getDx();
        int newY = playerY + direction.getDy();

        int targetElement = gameMap.getElement(newX, newY);

        // 撞墙不能移动
        if (targetElement == 1) {
            return false;
        }

        // 前方是箱子时，先判断能否推动（只计算一次）
        boolean pushingBox = (targetElement == 2 || targetElement == 5);
        int boxNewX = 0;
        int boxNewY = 0;
        boolean boxLandsOnTarget = false;
        if (pushingBox) {
            boxNewX = newX + direction.getDx();
            boxNewY = newY + direction.getDy();
            int boxTargetElement = gameMap.getElement(boxNewX, boxNewY);

            // 箱子不能推过墙或其他箱子
            if (boxTargetElement == 1 || boxTargetElement == 2 || boxTargetElement == 5) {
                return false;
            }
            // 记录箱子新位置是否为目的地
            boxLandsOnTarget = (boxTargetElement == 3 || boxTargetElement == 6);
        }

        // 保存当前状态到历史（用于撤销）
        mapHistory.add(gameMap.getMapCopy());
        stepsHistory.add(steps);

        // 推箱子：更新箱子新位置与箱子原位置
        if (pushingBox) {
            // 箱子新位置：在目的地则为5，否则为2
            gameMap.setElement(boxNewX, boxNewY, boxLandsOnTarget ? 5 : 2);
            // 箱子原来的位置：如果在目的地则露出目的地6，否则为空地0
            gameMap.setElement(newX, newY, (targetElement == 5) ? 6 : 0);
        }

        // 更新玩家位置
        int playerOldElement = gameMap.getElement(playerX, playerY);
        // 玩家新位置：站在目的地上（空目的地3，或箱子刚被推走露出目的地5）则为6，否则为4
        int newPlayerElement = (targetElement == 3 || targetElement == 5) ? 6 : 4;

        // 玩家原来位置：如果在目的地则恢复目的地，否则为空地
        gameMap.setElement(playerX, playerY, (playerOldElement == 6) ? 3 : 0);

        gameMap.setElement(newX, newY, newPlayerElement);
        gameMap.setPlayerPosition(newX, newY);

        steps++;

        // 检查是否完成
        if (gameMap.isCompleted()) {
            gameCompleted = true;
            elapsedTime = System.currentTimeMillis() - startTime;
        }

        // 检查刚推动的箱子是否进入角落死局
        if (pushingBox && gameMap.isCornerDeadlock(boxNewX, boxNewY)) {
            deadlockDetected = true;
        }

        return true;
    }

    /**
     * 撤销上一步移动
     * 
     * @return 是否撤销成功
     */
    public boolean undo() {
        if (mapHistory.isEmpty()) {
            return false;
        }

        // 恢复上一步的地图状态
        int[][] previousMap = mapHistory.remove(mapHistory.size() - 1);
        gameMap = new GameMap(previousMap);
        steps = stepsHistory.remove(stepsHistory.size() - 1);
        gameCompleted = false;
        deadlockDetected = false; // 撤销后清除死局标记

        return true;
    }

    /**
     * 更新游戏时间
     */
    public void updateTime() {
        if (gameStarted && !gameCompleted) {
            elapsedTime = System.currentTimeMillis() - startTime;
        }
    }

    // Getters
    public GameMap getGameMap() {
        return gameMap;
    }

    public int getSteps() {
        return steps;
    }

    public long getElapsedTime() {
        return elapsedTime;
    }

    public boolean isGameStarted() {
        return gameStarted;
    }

    public boolean isGameCompleted() {
        return gameCompleted;
    }

    /**
     * 检查是否检测到死局
     * 
     * @return 是否检测到死局
     */
    public boolean isDeadlockDetected() {
        return deadlockDetected;
    }

    /**
     * 清除死局标记（用于撤销后）
     */
    public void clearDeadlock() {
        this.deadlockDetected = false;
    }

    public Level getCurrentLevel() {
        return currentLevel;
    }

    /**
     * 格式化时间为 mm:ss 格式
     * 
     * @return 格式化后的时间字符串
     */
    public String getFormattedTime() {
        long seconds = elapsedTime / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}
