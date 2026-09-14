package logic;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 关卡管理器类，负责加载、保存和管理关卡
 */
public class LevelManager {
    private List<Level> levels; // 关卡列表
    private int currentLevelIndex; // 当前关卡索引
    private String levelDirectory; // 关卡文件目录

    /**
     * 构造函数
     * 
     * @param levelDirectory 关卡文件目录
     */
    public LevelManager(String levelDirectory) {
        this.levels = new ArrayList<>();
        this.currentLevelIndex = 0;
        this.levelDirectory = levelDirectory;
    }

    /**
     * 从文件加载所有关卡
     * 
     * @throws IOException 读取文件异常
     */
    public void loadLevels() throws IOException {
        levels.clear();
        File dir = new File(levelDirectory);
        if (!dir.exists() || !dir.isDirectory()) {
            throw new IOException("关卡目录不存在: " + levelDirectory);
        }

        File[] files = dir.listFiles((d, name) -> name.endsWith(".lvl"));
        if (files == null || files.length == 0) {
            throw new IOException("关卡目录中没有关卡文件");
        }

        // 按文件名中的数字排序，避免字典序导致 level10 排在 level2 之前
        java.util.Arrays.sort(files, (f1, f2) -> {
            int n1 = extractFileNumber(f1.getName());
            int n2 = extractFileNumber(f2.getName());
            if (n1 != n2) {
                return Integer.compare(n1, n2);
            }
            return f1.getName().compareTo(f2.getName());
        });

        int levelNum = 1;
        for (File file : files) {
            Level level = loadLevelFromFile(file, levelNum++);
            if (level != null) {
                levels.add(level);
            }
        }
    }

    /**
     * 从文件名中提取数字，用于排序
     *
     * @param fileName 文件名
     * @return 文件名中的第一个数字，无数字时返回 Integer.MAX_VALUE
     */
    private static int extractFileNumber(String fileName) {
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\\d+").matcher(fileName);
        return matcher.find() ? Integer.parseInt(matcher.group()) : Integer.MAX_VALUE;
    }

    /**
     * 从文件加载单个关卡
     * 
     * @param file        关卡文件
     * @param levelNumber 关卡编号
     * @return 关卡对象
     * @throws IOException 读取文件异常
     */
    private Level loadLevelFromFile(File file, int levelNumber) throws IOException {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), "UTF-8"))) {
            String nameLine = br.readLine();
            if (nameLine == null)
                return null;

            String name = nameLine.trim();
            List<String> mapLines = new ArrayList<>();
            String line;
            while ((line = br.readLine()) != null) {
                mapLines.add(line);
            }

            if (mapLines.isEmpty())
                return null;

            // 去除末尾空行
            while (!mapLines.isEmpty() && mapLines.get(mapLines.size() - 1).trim().isEmpty()) {
                mapLines.remove(mapLines.size() - 1);
            }
            if (mapLines.isEmpty())
                return null;

            // 去除每行末尾空白，避免行尾多余空格在地图右侧产生多余的空列
            for (int i = 0; i < mapLines.size(); i++) {
                mapLines.set(i, mapLines.get(i).replaceAll("\\s+$", ""));
            }

            // 解析地图
            int height = mapLines.size();
            int width = 0;
            for (String l : mapLines) {
                width = Math.max(width, l.length());
            }

            int[][] map = new int[height][width];
            for (int y = 0; y < height; y++) {
                String l = mapLines.get(y);
                for (int x = 0; x < width; x++) {
                    if (x < l.length()) {
                        char c = l.charAt(x);
                        map[y][x] = charToElement(c);
                    } else {
                        map[y][x] = 1; // 行长短于最大宽度时补墙，保持地图边界封闭
                    }
                }
            }

            GameMap gameMap = new GameMap(map);
            return new Level(levelNumber, name, gameMap);
        }
    }

    /**
     * 将字符转换为地图元素
     * 
     * @param c 字符
     * @return 地图元素值
     */
    private int charToElement(char c) {
        switch (c) {
            case '#':
                return 1; // 墙
            case '$':
                return 2; // 箱子
            case '.':
                return 3; // 目的地
            case '@':
                return 4; // 玩家
            case '*':
                return 5; // 箱子在目的地上
            case '+':
                return 6; // 玩家在目的地上
            case ' ':
                return 0; // 空地
            default:
                return 0;
        }
    }

    /**
     * 将地图元素转换为字符
     * 
     * @param element 地图元素值
     * @return 字符
     */
    public static char elementToChar(int element) {
        switch (element) {
            case 1:
                return '#'; // 墙
            case 2:
                return '$'; // 箱子
            case 3:
                return '.'; // 目的地
            case 4:
                return '@'; // 玩家
            case 5:
                return '*'; // 箱子在目的地上
            case 6:
                return '+'; // 玩家在目的地上
            default:
                return ' '; // 空地
        }
    }

    /**
     * 保存关卡到文件
     * 
     * @param level 关卡对象
     * @param file  目标文件
     * @throws IOException 写入文件异常
     */
    public void saveLevelToFile(Level level, File file) throws IOException {
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"))) {
            pw.println(level.getName());
            GameMap map = level.getGameMap();
            for (int y = 0; y < map.getHeight(); y++) {
                StringBuilder sb = new StringBuilder();
                for (int x = 0; x < map.getWidth(); x++) {
                    sb.append(elementToChar(map.getElement(x, y)));
                }
                pw.println(sb.toString());
            }
        }
    }

    /**
     * 获取指定关卡
     * 
     * @param index 关卡索引
     * @return 关卡对象
     */
    public Level getLevel(int index) {
        if (index >= 0 && index < levels.size()) {
            return levels.get(index);
        }
        return null;
    }

    /**
     * 获取当前关卡
     * 
     * @return 当前关卡对象
     */
    public Level getCurrentLevel() {
        return getLevel(currentLevelIndex);
    }

    /**
     * 进入下一关
     * 
     * @return 是否成功进入下一关
     */
    public boolean nextLevel() {
        if (currentLevelIndex < levels.size() - 1) {
            currentLevelIndex++;
            return true;
        }
        return false;
    }

    /**
     * 进入上一关
     * 
     * @return 是否成功进入上一关
     */
    public boolean previousLevel() {
        if (currentLevelIndex > 0) {
            currentLevelIndex--;
            return true;
        }
        return false;
    }

    /**
     * 跳转到指定关卡
     * 
     * @param levelNumber 关卡编号（从1开始）
     * @return 是否成功跳转
     */
    public boolean goToLevel(int levelNumber) {
        int index = levelNumber - 1;
        if (index >= 0 && index < levels.size()) {
            currentLevelIndex = index;
            return true;
        }
        return false;
    }

    /**
     * 添加新关卡
     * 
     * @param level 关卡对象
     */
    public void addLevel(Level level) {
        levels.add(level);
        level.setLevelNumber(levels.size());
    }

    // Getters
    public List<Level> getLevels() {
        return levels;
    }

    public int getCurrentLevelIndex() {
        return currentLevelIndex;
    }

    public int getLevelCount() {
        return levels.size();
    }

    public String getLevelDirectory() {
        return levelDirectory;
    }
}
