package ui;

import logic.GameEngine;
import logic.GameEngine.Direction;
import logic.Level;
import logic.LevelManager;
import logic.ScoreRecord;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.border.TitledBorder;

/**
 * 主窗口类，游戏的主要界面
 */
public class MainFrame extends JFrame {
    private GamePanel gamePanel; // 游戏面板
    private JLabel levelLabel; // 关卡标签
    private JLabel stepsLabel; // 步数标签
    private JLabel timeLabel; // 时间标签
    private GameEngine gameEngine; // 游戏引擎
    private LevelManager levelManager; // 关卡管理器
    private ScoreRecord scoreRecord; // 分数记录
    private Timer gameTimer; // 游戏计时器
    private String levelsDir; // 关卡目录
    private String scoresFile; // 分数文件

    /**
     * 构造函数
     */
    public MainFrame() {
        setTitle("推箱子游戏");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // 初始化路径
        levelsDir = "levels";
        scoresFile = "scores/scores.dat";

        // 初始化组件
        initComponents();

        // 加载关卡
        loadLevels();

        // 设置窗口大小和位置（初始 1024x768，地图绘制自适应窗口大小）
        setSize(1024, 768);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(600, 500));

        // 启动游戏计时器
        startGameTimer();
    }

    /**
     * 初始化界面组件
     */
    private void initComponents() {
        // 顶部信息面板
        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);

        // 游戏面板：直接填充中部区域，地图在绘制时自适应大小并居中
        gamePanel = new GamePanel();
        gamePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(gamePanel, BorderLayout.CENTER);

        // 底部按钮面板
        JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout.SOUTH);

        // 键盘事件 - 使用KeyBindings确保焦点问题不影响输入
        InputMap inputMap = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        javax.swing.ActionMap actionMap = getRootPane().getActionMap();

        inputMap.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_UP, 0), "moveUp");
        inputMap.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_DOWN, 0), "moveDown");
        inputMap.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_LEFT, 0), "moveLeft");
        inputMap.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_RIGHT, 0), "moveRight");
        inputMap.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Z, java.awt.event.InputEvent.CTRL_DOWN_MASK),
                "undo");

        actionMap.put("moveUp", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleDirectionMove(GameEngine.Direction.UP);
            }
        });
        actionMap.put("moveDown", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleDirectionMove(GameEngine.Direction.DOWN);
            }
        });
        actionMap.put("moveLeft", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleDirectionMove(GameEngine.Direction.LEFT);
            }
        });
        actionMap.put("moveRight", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleDirectionMove(GameEngine.Direction.RIGHT);
            }
        });
        actionMap.put("undo", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                undoMove();
            }
        });
    }

    /**
     * 创建顶部信息面板
     * 
     * @return 顶部面板
     */
    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 10));
        panel.setBorder(BorderFactory.createEtchedBorder());

        levelLabel = new JLabel("关卡: 1");
        levelLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        panel.add(levelLabel);

        stepsLabel = new JLabel("步数: 0");
        stepsLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        panel.add(stepsLabel);

        timeLabel = new JLabel("时间: 00:00");
        timeLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        panel.add(timeLabel);

        return panel;
    }

    /**
     * 创建箭头按钮（使用自定义绘制确保显示正确）
     * 
     * @param direction 方向标识：UP/DOWN/LEFT/RIGHT
     * @return 配置好的按钮
     */
    private JButton createArrowButton(String direction) {
        JButton button = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();
                int size = Math.min(w, h) / 4;
                int cx = w / 2;
                int cy = h / 2;

                g2.setColor(Color.BLACK);
                int[] xPoints, yPoints;

                switch (direction) {
                    case "UP":
                        xPoints = new int[] { cx, cx - size, cx + size };
                        yPoints = new int[] { cy - size, cy + size / 2, cy + size / 2 };
                        break;
                    case "DOWN":
                        xPoints = new int[] { cx, cx - size, cx + size };
                        yPoints = new int[] { cy + size, cy - size / 2, cy - size / 2 };
                        break;
                    case "LEFT":
                        xPoints = new int[] { cx - size, cx + size / 2, cx + size / 2 };
                        yPoints = new int[] { cy, cy - size, cy + size };
                        break;
                    case "RIGHT":
                        xPoints = new int[] { cx + size, cx - size / 2, cx - size / 2 };
                        yPoints = new int[] { cy, cy - size, cy + size };
                        break;
                    default:
                        return;
                }
                g2.fillPolygon(xPoints, yPoints, 3);
            }
        };
        button.setPreferredSize(new Dimension(50, 50));
        button.setFocusPainted(false);
        button.setBackground(new Color(240, 240, 240));
        button.setOpaque(true);
        return button;
    }

    /**
     * 创建底部按钮面板
     * 
     * @return 底部面板
     */
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        panel.setBorder(BorderFactory.createEtchedBorder());

        // 方向键面板（十字布局）
        JPanel dirPanel = new JPanel(new GridBagLayout());
        dirPanel.setBorder(BorderFactory.createTitledBorder("方向"));
        GridBagConstraints gbc = new GridBagConstraints();

        JButton upBtn = createArrowButton("UP");
        upBtn.addActionListener(e -> handleDirectionMove(GameEngine.Direction.UP));
        gbc.gridx = 1;
        gbc.gridy = 0;
        dirPanel.add(upBtn, gbc);

        JButton leftBtn = createArrowButton("LEFT");
        leftBtn.addActionListener(e -> handleDirectionMove(GameEngine.Direction.LEFT));
        gbc.gridx = 0;
        gbc.gridy = 1;
        dirPanel.add(leftBtn, gbc);

        JButton downBtn = createArrowButton("DOWN");
        downBtn.addActionListener(e -> handleDirectionMove(GameEngine.Direction.DOWN));
        gbc.gridx = 1;
        gbc.gridy = 1;
        dirPanel.add(downBtn, gbc);

        JButton rightBtn = createArrowButton("RIGHT");
        rightBtn.addActionListener(e -> handleDirectionMove(GameEngine.Direction.RIGHT));
        gbc.gridx = 2;
        gbc.gridy = 1;
        dirPanel.add(rightBtn, gbc);

        panel.add(dirPanel);

        // 功能按钮面板
        JPanel funcPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        funcPanel.setBorder(BorderFactory.createTitledBorder("操作"));

        JButton prevBtn = new JButton("上一关");
        prevBtn.addActionListener(e -> previousLevel());
        funcPanel.add(prevBtn);

        JButton nextBtn = new JButton("下一关");
        nextBtn.addActionListener(e -> nextLevel());
        funcPanel.add(nextBtn);

        JButton resetBtn = new JButton("重新开始");
        resetBtn.addActionListener(e -> resetLevel());
        funcPanel.add(resetBtn);

        JButton undoBtn = new JButton("撤销");
        undoBtn.addActionListener(e -> undoMove());
        funcPanel.add(undoBtn);

        JButton editorBtn = new JButton("关卡编辑器");
        editorBtn.addActionListener(e -> openLevelEditor());
        funcPanel.add(editorBtn);

        JButton leaderboardBtn = new JButton("排行榜");
        leaderboardBtn.addActionListener(e -> openLeaderboard());
        funcPanel.add(leaderboardBtn);

        panel.add(funcPanel);

        return panel;
    }

    /**
     * 加载关卡
     */
    private void loadLevels() {
        try {
            levelManager = new LevelManager(levelsDir);
            levelManager.loadLevels();
            scoreRecord = new ScoreRecord(scoresFile);
            scoreRecord.loadScores();

            if (levelManager.getLevelCount() > 0) {
                Level firstLevel = levelManager.getCurrentLevel();
                gameEngine = new GameEngine(firstLevel);
                gamePanel.setGameMap(gameEngine.getGameMap());
                updateInfoPanel();
            } else {
                JOptionPane.showMessageDialog(this, "没有找到关卡文件！", "错误", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "加载关卡失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 处理方向键移动
     * 
     * @param direction 移动方向
     */
    private void handleDirectionMove(GameEngine.Direction direction) {
        if (gameEngine == null || gameEngine.isGameCompleted()) {
            return;
        }

        if (gameEngine.move(direction)) {
            gamePanel.repaint();
            updateInfoPanel();

            // 检查是否通关
            if (gameEngine.isGameCompleted()) {
                handleLevelComplete();
            }
        }
    }

    /**
     * 更新信息面板
     */
    private void updateInfoPanel() {
        if (gameEngine != null) {
            Level level = gameEngine.getCurrentLevel();
            levelLabel.setText("关卡: " + level.getLevelNumber());
            stepsLabel.setText("步数: " + gameEngine.getSteps());
            timeLabel.setText("时间: " + gameEngine.getFormattedTime());
        }
    }

    /**
     * 启动游戏计时器
     */
    private void startGameTimer() {
        gameTimer = new Timer();
        gameTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (gameEngine != null && gameEngine.isGameStarted() && !gameEngine.isGameCompleted()) {
                    gameEngine.updateTime();
                    SwingUtilities.invokeLater(() -> updateInfoPanel());
                }
            }
        }, 0, 1000);
    }

    /**
     * 处理关卡完成
     */
    private void handleLevelComplete() {
        // 保存分数
        Level level = gameEngine.getCurrentLevel();
        int steps = gameEngine.getSteps();
        boolean isNewRecord = scoreRecord.updateScore(level.getLevelNumber(), steps);
        try {
            scoreRecord.saveScores();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 显示通关提示
        String message = "恭喜通关！\n步数: " + steps + "\n时间: " + gameEngine.getFormattedTime();
        if (isNewRecord) {
            message += "\n\n新纪录！";
        }

        int result = JOptionPane.showConfirmDialog(this, message + "\n\n是否进入下一关？",
                "通关", JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
            nextLevel();
        }
    }

    /**
     * 进入上一关
     */
    private void previousLevel() {
        if (levelManager.previousLevel()) {
            Level level = levelManager.getCurrentLevel();
            gameEngine.loadLevel(level);
            gamePanel.setGameMap(gameEngine.getGameMap());
            updateInfoPanel();
        } else {
            JOptionPane.showMessageDialog(this, "已经是第一关了！", "提示", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * 进入下一关
     */
    private void nextLevel() {
        if (levelManager.nextLevel()) {
            Level level = levelManager.getCurrentLevel();
            gameEngine.loadLevel(level);
            gamePanel.setGameMap(gameEngine.getGameMap());
            updateInfoPanel();
        } else {
            JOptionPane.showMessageDialog(this, "恭喜！你已经通关了！", "提示", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * 重新开始当前关卡
     */
    private void resetLevel() {
        if (gameEngine != null) {
            gameEngine.resetGame();
            gamePanel.setGameMap(gameEngine.getGameMap());
            updateInfoPanel();
        }
    }

    /**
     * 撤销上一步移动
     */
    private void undoMove() {
        if (gameEngine != null && gameEngine.undo()) {
            gamePanel.setGameMap(gameEngine.getGameMap());
            updateInfoPanel();
        }
    }

    /**
     * 打开关卡编辑器
     */
    private void openLevelEditor() {
        LevelEditor editor = new LevelEditor(this, levelManager, levelsDir);
        editor.setVisible(true);
    }

    /**
     * 关卡编辑器保存后刷新主窗口
     * 由 LevelEditor 调用
     */
    public void refreshAfterLevelAdded() {
        // 跳转到列表末尾的新关卡并加载，避免重置玩家正在玩的关卡进度
        if (levelManager.getLevelCount() > 0 && gameEngine != null) {
            levelManager.goToLevel(levelManager.getLevelCount());
            Level level = levelManager.getCurrentLevel();
            gameEngine.loadLevel(level);
            gamePanel.setGameMap(gameEngine.getGameMap());
            updateInfoPanel();
        }
    }

    /**
     * 打开排行榜
     */
    private void openLeaderboard() {
        LeaderboardDialog dialog = new LeaderboardDialog(this, scoreRecord, levelManager.getLevelCount());
        dialog.setVisible(true);
    }

    /**
     * 主方法
     * 
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
