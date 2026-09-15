package ui;

import logic.GameMap;

import javax.swing.*;
import java.awt.*;

/**
 * 游戏面板类，负责绘制游戏地图
 * 使用 Swing 图形界面绘制墙块、箱子、玩家和目的地
 * 格子大小根据面板当前尺寸自适应，地图始终在面板中央显示
 */
public class GamePanel extends JPanel {
    private GameMap gameMap; // 当前游戏地图

    /** 自适应时格子大小的上限（像素），避免小地图被放得过大 */
    private static final int MAX_TILE_SIZE = 64;
    /** 自适应时格子大小的下限（像素），保证元素仍可辨认 */
    private static final int MIN_TILE_SIZE = 8;

    // 颜色定义
    private static final Color COLOR_WALL = new Color(80, 80, 80);
    private static final Color COLOR_FLOOR = new Color(240, 240, 240);
    private static final Color COLOR_BOX = new Color(210, 150, 60);
    private static final Color COLOR_BOX_ON_TARGET = new Color(80, 180, 80);
    private static final Color COLOR_PLAYER = new Color(60, 120, 220);
    private static final Color COLOR_GRID = new Color(200, 200, 200);

    /**
     * 构造函数
     */
    public GamePanel() {
        setBackground(Color.WHITE);
    }

    /**
     * 设置游戏地图
     * 
     * @param gameMap 游戏地图
     */
    public void setGameMap(GameMap gameMap) {
        this.gameMap = gameMap;
        repaint();
    }

    /**
     * 绘制组件：根据面板当前宽高计算格子尺寸并居中绘制
     * 窗口拉伸时地图随之缩放，始终完整可见且居中
     * 
     * @param g 图形上下文
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (gameMap == null) {
            return;
        }

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int mapW = gameMap.getWidth();
        int mapH = gameMap.getHeight();

        // 自适应格子大小：取宽、高两个方向能完整容纳地图的较小值
        int tile = Math.min(getWidth() / mapW, getHeight() / mapH);
        tile = Math.max(MIN_TILE_SIZE, Math.min(tile, MAX_TILE_SIZE));

        // 计算居中偏移量
        int offsetX = (getWidth() - tile * mapW) / 2;
        int offsetY = (getHeight() - tile * mapH) / 2;

        for (int y = 0; y < mapH; y++) {
            for (int x = 0; x < mapW; x++) {
                int element = gameMap.getElement(x, y);
                int px = offsetX + x * tile;
                int py = offsetY + y * tile;
                drawTile(g2d, element, px, py, tile);
            }
        }
    }

    /**
     * 绘制单个格子
     * 
     * @param g2d     图形上下文
     * @param element 地图元素
     * @param px      X像素坐标
     * @param py      Y像素坐标
     * @param tile    当前格子像素大小
     */
    private void drawTile(Graphics2D g2d, int element, int px, int py, int tile) {
        // 绘制背景
        switch (element) {
            case 1: // 墙
                g2d.setColor(COLOR_WALL);
                g2d.fillRect(px, py, tile, tile);
                drawWallPattern(g2d, px, py, tile);
                return;
            case 3: // 目的地
            case 6: // 玩家在目的地
                g2d.setColor(COLOR_FLOOR);
                g2d.fillRect(px, py, tile, tile);
                drawTarget(g2d, px, py, tile);
                break;
            default:
                g2d.setColor(COLOR_FLOOR);
                g2d.fillRect(px, py, tile, tile);
                break;
        }

        // 绘制元素
        switch (element) {
            case 2: // 箱子
                drawBox(g2d, px, py, tile, COLOR_BOX);
                break;
            case 4: // 玩家
                drawPlayer(g2d, px, py, tile);
                break;
            case 5: // 箱子在目的地
                drawBox(g2d, px, py, tile, COLOR_BOX_ON_TARGET);
                break;
            case 6: // 玩家在目的地
                drawPlayer(g2d, px, py, tile);
                break;
        }

        // 绘制网格线
        g2d.setColor(COLOR_GRID);
        g2d.drawRect(px, py, tile, tile);
    }

