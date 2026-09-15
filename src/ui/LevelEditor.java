package ui;

import logic.GameMap;
import logic.Level;
import logic.LevelManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;

/**
 * 关卡编辑器对话框，允许用户设计自定义关卡
 * 提供可视化工具栏、元素统计、加载已有关卡等功能
 */
public class LevelEditor extends JDialog {
    /** 编辑器中每个格子的像素大小（固定值，配合滚动区域使用） */
    private static final int TILE_SIZE = 40;

    private EditorPanel editorPanel;
    private JComboBox<String> toolComboBox;
    private JTextField nameField;
    private JLabel statusLabel;
    private LevelManager levelManager;
    private String levelsDir;
    private MainFrame parentFrame;

    /** 编辑工具定义 */
    private static final String[] TOOLS = {
            "墙", "箱子", "目的地", "玩家", "箱子在目的地", "玩家在目的地", "空地(橡皮擦)"
    };
    /** 工具对应的地图元素值 */
    private static final int[] TOOL_VALUES = { 1, 2, 3, 4, 5, 6, 0 };
    /** 工具对应的颜色 */
    private static final Color[] TOOL_COLORS = {
            new Color(80, 80, 80),
            new Color(210, 150, 60),
            new Color(220, 80, 80),
            new Color(60, 120, 220),
            new Color(80, 180, 80),
            new Color(140, 80, 200),
            new Color(240, 240, 240)
    };

    /**
     * 构造函数
     * 
     * @param parent       父窗口
     * @param levelManager 关卡管理器
     * @param levelsDir    关卡目录
     */
    public LevelEditor(MainFrame parent, LevelManager levelManager, String levelsDir) {
        super(parent, "关卡编辑器", true);
        this.parentFrame = parent;
        this.levelManager = levelManager;
        this.levelsDir = levelsDir;

        setSize(900, 700);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        initComponents();

        // 默认创建一个 10x10 的地图
        editorPanel.createNewMap(10, 10);
        updateStatus();
    }

