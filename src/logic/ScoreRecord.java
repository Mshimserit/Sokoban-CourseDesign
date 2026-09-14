package logic;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * 分数记录类，负责管理每个关卡的最高分（最少步数）
 * 数据持久化到文件
 */
public class ScoreRecord {
    private Map<Integer, Integer> scores; // 关卡编号 -> 最佳步数
    private String filePath; // 分数文件路径

    /**
     * 构造函数
     * 
     * @param filePath 分数文件路径
     */
    public ScoreRecord(String filePath) {
        this.filePath = filePath;
        this.scores = new HashMap<>();
    }

    /**
     * 从文件加载分数
     * 
     * @throws IOException 读取文件异常
     */
    public void loadScores() throws IOException {
        scores.clear();
        File file = new File(filePath);
        if (!file.exists()) {
            return;
        }

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), "UTF-8"))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty())
                    continue;

                String[] parts = line.split("=");
                if (parts.length == 2) {
                    try {
                        int levelNum = Integer.parseInt(parts[0].trim());
                        int bestSteps = Integer.parseInt(parts[1].trim());
                        scores.put(levelNum, bestSteps);
                    } catch (NumberFormatException e) {
                        // 忽略格式错误的行
                    }
                }
            }
        }
    }

    /**
     * 保存分数到文件
     * 
     * @throws IOException 写入文件异常
     */
    public void saveScores() throws IOException {
        File file = new File(filePath);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        try (PrintWriter pw = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(file), "UTF-8"))) {
            for (Map.Entry<Integer, Integer> entry : scores.entrySet()) {
                pw.println(entry.getKey() + "=" + entry.getValue());
            }
        }
    }

    /**
     * 获取指定关卡的最佳步数
     * 
     * @param levelNumber 关卡编号
     * @return 最佳步数，-1表示无记录
     */
    public int getBestSteps(int levelNumber) {
        return scores.getOrDefault(levelNumber, -1);
    }

    /**
     * 更新指定关卡的分数
     * 
     * @param levelNumber 关卡编号
     * @param steps       新步数
     * @return 是否刷新了记录
     */
    public boolean updateScore(int levelNumber, int steps) {
        int currentBest = scores.getOrDefault(levelNumber, -1);
        if (currentBest == -1 || steps < currentBest) {
            scores.put(levelNumber, steps);
            return true;
        }
        return false;
    }

    /**
     * 获取所有分数记录
     * 
     * @return 分数映射
     */
    public Map<Integer, Integer> getAllScores() {
        return new HashMap<>(scores);
    }

    /**
     * 获取关卡数量
     * 
     * @return 有记录的关卡数
     */
    public int getRecordCount() {
        return scores.size();
    }
}