    /**
     * 绘制墙壁纹理（砖块图案）
     * 使用裁剪区域限制绘制范围，防止纹理溢出格子边界
     * 
     * @param g2d  图形上下文
     * @param px   X像素坐标
     * @param py   Y像素坐标
     * @param tile 当前格子像素大小
     */
    private void drawWallPattern(Graphics2D g2d, int px, int py, int tile) {
        g2d.setColor(new Color(100, 100, 100));
        // 裁剪到当前格子范围内，避免砖块线条画到相邻格子
        Shape oldClip = g2d.getClip();
        g2d.clipRect(px, py, tile, tile);

        int brickH = tile / 2;
        int brickW = tile / 2;
        for (int row = 0; row < 2; row++) {
            int offset = (row % 2 == 0) ? 0 : brickW / 2;
            for (int col = -1; col < 3; col++) {
                int bx = px + col * brickW + offset;
                int by = py + row * brickH;
                g2d.drawRect(bx, by, brickW, brickH);
            }
        }

        g2d.setClip(oldClip);
    }

    /**
     * 绘制目的地标记
     * 
     * @param g2d  图形上下文
     * @param px   X像素坐标
     * @param py   Y像素坐标
     * @param tile 当前格子像素大小
     */
    private void drawTarget(Graphics2D g2d, int px, int py, int tile) {
        g2d.setColor(new Color(220, 80, 80));
        int cx = px + tile / 2;
        int cy = py + tile / 2;
        int r = tile / 4;
        g2d.fillOval(cx - r, cy - r, r * 2, r * 2);
        g2d.setColor(new Color(255, 120, 120));
        int r2 = r / 2;
        g2d.fillOval(cx - r2, cy - r2, r2 * 2, r2 * 2);
    }

    /**
     * 绘制箱子
     * 
     * @param g2d   图形上下文
     * @param px    X像素坐标
     * @param py    Y像素坐标
     * @param tile  当前格子像素大小
     * @param color 箱子颜色
     */
    private void drawBox(Graphics2D g2d, int px, int py, int tile, Color color) {
        int margin = Math.max(2, tile / 10);
        g2d.setColor(color);
        g2d.fillRoundRect(px + margin, py + margin,
                tile - margin * 2, tile - margin * 2, 6, 6);
        // 绘制X标记
        g2d.setColor(color.darker());
        g2d.drawLine(px + margin + 4, py + margin + 4,
                px + tile - margin - 4, py + tile - margin - 4);
        g2d.drawLine(px + tile - margin - 4, py + margin + 4,
                px + margin + 4, py + tile - margin - 4);
    }

    /**
     * 绘制玩家
     * 
     * @param g2d  图形上下文
     * @param px   X像素坐标
     * @param py   Y像素坐标
     * @param tile 当前格子像素大小
     */
    private void drawPlayer(Graphics2D g2d, int px, int py, int tile) {
        int cx = px + tile / 2;
        int cy = py + tile / 2;
        int r = tile / 3;

        // 身体
        g2d.setColor(COLOR_PLAYER);
        g2d.fillOval(cx - r, cy - r, r * 2, r * 2);

        // 眼睛
        g2d.setColor(Color.WHITE);
        int eyeR = Math.max(1, r / 4);
        g2d.fillOval(cx - r / 2 - eyeR, cy - r / 3 - eyeR, eyeR * 2, eyeR * 2);
        g2d.fillOval(cx + r / 2 - eyeR, cy - r / 3 - eyeR, eyeR * 2, eyeR * 2);

        // 瞳孔
        g2d.setColor(Color.BLACK);
        int pupilR = Math.max(1, eyeR / 2);
        g2d.fillOval(cx - r / 2 - pupilR, cy - r / 3 - pupilR, pupilR * 2, pupilR * 2);
        g2d.fillOval(cx + r / 2 - pupilR, cy - r / 3 - pupilR, pupilR * 2, pupilR * 2);
    }
}
