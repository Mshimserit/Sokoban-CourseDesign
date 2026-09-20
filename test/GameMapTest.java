package test;

import logic.GameMap;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * GameMap 类的单元测试
 */
public class GameMapTest {

  @Test
  @DisplayName("测试从二维数组创建地图")
  void testCreateMapFromArray() {
    int[][] mapData = {
        { 1, 1, 1, 1, 1 },
        { 1, 0, 0, 0, 1 },
        { 1, 0, 4, 0, 1 },
        { 1, 0, 0, 0, 1 },
        { 1, 1, 1, 1, 1 }
    };
    GameMap map = new GameMap(mapData);

    assertEquals(5, map.getWidth());
    assertEquals(5, map.getHeight());
    assertEquals(2, map.getPlayerX());
    assertEquals(2, map.getPlayerY());
  }

  @Test
  @DisplayName("测试地图元素获取")
  void testGetElement() {
    int[][] mapData = {
        { 1, 1, 1 },
        { 1, 4, 1 },
        { 1, 1, 1 }
    };
    GameMap map = new GameMap(mapData);

    assertEquals(1, map.getElement(0, 0)); // 墙
    assertEquals(4, map.getElement(1, 1)); // 玩家
    assertEquals(1, map.getElement(-1, 0)); // 越界视为墙
  }

  @Test
  @DisplayName("测试地图元素设置")
  void testSetElement() {
    int[][] mapData = {
        { 1, 1, 1 },
        { 1, 0, 1 },
        { 1, 1, 1 }
    };
    GameMap map = new GameMap(mapData);

    map.setElement(1, 1, 4); // 设置玩家
    assertEquals(4, map.getElement(1, 1));
  }

  @Test
  @DisplayName("测试可通行判断")
  void testIsPassable() {
    int[][] mapData = {
        { 1, 1, 1 },
        { 1, 0, 1 },
        { 1, 1, 1 }
    };
    GameMap map = new GameMap(mapData);

    assertFalse(map.isPassable(0, 0)); // 墙不可通行
    assertTrue(map.isPassable(1, 1)); // 空地可通行
  }

  @Test
  @DisplayName("测试箱子统计")
  void testBoxCount() {
    int[][] mapData = {
        { 1, 1, 1, 1, 1 },
        { 1, 2, 0, 2, 1 },
        { 1, 0, 4, 0, 1 },
        { 1, 0, 0, 0, 1 },
        { 1, 1, 1, 1, 1 }
    };
    GameMap map = new GameMap(mapData);

    assertEquals(2, map.getBoxCount());
  }

  @Test
  @DisplayName("测试目的地统计")
  void testTargetCount() {
    int[][] mapData = {
        { 1, 1, 1, 1, 1 },
        { 1, 0, 0, 0, 1 },
        { 1, 0, 4, 0, 1 },
        { 1, 3, 0, 3, 1 },
        { 1, 1, 1, 1, 1 }
    };
    GameMap map = new GameMap(mapData);

    assertEquals(2, map.getTargetCount());
  }

  @Test
  @DisplayName("测试通关判断 - 未完成")
  void testIsCompleted_NotDone() {
    int[][] mapData = {
        { 1, 1, 1, 1, 1 },
        { 1, 2, 0, 0, 1 },
        { 1, 0, 4, 0, 1 },
        { 1, 3, 0, 0, 1 },
        { 1, 1, 1, 1, 1 }
    };
    GameMap map = new GameMap(mapData);

    assertFalse(map.isCompleted()); // 箱子不在目的地上
  }

  @Test
  @DisplayName("测试通关判断 - 已完成")
  void testIsCompleted_Done() {
    int[][] mapData = {
        { 1, 1, 1, 1, 1 },
        { 1, 5, 0, 0, 1 }, // 5 = 箱子在目的地上
        { 1, 0, 4, 0, 1 },
        { 1, 0, 0, 0, 1 },
        { 1, 1, 1, 1, 1 }
    };
    GameMap map = new GameMap(mapData);

    assertTrue(map.isCompleted());
  }

  @Test
  @DisplayName("测试地图深拷贝")
  void testGetMapCopy() {
    int[][] mapData = {
        { 1, 1, 1 },
        { 1, 4, 1 },
        { 1, 1, 1 }
    };
    GameMap map = new GameMap(mapData);
    int[][] copy = map.getMapCopy();

    // 修改原地图不应影响副本
    map.setElement(1, 1, 0);
    assertEquals(4, copy[1][1]);
    assertEquals(0, map.getElement(1, 1));
  }

  @Test
  @DisplayName("测试指定大小创建空地图")
  void testCreateEmptyMap() {
    GameMap map = new GameMap(10, 8);

    assertEquals(10, map.getWidth());
    assertEquals(8, map.getHeight());
    assertEquals(0, map.getBoxCount());
    assertEquals(0, map.getTargetCount());
  }
}