    /**
     * 初始化界面组件
     */
    private void initComponents() {
        // 顶部工具栏
        JPanel toolPanel = createToolPanel();
        add(toolPanel, BorderLayout.NORTH);

        // 编辑面板
        editorPanel = new EditorPanel();
        JScrollPane scrollPane = new JScrollPane(editorPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        // 底部状态栏和按钮
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(createButtonPanel(), BorderLayout.CENTER);
        statusLabel = new JLabel("  就绪");
        statusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
        bottomPanel.add(statusLabel, BorderLayout.SOUTH);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    /**
     * 创建工具面板
     * 
     * @return 工具面板
     */
    private JPanel createToolPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEtchedBorder());

        // 第一行：关卡名称和地图尺寸
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        row1.add(new JLabel("关卡名称:"));
        nameField = new JTextField("自定义关卡", 15);
        row1.add(nameField);

        row1.add(Box.createHorizontalStrut(15));
        row1.add(new JLabel("宽:"));
        JTextField widthField = new JTextField("10", 3);
        row1.add(widthField);
        row1.add(new JLabel("高:"));
        JTextField heightField = new JTextField("10", 3);
        row1.add(heightField);

        JButton newBtn = new JButton("新建地图");
        newBtn.addActionListener(e -> {
            int confirm = JOptionPane.YES_OPTION;
            if (editorPanel.hasContent()) {
                confirm = JOptionPane.showConfirmDialog(this,
                        "当前地图内容将丢失，是否继续？", "确认", JOptionPane.YES_NO_OPTION);
            }
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    int w = Integer.parseInt(widthField.getText());
                    int h = Integer.parseInt(heightField.getText());
                    if (w >= 3 && h >= 3 && w <= 30 && h <= 30) {
                        editorPanel.createNewMap(w, h);
                        updateStatus();
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "尺寸必须在 3~30 之间", "错误", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this,
                            "请输入有效的数字", "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        row1.add(newBtn);

        JButton surroundBtn = new JButton("加围墙边框");
        surroundBtn.setToolTipText("在地图四周添加一圈墙壁");
        surroundBtn.addActionListener(e -> {
            editorPanel.surroundWithWalls();
            updateStatus();
        });
        row1.add(surroundBtn);

        panel.add(row1);

        // 第二行：工具选择和加载
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        row2.add(new JLabel("绘制工具:"));
        toolComboBox = new JComboBox<>(TOOLS);
        toolComboBox.setRenderer(new ToolCellRenderer());
        toolComboBox.setSelectedIndex(0); // 默认选择"墙"
        toolComboBox.addActionListener(e -> {
            int index = toolComboBox.getSelectedIndex();
            if (index >= 0 && index < TOOL_VALUES.length) {
                editorPanel.setCurrentTool(TOOL_VALUES[index]);
            }
        });
        row2.add(toolComboBox);

        row2.add(Box.createHorizontalStrut(15));

        // 清空按钮
        JButton clearButton = new JButton("清空地图");
        clearButton.addActionListener(e -> {
            editorPanel.clearMap();
            updateStatus();
        });
        row2.add(clearButton);

        panel.add(row2);
        return panel;
    }

    /**
     * 创建按钮面板
     * 
     * @return 按钮面板
     */
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        panel.setBorder(BorderFactory.createEtchedBorder());

        JButton saveBtn = new JButton("保存关卡");
        saveBtn.addActionListener(e -> saveLevel());
        panel.add(saveBtn);

        JButton closeBtn = new JButton("关闭");
        closeBtn.addActionListener(e -> dispose());
        panel.add(closeBtn);

        return panel;
    }

    /**
     * 更新状态栏
     */
    private void updateStatus() {
        int[] counts = editorPanel.getElementCounts();
        statusLabel.setText(String.format(
                "  墙: %d | 箱子: %d | 目的地: %d | 玩家: %d | 地图: %dx%d",
                counts[0], counts[1], counts[2], counts[3],
                editorPanel.getMapWidth(), editorPanel.getMapHeight()));
    }

    /**
     * 保存关卡
     */
    private void saveLevel() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入关卡名称", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int[][] mapData = editorPanel.getMapData();
        if (mapData == null) {
            JOptionPane.showMessageDialog(this, "请先创建地图", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 验证地图
        String error = validateMap(mapData);
        if (error != null) {
            JOptionPane.showMessageDialog(this, error, "验证失败", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 生成文件名 - 查找下一个可用的编号
        int levelNum = levelManager.getLevelCount() + 1;
        String fileName = String.format("level%02d.lvl", levelNum);
        File file = new File(levelsDir, fileName);

        // 如果文件已存在，查找下一个可用编号
        while (file.exists()) {
            levelNum++;
            fileName = String.format("level%02d.lvl", levelNum);
            file = new File(levelsDir, fileName);
        }

        // 创建关卡对象
        GameMap gameMap = new GameMap(mapData);
        Level level = new Level(levelNum, name, gameMap);

        try {
            levelManager.saveLevelToFile(level, file);
            levelManager.addLevel(level);

            JOptionPane.showMessageDialog(this,
                    "关卡已保存: " + fileName + "\n(第" + levelNum + "关)",
                    "保存成功", JOptionPane.INFORMATION_MESSAGE);

            // 通知主窗口刷新
            if (parentFrame != null) {
                parentFrame.refreshAfterLevelAdded();
            }

            // 保存成功后关闭编辑器
            dispose();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "保存失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 验证地图是否有效
     * 
     * @param mapData 地图数据
     * @return 错误信息，null表示有效
     */
    private String validateMap(int[][] mapData) {
        int playerCount = 0;
        int boxCount = 0;
        int targetCount = 0;

        for (int y = 0; y < mapData.length; y++) {
            for (int x = 0; x < mapData[y].length; x++) {
                int element = mapData[y][x];
                if (element == 4 || element == 6)
                    playerCount++;
                if (element == 2 || element == 5)
                    boxCount++;
                if (element == 3 || element == 5 || element == 6)
                    targetCount++;
            }
        }

        if (playerCount == 0)
            return "必须有一个玩家";
        if (playerCount > 1)
            return "只能有一个玩家";
        if (boxCount == 0)
            return "必须至少有一个箱子";
        if (targetCount == 0)
            return "必须至少有一个目的地";
        if (boxCount != targetCount)
            return "箱子数量(" + boxCount + ")必须等于目的地数量(" + targetCount + ")";

        return null;
    }

    /**
     * 工具下拉框的自定义渲染器，显示颜色方块
     */
    private class ToolCellRenderer implements ListCellRenderer<String> {
        private JLabel label = new JLabel();

        public ToolCellRenderer() {
            label.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends String> list,
                String value, int index, boolean isSelected, boolean cellHasFocus) {
            if (index < 0 || index >= TOOL_COLORS.length) {
                label.setText(value);
            } else {
                label.setText("  " + value);
                label.setOpaque(true);
                if (index < TOOL_VALUES.length && TOOL_VALUES[index] != 0) {
                    label.setBackground(TOOL_COLORS[index]);
                    label.setForeground(Color.WHITE);
                } else {
                    label.setBackground(list.getBackground());
                    label.setForeground(list.getForeground());
                }
            }
            label.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
            return label;
        }
    }

    /**
     * 编辑器面板内部类，负责地图绘制和鼠标交互
     */
    private class EditorPanel extends JPanel {
        private int[][] mapData;
        private int mapWidth;
        private int mapHeight;
        private int currentTool = 1; // 默认工具：墙
        private boolean isRightDragging = false;

        /**
         * 构造函数，设置鼠标事件
         */
        public EditorPanel() {
            setBackground(new Color(180, 180, 180));
            setPreferredSize(new Dimension(400, 400));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (SwingUtilities.isRightMouseButton(e)) {
                        isRightDragging = true;
                        eraseTile(e.getX(), e.getY());
                    } else {
                        paintTile(e.getX(), e.getY());
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    isRightDragging = false;
                    updateStatus();
                }
            });

            addMouseMotionListener(new MouseAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    if (isRightDragging) {
                        eraseTile(e.getX(), e.getY());
                    } else {
                        paintTile(e.getX(), e.getY());
                    }
                }
            });
        }

        /**
         * 创建新地图（全部填充为空地）
         * 
         * @param width  宽度
         * @param height 高度
         */
        public void createNewMap(int width, int height) {
            this.mapWidth = width;
            this.mapHeight = height;
            this.mapData = new int[height][width];
            setPreferredSize(new Dimension(width * TILE_SIZE,
                    height * TILE_SIZE));
            revalidate();
            repaint();
        }

        /**
         * 清空地图
         */
        public void clearMap() {
            if (mapData != null) {
                for (int y = 0; y < mapHeight; y++) {
                    for (int x = 0; x < mapWidth; x++) {
                        mapData[y][x] = 0;
                    }
                }
                repaint();
            }
        }

        /**
         * 在地图四周添加一圈墙壁
         */
        public void surroundWithWalls() {
            if (mapData == null)
                return;
            for (int x = 0; x < mapWidth; x++) {
                mapData[0][x] = 1;
                mapData[mapHeight - 1][x] = 1;
            }
            for (int y = 0; y < mapHeight; y++) {
                mapData[y][0] = 1;
                mapData[y][mapWidth - 1] = 1;
            }
            repaint();
        }

        /**
         * 检查地图是否有内容
         * 
         * @return 是否有非空地元素
         */
        public boolean hasContent() {
            if (mapData == null)
                return false;
            for (int y = 0; y < mapHeight; y++) {
                for (int x = 0; x < mapWidth; x++) {
                    if (mapData[y][x] != 0)
                        return true;
                }
            }
            return false;
        }

        /**
         * 统计各元素数量
         * 
         * @return [墙数, 箱子数, 目的地数, 玩家数]
         */
        public int[] getElementCounts() {
            int[] counts = new int[4];
            if (mapData == null)
                return counts;
            for (int y = 0; y < mapHeight; y++) {
                for (int x = 0; x < mapWidth; x++) {
                    int e = mapData[y][x];
                    if (e == 1)
                        counts[0]++;
                    else if (e == 2 || e == 5)
                        counts[1]++;
                    else if (e == 3 || e == 5 || e == 6)
                        counts[2]++;
                    else if (e == 4 || e == 6)
                        counts[3]++;
                }
            }
            return counts;
        }

        /**
         * 设置当前绘制工具
         * 
         * @param tool 工具对应的元素值
         */
        public void setCurrentTool(int tool) {
            this.currentTool = tool;
        }

        /**
         * 用当前工具绘制格子
         */
        private void paintTile(int px, int py) {
            if (mapData == null)
                return;
            int tileSize = TILE_SIZE;
            int x = px / tileSize;
            int y = py / tileSize;
            if (x >= 0 && x < mapWidth && y >= 0 && y < mapHeight) {
                mapData[y][x] = currentTool;
                repaint();
            }
        }

        /**
         * 右键擦除格子（设为空地）
         */
        private void eraseTile(int px, int py) {
            if (mapData == null)
                return;
            int tileSize = TILE_SIZE;
            int x = px / tileSize;
            int y = py / tileSize;
            if (x >= 0 && x < mapWidth && y >= 0 && y < mapHeight) {
                mapData[y][x] = 0;
                repaint();
            }
        }

        /**
         * 获取地图数据副本
         * 
         * @return 地图数据
         */
        public int[][] getMapData() {
            if (mapData == null)
                return null;
            int[][] copy = new int[mapHeight][mapWidth];
            for (int i = 0; i < mapHeight; i++) {
                System.arraycopy(mapData[i], 0, copy[i], 0, mapWidth);
            }
            return copy;
        }

        public int getMapWidth() {
            return mapWidth;
        }

        public int getMapHeight() {
            return mapHeight;
        }

        /**
         * 绘制组件
         */
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (mapData == null) {
                g.setColor(Color.GRAY);
                g.setFont(new Font("微软雅黑", Font.PLAIN, 16));
                g.drawString("请点击上方「新建地图」按钮创建地图", 20, 30);
                return;
            }

            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            int tileSize = TILE_SIZE;

            for (int y = 0; y < mapHeight; y++) {
                for (int x = 0; x < mapWidth; x++) {
                    int element = mapData[y][x];
                    int px = x * tileSize;
                    int py = y * tileSize;
                    drawEditorTile(g2d, element, px, py, tileSize);
                }
            }
        }

        /**
         * 绘制编辑器格子
         */
        private void drawEditorTile(Graphics2D g2d, int element, int px, int py, int tileSize) {
            // 背景
            g2d.setColor(new Color(240, 240, 240));
            g2d.fillRect(px, py, tileSize, tileSize);

            int m = 4; // margin
            switch (element) {
                case 1: // 墙
                    g2d.setColor(new Color(80, 80, 80));
                    g2d.fillRect(px, py, tileSize, tileSize);
                    g2d.setColor(new Color(100, 100, 100));
                    g2d.drawRect(px + 2, py + 2, tileSize - 4, tileSize / 2 - 2);
                    g2d.drawRect(px + tileSize / 2, py + tileSize / 2, tileSize / 2 - 2, tileSize / 2 - 2);
                    break;
                case 2: // 箱子
                    g2d.setColor(new Color(210, 150, 60));
                    g2d.fillRoundRect(px + m, py + m, tileSize - m * 2, tileSize - m * 2, 4, 4);
                    g2d.setColor(new Color(170, 110, 30));
                    g2d.drawRoundRect(px + m, py + m, tileSize - m * 2, tileSize - m * 2, 4, 4);
                    break;
                case 3: // 目的地
                    g2d.setColor(new Color(220, 80, 80));
                    int cr = tileSize / 4;
                    g2d.fillOval(px + tileSize / 2 - cr, py + tileSize / 2 - cr, cr * 2, cr * 2);
                    g2d.setColor(new Color(255, 150, 150));
                    int cr2 = cr / 2;
                    g2d.fillOval(px + tileSize / 2 - cr2, py + tileSize / 2 - cr2, cr2 * 2, cr2 * 2);
                    break;
                case 4: // 玩家
                    g2d.setColor(new Color(60, 120, 220));
                    g2d.fillOval(px + m, py + m, tileSize - m * 2, tileSize - m * 2);
                    g2d.setColor(Color.WHITE);
                    g2d.fillOval(px + tileSize / 2 - 4, py + tileSize / 3 - 2, 4, 4);
                    g2d.fillOval(px + tileSize / 2 + 2, py + tileSize / 3 - 2, 4, 4);
                    break;
                case 5: // 箱子在目的地
                    g2d.setColor(new Color(80, 180, 80));
                    g2d.fillRoundRect(px + m, py + m, tileSize - m * 2, tileSize - m * 2, 4, 4);
                    g2d.setColor(new Color(220, 80, 80));
                    int dr = 4;
                    g2d.fillOval(px + tileSize / 2 - dr, py + tileSize / 2 - dr, dr * 2, dr * 2);
                    break;
                case 6: // 玩家在目的地
                    g2d.setColor(new Color(220, 80, 80));
                    int tr = tileSize / 4;
                    g2d.fillOval(px + tileSize / 2 - tr, py + tileSize / 2 - tr, tr * 2, tr * 2);
                    g2d.setColor(new Color(60, 120, 220));
                    g2d.fillOval(px + m + 2, py + m + 2, tileSize - m * 2 - 4, tileSize - m * 2 - 4);
                    break;
            }

            // 网格线
            g2d.setColor(new Color(200, 200, 200));
            g2d.drawRect(px, py, tileSize, tileSize);
        }
    }
}
