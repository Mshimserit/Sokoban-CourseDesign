package test;

import logic.ScoreRecord;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * ScoreRecord 类的单元测试
 */
public class ScoreRecordTest {

    private ScoreRecord scoreRecord;
    private String testFile;

    @BeforeEach
    void setUp() {
        testFile = "test_scores_" + System.currentTimeMillis() + ".dat";
        scoreRecord = new ScoreRecord(testFile);
    }

    @AfterEach
    void tearDown() {
        File file = new File(testFile);
        if (file.exists()) {
            file.delete();
        }
    }

    @Test
    @DisplayName("测试初始状态")
    void testInitialState() {
        assertEquals(0, scoreRecord.getRecordCount());
        assertEquals(-1, scoreRecord.getBestSteps(1)); // 无记录时返回 -1
    }

    @Test
    @DisplayName("测试更新分数 - 首次记录")
    void testUpdateScore_FirstTime() {
        boolean updated = scoreRecord.updateScore(1, 100);
        assertTrue(updated);
        assertEquals(100, scoreRecord.getBestSteps(1));
    }

    @Test
    @DisplayName("测试更新分数 - 刷新记录")
    void testUpdateScore_BetterScore() {
        scoreRecord.updateScore(1, 100);
        boolean updated = scoreRecord.updateScore(1, 80);
        assertTrue(updated);
        assertEquals(80, scoreRecord.getBestSteps(1));
    }

    @Test
    @DisplayName("测试更新分数 - 不刷新记录")
    void testUpdateScore_WorseScore() {
        scoreRecord.updateScore(1, 80);
        boolean updated = scoreRecord.updateScore(1, 100);
        assertFalse(updated);
        assertEquals(80, scoreRecord.getBestSteps(1));
    }

    @Test
    @DisplayName("测试保存和加载分数")
    void testSaveAndLoadScores() throws IOException {
        scoreRecord.updateScore(1, 100);
        scoreRecord.updateScore(2, 150);
        scoreRecord.updateScore(3, 200);
        
        scoreRecord.saveScores();
        
        // 创建新的 ScoreRecord 并加载
        ScoreRecord newRecord = new ScoreRecord(testFile);
        newRecord.loadScores();
        
        assertEquals(100, newRecord.getBestSteps(1));
        assertEquals(150, newRecord.getBestSteps(2));
        assertEquals(200, newRecord.getBestSteps(3));
        assertEquals(3, newRecord.getRecordCount());
    }

    @Test
    @DisplayName("测试获取所有分数")
    void testGetAllScores() {
        scoreRecord.updateScore(1, 100);
        scoreRecord.updateScore(2, 150);
        
        Map<Integer, Integer> allScores = scoreRecord.getAllScores();
        
        assertEquals(2, allScores.size());
        assertEquals(100, allScores.get(1));
        assertEquals(150, allScores.get(2));
    }

    @Test
    @DisplayName("测试加载不存在的文件")
    void testLoadNonExistentFile() throws IOException {
        ScoreRecord newRecord = new ScoreRecord("non_existent.dat");
        newRecord.loadScores(); // 不应该抛出异常
        
        assertEquals(0, newRecord.getRecordCount());
    }

    @Test
    @DisplayName("测试保存时自动创建父目录")
    void testSaveCreatesParentDirectory() throws IOException {
        String nestedFile = "nested/dir/scores.dat";
        ScoreRecord nestedRecord = new ScoreRecord(nestedFile);
        nestedRecord.updateScore(1, 100);
        
        nestedRecord.saveScores();
        
        File file = new File(nestedFile);
        assertTrue(file.exists());
        
        // 清理
        file.delete();
        new File("nested/dir").delete();
        new File("nested").delete();
    }

    @Test
    @DisplayName("测试加载格式错误的文件")
    void testLoadMalformedFile() throws IOException {
        // 创建包含错误格式的文件
        File file = new File(testFile);
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(file), StandardCharsets.UTF_8))) {
            pw.println("1=100");
            pw.println("invalid_line"); // 错误格式
            pw.println("2=200");
            pw.println("not_a_number=abc"); // 错误格式
        }
        
        scoreRecord.loadScores();
        
        // 应该只加载有效的记录
        assertEquals(2, scoreRecord.getRecordCount());
        assertEquals(100, scoreRecord.getBestSteps(1));
        assertEquals(200, scoreRecord.getBestSteps(2));
    }

    @Test
    @DisplayName("测试多个关卡的分数管理")
    void testMultipleLevels() {
        for (int i = 1; i <= 10; i++) {
            scoreRecord.updateScore(i, i * 10);
        }
        
        assertEquals(10, scoreRecord.getRecordCount());
        assertEquals(50, scoreRecord.getBestSteps(5));
        assertEquals(100, scoreRecord.getBestSteps(10));
    }
}
