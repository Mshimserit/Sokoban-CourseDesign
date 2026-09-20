package logic;

/**
 * 游戏地图类，表示推箱子游戏的地图数据
 * 地图元素定义：
 * 0 - 空地（可通行）
 * 1 - 墙（不可通行）
 * 2 - 箱子
 * 3 - 目的地
 * 4 - 玩家
 * 5 - 箱子在目的地上
 * 6 - 玩家在目的地上
 */
public class GameMap {
    private int[][] map; // 地图数据
    private int width; // 地图宽度
    private int height; // 地图高度
    private int playerX; // 玩家X坐标
    private int playerY; // 玩家Y坐标
    private int boxCount; // 箱子总数
    private int targetCount; // 目的地总数

    /**
     * 构造函数，创建指定大小的空地图
     * 
     * @param width  地图宽度
     * @param height 地图高度
     */
    public GameMap(int width, int height) {
        this.width = width;
        this.height = height;
        this.map = new int[height][width];
        this.boxCount = 0;
        this.targetCount = 0;
    }

    /**
     * 构造函数，从已有地图数据创建
     * 
     * @param map 地图数据
     */
    public GameMap(int[][] map) {
        this.height = map.length;
        this.width = map[0].length;
        this.map = new int[height][width];
        for (int i = 0; i < height; i++) {
            System.arraycopy(map[i], 0, this.map[i], 0, width);
        }
        analyzeMap();
    }

    /**
     * 分析地图，统计箱子、目的地数量，找到玩家位置
     */
    private void analyzeMap() {
        boxCount = 0;
        targetCount = 0;
        playerX = -1;
        playerY = -1;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int element = map[y][x];
                // 统计箱子（2=箱子，5=箱子在目的地）
                if (element == 2 || element == 5) {
                    boxCount++;
                }
                // 统计目的地（3=目的地，5=箱子在目的地，6=玩家在目的地）
                if (element == 3 || element == 5 || element == 6) {
                    targetCount++;
                }
                // 找到玩家位置（4=玩家，6=玩家在目的地）
                if (element == 4 || element == 6) {
                    playerX = x;
                    playerY = y;
                }
            }
        }
    }

    /**
     * 获取指定位置的地图元素
     * 
     * @param x X坐标
     * @param y Y坐标
     * @return 地图元素值
     */
    public int getElement(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return 1; // 越界视为墙
        }
        return map[y][x];
    }

    /**
     * 设置指定位置的地图元素
     * 
     * @param x     X坐标
     * @param y     Y坐标
     * @param value 元素值
     */
    public void setElement(int x, int y, int value) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            map[y][x] = value;
        }
    }

    /**
     * 检查指定位置是否可通行
     * 
     * @param x X坐标
     * @param y Y坐标
     * @return 是否可通行
     */
    public boolean isPassable(int x, int y) {
        int element = getElement(x, y);
        return element == 0 || element == 3 || element == 6;
    }

    /**
     * 检查是否所有箱子都在目的地上
     * 
     * @return 是否完成
     */
    public boolean isCompleted() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // 如果有目的地没有箱子，则未完成
                if (map[y][x] == 3 || map[y][x] == 6) {
                    return false;
                }
            }
        }
        return boxCount > 0 && boxCount == targetCount;
    }

    /**
     * 获取地图数据的深拷贝
     * 
     * @return 地图数据副本
     */
    public int[][] getMapCopy() {
        int[][] copy = new int[height][width];
        for (int i = 0; i < height; i++) {
            System.arraycopy(map[i], 0, copy[i], 0, width);
        }
        return copy;
    }

    // Getters
    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getPlayerX() {
        return playerX;
    }

    public int getPlayerY() {
        return playerY;
    }

    public int getBoxCount() {
        return boxCount;
    }

    public int getTargetCount() {
        return targetCount;
    }

    /**
     * 设置玩家位置
     * 
     * @param x X坐标
     * @param y Y坐标
     */
    public void setPlayerPosition(int x, int y) {
        this.playerX = x;
        this.playerY = y;
    }

    /**
     * 重新分析地图（用于地图编辑后）
     */
    public void refresh() {
        analyzeMap();
    }

    /**
     * 检测是否存在死局（箱子被推到角落）
     * 死局定义：箱子在角落（至少两个相邻方向是墙）
     * 
     * @return 是否存在死局
     */
    public boolean isDeadlock() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int element = map[y][x];
                // 检查普通箱子（2）和箱子在目的地（5）
                if (element == 2 || element == 5) {
                    if (isCornerDeadlock(x, y)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 检查指定位置是否是角落死局
     * 角落定义：至少有两个相邻方向（上下或左右）被墙封死
     * 
     * @param x X坐标
     * @param y Y坐标
     * @return 是否是角落死局
     */
    public boolean isCornerDeadlock(int x, int y) {
        boolean wallUp = getElement(x, y - 1) == 1;
        boolean wallDown = getElement(x, y + 1) == 1;
        boolean wallLeft = getElement(x - 1, y) == 1;
        boolean wallRight = getElement(x + 1, y) == 1;

        // 角落：上下+左 或 上下+右 或 左右+上 或 左右+下
        return (wallUp && wallLeft) || (wallUp && wallRight) ||
                (wallDown && wallLeft) || (wallDown && wallRight);
    }

    /**
     * 获取死局箱子的位置列表（用于调试或高亮显示）
     * 
     * @return 死局箱子位置数组，每个元素为 [x, y]
     */
    public int[][] getDeadlockBoxes() {
        java.util.List<int[]> deadlocks = new java.util.ArrayList<>();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (map[y][x] == 2 && isCornerDeadlock(x, y)) {
                    deadlocks.add(new int[] { x, y });
                }
            }
        }
        return deadlocks.toArray(new int[0][]);
    }
}
