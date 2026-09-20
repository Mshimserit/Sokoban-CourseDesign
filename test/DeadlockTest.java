import logic.GameMap;
import logic.GameEngine;
import logic.Level;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 死局检测模块测试
 */
public class DeadlockTest {

    @Test
    @DisplayName("测试角落死局检测 - 箱子在角落")
    public void testCornerDeadlock() {
        // 箱子在左上角，两边都是墙
        int[][] mapData = {
                { 1, 1, 1, 1, 1 },
                { 1, 2, 0, 0, 1 },
                { 1, 0, 0, 0, 1 },
                { 1, 0, 0, 0, 1 },
                { 1, 1, 1, 1, 1 }
        };
        GameMap map = new GameMap(mapData);
        assertTrue(map.isDeadlock(), "箱子在角落应该检测到死局");
    }

    @Test
    @DisplayName("测试非死局 - 箱子不在角落")
    public void testNoDeadlock() {
        // 箱子在中间，不在角落
        int[][] mapData = {
                { 1, 1, 1, 1, 1 },
                { 1, 0, 0, 0, 1 },
                { 1, 0, 2, 0, 1 },
                { 1, 0, 0, 0, 1 },
                { 1, 1, 1, 1, 1 }
        };
        GameMap map = new GameMap(mapData);
        assertFalse(map.isDeadlock(), "箱子不在角落不应该检测到死局");
    }

    @Test
    @DisplayName("测试箱子在目的地 - 不算死局")
    public void testBoxOnTarget() {
        // 箱子在目的地上，即使在角落也不算死局
        int[][] mapData = {
                { 1, 1, 1, 1, 1 },
                { 1, 5, 0, 0, 1 }, // 5表示箱子在目的地
                { 1, 0, 0, 0, 1 },
                { 1, 0, 0, 0, 1 },
                { 1, 1, 1, 1, 1 }
        };
        GameMap map = new GameMap(mapData);
        assertFalse(map.isDeadlock(), "箱子在目的地不算死局");
    }

    @Test
    @DisplayName("测试多个箱子 - 一个在角落")
    public void testMultipleBoxesOneDeadlock() {
        int[][] mapData = {
                { 1, 1, 1, 1, 1, 1 },
                { 1, 2, 0, 0, 0, 1 }, // 左上角箱子
                { 1, 0, 0, 0, 0, 1 },
                { 1, 0, 0, 0, 2, 1 }, // 右下角箱子
                { 1, 1, 1, 1, 1, 1 }
        };
        GameMap map = new GameMap(mapData);
        assertTrue(map.isDeadlock(), "有一个箱子在角落应该检测到死局");
    }

    @Test
    @DisplayName("测试获取死局箱子位置")
    public void testGetDeadlockBoxes() {
        int[][] mapData = {
                { 1, 1, 1, 1, 1 },
                { 1, 2, 0, 0, 1 }, // 左上角箱子
                { 1, 0, 0, 0, 1 },
                { 1, 0, 0, 2, 1 }, // 右下角箱子
                { 1, 1, 1, 1, 1 }
        };
        GameMap map = new GameMap(mapData);
        int[][] deadlockBoxes = map.getDeadlockBoxes();
        assertEquals(2, deadlockBoxes.length, "应该有2个死局箱子");
    }

    @Test
    @DisplayName("测试GameEngine死局检测")
    public void testGameEngineDeadlock() {
        int[][] mapData = {
                { 1, 1, 1, 1, 1 },
                { 1, 4, 2, 0, 1 }, // 玩家和箱子
                { 1, 0, 0, 0, 1 },
                { 1, 0, 0, 0, 1 },
                { 1, 1, 1, 1, 1 }
        };
        GameMap gameMap = new GameMap(mapData);
        Level level = new Level(1, "测试关卡", gameMap);
        GameEngine engine = new GameEngine(level);

        // 向右推箱子到角落
        engine.move(GameEngine.Direction.RIGHT);
        assertTrue(engine.isDeadlockDetected(), "推箱子到角落后应该检测到死局");
    }

    @Test
    @DisplayName("测试撤销后清除死局标记")
    public void testUndoClearsDeadlock() {
        int[][] mapData = {
                { 1, 1, 1, 1, 1 },
                { 1, 4, 2, 0, 1 },
                { 1, 0, 0, 0, 1 },
                { 1, 0, 0, 0, 1 },
                { 1, 1, 1, 1, 1 }
        };
        GameMap gameMap = new GameMap(mapData);
        Level level = new Level(1, "测试关卡", gameMap);
        GameEngine engine = new GameEngine(level);

        // 推箱子到角落
        engine.move(GameEngine.Direction.RIGHT);
        assertTrue(engine.isDeadlockDetected(), "应该检测到死局");

        // 撤销
        engine.undo();
        assertFalse(engine.isDeadlockDetected(), "撤销后应该清除死局标记");
    }

    @Test
    @DisplayName("测试重置游戏清除死局标记")
    public void testResetClearsDeadlock() {
        int[][] mapData = {
                { 1, 1, 1, 1, 1 },
                { 1, 4, 2, 0, 1 },
                { 1, 0, 0, 0, 1 },
                { 1, 0, 0, 0, 1 },
                { 1, 1, 1, 1, 1 }
        };
        GameMap gameMap = new GameMap(mapData);
        Level level = new Level(1, "测试关卡", gameMap);
        GameEngine engine = new GameEngine(level);

        // 推箱子到角落
        engine.move(GameEngine.Direction.RIGHT);
        assertTrue(engine.isDeadlockDetected(), "应该检测到死局");

        // 重置游戏
        engine.resetGame();
        assertFalse(engine.isDeadlockDetected(), "重置后应该清除死局标记");
    }
}
