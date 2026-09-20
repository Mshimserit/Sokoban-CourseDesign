package test;

import logic.Level;
import logic.LevelManager;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * LevelManager 类的单元测试
 */
public class LevelManagerTest {

    private LevelManager manager;
    private String testDir;

    @BeforeEach
    void setUp() throws IOException {
        // 创建临时测试目录
        testDir = "test_levels_" + System.currentTimeMillis();
        File dir = new File(testDir);
        dir.mkdirs();
        
        // 创建测试关卡文件
        createTestLevelFile("level01.lvl", "第一关", 
            "#####\n" +
            "#@$.#\n" +
            "#####");
        
        createTestLevelFile("level02.lvl", "第二关",
            "######\n" +
            "#@ $.#\n" +
            "######");
        
        manager = new LevelManager(testDir);
    }

    @AfterEach
    void tearDown() {
        // 清理测试目录
        File dir = new File(testDir);
        if (dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File f : files) {
                    f.delete();
                }
            }
            dir.delete();
        }
    }

    private void createTestLevelFile(String filename, String name, String mapContent) throws IOException {
        File file = new File(testDir, filename);
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(file), StandardCharsets.UTF_8))) {
            pw.println(name);
            pw.print(mapContent);
        }
    }

    @Test
    @DisplayName("测试加载关卡")
    void testLoadLevels() throws IOException {
        manager.loadLevels();
        
        assertEquals(2, manager.getLevelCount());
        assertNotNull(manager.getLevel(0));
        assertNotNull(manager.getLevel(1));
    }

    @Test
    @DisplayName("测试关卡名称")
    void testLevelNames() throws IOException {
        manager.loadLevels();
        
        Level level1 = manager.getLevel(0);
        Level level2 = manager.getLevel(1);
        
        assertEquals("第一关", level1.getName());
        assertEquals("第二关", level2.getName());
    }

    @Test
    @DisplayName("测试当前关卡")
    void testGetCurrentLevel() throws IOException {
        manager.loadLevels();
        
        Level current = manager.getCurrentLevel();
        assertNotNull(current);
        assertEquals(0, manager.getCurrentLevelIndex());
    }

    @Test
    @DisplayName("测试下一关")
    void testNextLevel() throws IOException {
        manager.loadLevels();
        
        boolean hasNext = manager.nextLevel();
        assertTrue(hasNext);
        assertEquals(1, manager.getCurrentLevelIndex());
        
        // 已经是最后一关
        hasNext = manager.nextLevel();
        assertFalse(hasNext);
    }

    @Test
    @DisplayName("测试上一关")
    void testPreviousLevel() throws IOException {
        manager.loadLevels();
        manager.nextLevel(); // 到第二关
        
        boolean hasPrev = manager.previousLevel();
        assertTrue(hasPrev);
        assertEquals(0, manager.getCurrentLevelIndex());
        
        // 已经是第一关
        hasPrev = manager.previousLevel();
        assertFalse(hasPrev);
    }

    @Test
    @DisplayName("测试跳转到指定关卡")
    void testGoToLevel() throws IOException {
        manager.loadLevels();
        
        boolean success = manager.goToLevel(2);
        assertTrue(success);
        assertEquals(1, manager.getCurrentLevelIndex());
        
        // 跳转到不存在的关卡
        success = manager.goToLevel(99);
        assertFalse(success);
    }

    @Test
    @DisplayName("测试添加新关卡")
    void testAddLevel() throws IOException {
        manager.loadLevels();
        int initialCount = manager.getLevelCount();
        
        int[][] mapData = {
            {1, 1, 1},
            {1, 4, 1},
            {1, 1, 1}
        };
        logic.GameMap gameMap = new logic.GameMap(mapData);
        Level newLevel = new Level(3, "第三关", gameMap);
        
        manager.addLevel(newLevel);
        assertEquals(initialCount + 1, manager.getLevelCount());
    }

    @Test
    @DisplayName("测试保存关卡到文件")
    void testSaveLevelToFile() throws IOException {
        manager.loadLevels();
        Level level = manager.getLevel(0);
        
        File outputFile = new File(testDir, "saved_level.lvl");
        manager.saveLevelToFile(level, outputFile);
        
        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);
        
        // 清理
        outputFile.delete();
    }

    @Test
    @DisplayName("测试空目录加载")
    void testLoadEmptyDirectory() throws IOException {
        // 创建空目录
        String emptyDir = "empty_levels_" + System.currentTimeMillis();
        File dir = new File(emptyDir);
        dir.mkdirs();
        
        LevelManager emptyManager = new LevelManager(emptyDir);
        
        assertThrows(IOException.class, () -> {
            emptyManager.loadLevels();
        });
        
        dir.delete();
    }

    @Test
    @DisplayName("测试不存在的目录")
    void testLoadNonExistentDirectory() {
        LevelManager invalidManager = new LevelManager("non_existent_dir");
        
        assertThrows(IOException.class, () -> {
            invalidManager.loadLevels();
        });
    }
}
