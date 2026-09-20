package test;

import logic.GameEngine;
import logic.GameMap;
import logic.Level;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * GameEngine 类的单元测试
 */
public class GameEngineTest {

    private GameEngine engine;
    private Level level;

    @BeforeEach
    void setUp() {
        // 创建一个简单的测试地图
        int[][] mapData = {
                { 1, 1, 1, 1, 1 },
                { 1, 0, 0, 0, 1 },
                { 1, 2, 4, 0, 1 }, // 箱子在(1,2)，玩家在(2,2)
                { 1, 0, 0, 3, 1 }, // 目的地在(3,3)
                { 1, 1, 1, 1, 1 }
        };
        GameMap gameMap = new GameMap(mapData);
        level = new Level(1, "测试关卡", gameMap);
        engine = new GameEngine(level);
    }

    @Test
    @DisplayName("测试初始状态")
    void testInitialState() {
        assertEquals(0, engine.getSteps());
        assertFalse(engine.isGameStarted());
        assertFalse(engine.isGameCompleted());
        assertNotNull(engine.getGameMap());
    }

    @Test
    @DisplayName("测试玩家移动 - 向右")
    void testMoveRight() {
        boolean moved = engine.move(GameEngine.Direction.RIGHT);
        assertTrue(moved);
        assertEquals(1, engine.getSteps());
        assertTrue(engine.isGameStarted());
    }

    @Test
    @DisplayName("测试玩家移动 - 向左（推箱子）")
    void testMoveLeft() {
        // 玩家在(2,2)，左边是箱子在(1,2)，箱子左边是墙(0,2)
        // 所以推不动箱子，移动失败
        boolean moved = engine.move(GameEngine.Direction.LEFT);
        assertFalse(moved); // 箱子被墙挡住，推不动
        assertEquals(0, engine.getSteps()); // 步数不变
    }

    @Test
    @DisplayName("测试玩家移动 - 向上")
    void testMoveUp() {
        boolean moved = engine.move(GameEngine.Direction.UP);
        assertTrue(moved);
        assertEquals(1, engine.getSteps());
    }

    @Test
    @DisplayName("测试玩家移动 - 向下")
    void testMoveDown() {
        boolean moved = engine.move(GameEngine.Direction.DOWN);
        assertTrue(moved);
        assertEquals(1, engine.getSteps());
    }

    @Test
    @DisplayName("测试推箱子")
    void testPushBox() {
        // 创建一个可以推动箱子的地图
        int[][] mapData = {
                { 1, 1, 1, 1, 1 },
                { 1, 0, 0, 0, 1 },
                { 1, 0, 2, 4, 1 }, // 箱子在(2,2)，玩家在(3,2)
                { 1, 0, 0, 0, 1 },
                { 1, 1, 1, 1, 1 }
        };
        GameMap gameMap = new GameMap(mapData);
        Level testLevel = new Level(1, "推箱子测试", gameMap);
        GameEngine testEngine = new GameEngine(testLevel);
        GameMap map = testEngine.getGameMap();

        // 玩家在箱子右边(3,2)，向左推
        boolean moved = testEngine.move(GameEngine.Direction.LEFT);
        assertTrue(moved);

        // 箱子应该被推到(1,2)
        assertEquals(2, map.getElement(1, 2)); // 箱子新位置
        assertEquals(4, map.getElement(2, 2)); // 玩家到箱子原位置
        assertEquals(0, map.getElement(3, 2)); // 玩家原位置变空地
    }

    @Test
    @DisplayName("测试撞墙不能移动")
    void testHitWall() {
        // 玩家在(2,2)，向上移动
        engine.move(GameEngine.Direction.UP); // 到(2,1)
        engine.move(GameEngine.Direction.UP); // 到(2,0)是墙，移动失败

        // 再向上应该撞墙
        boolean moved = engine.move(GameEngine.Direction.UP);
        assertFalse(moved);
        assertEquals(1, engine.getSteps()); // 只有第一次成功
    }

    @Test
    @DisplayName("测试撤销功能")
    void testUndo() {
        // 移动一步
        engine.move(GameEngine.Direction.RIGHT);
        assertEquals(1, engine.getSteps());

        // 撤销
        boolean undone = engine.undo();
        assertTrue(undone);
        assertEquals(0, engine.getSteps());
    }

    @Test
    @DisplayName("测试多次撤销")
    void testMultipleUndo() {
        engine.move(GameEngine.Direction.RIGHT);
        engine.move(GameEngine.Direction.DOWN);
        assertEquals(2, engine.getSteps());

        engine.undo();
        assertEquals(1, engine.getSteps());

        engine.undo();
        assertEquals(0, engine.getSteps());

        // 没有更多历史记录时撤销应该失败
        boolean undone = engine.undo();
        assertFalse(undone);
    }

    @Test
    @DisplayName("测试重置游戏")
    void testResetGame() {
        engine.move(GameEngine.Direction.RIGHT);
        engine.move(GameEngine.Direction.DOWN);
        assertEquals(2, engine.getSteps());

        engine.resetGame();
        assertEquals(0, engine.getSteps());
        assertFalse(engine.isGameStarted());
    }

    @Test
    @DisplayName("测试加载新关卡")
    void testLoadLevel() {
        int[][] newMapData = {
                { 1, 1, 1 },
                { 1, 4, 1 },
                { 1, 1, 1 }
        };
        GameMap newMap = new GameMap(newMapData);
        Level newLevel = new Level(2, "新关卡", newMap);

        engine.loadLevel(newLevel);
        assertEquals(newLevel, engine.getCurrentLevel());
        assertEquals(0, engine.getSteps());
    }

    @Test
    @DisplayName("测试通关检测 - 初始已通关")
    void testGameCompletion_Initial() {
        // 创建一个箱子在目的地上的地图
        int[][] mapData = {
                { 1, 1, 1, 1 },
                { 1, 5, 0, 1 }, // 5 = 箱子在目的地
                { 1, 0, 4, 1 },
                { 1, 1, 1, 1 }
        };
        GameMap gameMap = new GameMap(mapData);
        Level testLevel = new Level(1, "通关测试", gameMap);
        GameEngine testEngine = new GameEngine(testLevel);

        // 初始状态已经是通关状态
        assertTrue(testEngine.isGameCompleted());
    }

    @Test
    @DisplayName("测试通关检测 - 移动后通关")
    void testGameCompletion_AfterMove() {
        // 创建一个需要移动一步才能通关的地图
        // 箱子在(2,2)，目的地在(2,1)，玩家在(2,3)
        // 玩家向上推箱子到目的地即通关
        int[][] mapData = {
                { 1, 1, 1, 1, 1 },
                { 1, 0, 3, 0, 1 }, // 目的地在(2,1)
                { 1, 0, 2, 0, 1 }, // 箱子在(2,2)
                { 1, 0, 4, 0, 1 }, // 玩家在(2,3)
                { 1, 1, 1, 1, 1 }
        };
        GameMap gameMap = new GameMap(mapData);
        Level testLevel = new Level(1, "移动通关测试", gameMap);
        GameEngine testEngine = new GameEngine(testLevel);

        // 初始未通关
        assertFalse(testEngine.isGameCompleted());

        // 玩家向上推箱子到目的地
        testEngine.move(GameEngine.Direction.UP);
        assertTrue(testEngine.isGameCompleted());
    }

    @Test
    @DisplayName("测试时间格式化")
    void testGetFormattedTime() {
        // 初始时间应该是 00:00
        assertEquals("00:00", engine.getFormattedTime());
    }
}
