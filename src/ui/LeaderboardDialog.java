package ui;

import logic.ScoreRecord;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Map;

/**
 * 排行榜对话框，显示每个关卡的最佳成绩
 */
public class LeaderboardDialog extends JDialog {
    private ScoreRecord scoreRecord;
    private int totalLevels;

    /**
     * 构造函数
     * @param parent 父窗口
     * @param scoreRecord 分数记录
     * @param totalLevels 总关卡数
     */
    public LeaderboardDialog(JFrame parent, ScoreRecord scoreRecord, int totalLevels) {
        super(parent, "排行榜", true);
        this.scoreRecord = scoreRecord;
        this.totalLevels = totalLevels;

        setSize(400, 500);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        initComponents();
    }

    /**
     * 初始化界面组件
     */
    private void initComponents() {
        // 标题
        JLabel titleLabel = new JLabel("排行榜", SwingConstants.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 20));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(titleLabel, BorderLayout.NORTH);

        // 表格
        String[] columnNames = {"关卡", "最佳步数", "状态"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        Map<Integer, Integer> scores = scoreRecord.getAllScores();
        for (int i = 1; i <= totalLevels; i++) {
            int bestSteps = scores.getOrDefault(i, -1);
            String status = (bestSteps == -1) ? "未完成" : "已完成";
            String stepsStr = (bestSteps == -1) ? "-" : String.valueOf(bestSteps);
            tableModel.addRow(new Object[]{"第" + i + "关", stepsStr, status});
        }

        JTable table = new JTable(tableModel);
        table.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 14));

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // 底部按钮
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton closeBtn = new JButton("关闭");
        closeBtn.addActionListener(e -> dispose());
        buttonPanel.add(closeBtn);
        add(buttonPanel, BorderLayout.SOUTH);
    }
}
