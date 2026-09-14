package logic;

/**
 * 关卡类，表示一个游戏关卡
 */
public class Level {
    private int levelNumber;           // 关卡编号
    private String name;               // 关卡名称
    private GameMap gameMap;           // 游戏地图
    private int bestSteps;             // 最佳步数（最高分）

    /**
     * 构造函数
     * @param levelNumber 关卡编号
     * @param name 关卡名称
     * @param gameMap 游戏地图
     */
    public Level(int levelNumber, String name, GameMap gameMap) {
        this.levelNumber = levelNumber;
        this.name = name;
        this.gameMap = gameMap;
        this.bestSteps = -1; // -1表示尚无记录
    }

    /**
     * 获取关卡初始地图的副本
     * @return 地图数据副本
     */
    public int[][] getInitialMap() {
        return gameMap.getMapCopy();
    }

    // Getters and Setters
    public int getLevelNumber() { return levelNumber; }
    public void setLevelNumber(int levelNumber) { this.levelNumber = levelNumber; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public GameMap getGameMap() { return gameMap; }
    public void setGameMap(GameMap gameMap) { this.gameMap = gameMap; }
    public int getBestSteps() { return bestSteps; }
    public void setBestSteps(int bestSteps) { this.bestSteps = bestSteps; }

    /**
     * 更新最佳步数
     * @param steps 新步数
     * @return 是否刷新了记录
     */
    public boolean updateBestSteps(int steps) {
        if (bestSteps == -1 || steps < bestSteps) {
            bestSteps = steps;
            return true;
        }
        return false;
    }
}
