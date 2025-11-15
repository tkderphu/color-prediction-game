package site.viosmash.client.ui;

import site.viosmash.common.MatchPlayer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

/**
 * @author Nguyen Quang Phu
 * @since 12/10/2025
 */
public class LeaderboardFrame extends JFrame {

    private long matchId;

    public LeaderboardFrame(long matchId, List<MatchPlayer> leaderboard) {
        this.matchId = matchId;
        initComponents(leaderboard);
        setupFrame();
    }

    private void setupFrame() {
        setTitle("Bảng xếp hạng - Trận đấu #" + matchId);
        setSize(700, 500);
        setLocationRelativeTo(null);
        setResizable(true);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    private void initComponents(List<MatchPlayer> leaderboard) {
        Color backgroundColor = new Color(255, 246, 195);
        Color headerColor = new Color(220, 120, 50);
        Color headerTextColor = Color.WHITE;

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                Graphics2D g2 = (Graphics2D) g.create();

                // 1. Draw background normally
                try {
                    ImageIcon backgroundImage = new ImageIcon(getClass().getResource("/background.jpg"));
                    g2.drawImage(backgroundImage.getImage(), 0, 0, getWidth(), getHeight(), this);
                } catch (Exception e) {
                    g2.setColor(backgroundColor);
                    g2.fillRect(0, 0, getWidth(), getHeight());
                }

                // 2. Draw overlay (darken background to highlight text)
                float overlayOpacity = 0.45f; // adjust 0.25 - 0.45 if needed
                g2.setComposite(AlphaComposite.SrcOver.derive(overlayOpacity));
                g2.setColor(Color.BLACK);
                g2.fillRect(0, 0, getWidth(), getHeight());

                g2.dispose();
            }
        };
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Bảng Xếp Hạng - Trận #" + matchId, JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(new EmptyBorder(0, 0, 20, 0));

        String[] columnNames = {"Hạng", "Tên người chơi", "Tổng điểm", "Thời gian (s)"};

        Object[][] leaderboardData = buildLeaderboardData(leaderboard);

        JTable leaderboardTable = new JTable(leaderboardData, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        leaderboardTable.setRowHeight(40);
        leaderboardTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        leaderboardTable.setSelectionBackground(new Color(220, 120, 50, 100));
        leaderboardTable.setSelectionForeground(Color.BLACK);
        leaderboardTable.setGridColor(new Color(200, 200, 200));
        leaderboardTable.setShowGrid(true);
        leaderboardTable.setIntercellSpacing(new Dimension(1, 1));

        JTableHeader header = leaderboardTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(headerColor);
        header.setForeground(headerTextColor);
        header.setPreferredSize(new Dimension(header.getWidth(), 45));

        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setBackground(headerColor);
                setForeground(headerTextColor);
                setHorizontalAlignment(JLabel.CENTER);
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 2, 1, new Color(200, 200, 200)),
                        BorderFactory.createEmptyBorder(0, 10, 0, 10)
                ));
                return this;
            }
        });

        leaderboardTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                setHorizontalAlignment(JLabel.CENTER);
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

                if (!isSelected) {
                    if (row % 2 == 0) {
                        setBackground(new Color(255, 255, 255, 220));
                    } else {
                        setBackground(new Color(245, 245, 245, 220));
                    }
                }

                if (row == 0) {
                    setBackground(new Color(255, 248, 225));
                    setFont(new Font("Segoe UI", Font.BOLD, 14));
                    if (column == 0) setText("" + value);
                } else if (row == 1) {
                    setBackground(new Color(248, 248, 248));
                    setFont(new Font("Segoe UI", Font.BOLD, 14));
                    if (column == 0) setText("" + value);
                } else if (row == 2) {
                    setBackground(new Color(255, 245, 238));
                    setFont(new Font("Segoe UI", Font.BOLD, 14));
                    if (column == 0) setText("" + value);
                } else {
                    if (column == 0) setText(value + ".");
                }

                return this;
            }
        });

        leaderboardTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        leaderboardTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        leaderboardTable.getColumnModel().getColumn(2).setPreferredWidth(120);
        leaderboardTable.getColumnModel().getColumn(3).setPreferredWidth(120);

        JScrollPane scrollPane = new JScrollPane(leaderboardTable);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setOpaque(false);

        JPanel statsPanel = createStatsPanel(leaderboard);

        JButton closeButton = createStyledButton("Đóng", new Color(108, 117, 125));
        closeButton.addActionListener(e -> dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(new EmptyBorder(15, 0, 0, 0));
        buttonPanel.add(closeButton);

        mainPanel.add(titleLabel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setOpaque(false);
        southPanel.add(statsPanel, BorderLayout.NORTH);
        southPanel.add(buttonPanel, BorderLayout.SOUTH);

        mainPanel.add(southPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private JPanel createStatsPanel(List<MatchPlayer> leaderboard) {
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        statsPanel.setOpaque(false);
        statsPanel.setBorder(new EmptyBorder(15, 0, 0, 0));

        if (leaderboard != null && !leaderboard.isEmpty()) {
            int totalPlayers = leaderboard.size();
            float maxScore = leaderboard.get(0).getTotalScore();
            String winner = leaderboard.get(0).getUser().getUsername();

            JLabel playersLabel = createStatLabel("Tổng số người chơi: " + totalPlayers);
            JLabel winnerLabel = createStatLabel("Người chiến thắng: " + winner);
            JLabel scoreLabel = createStatLabel("Điểm cao nhất: " + maxScore);

            statsPanel.add(playersLabel);
            statsPanel.add(winnerLabel);
            statsPanel.add(scoreLabel);
        } else {
            JLabel noDataLabel = createStatLabel("Không có dữ liệu thống kê");
            statsPanel.add(noDataLabel);
        }

        return statsPanel;
    }

    private JLabel createStatLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(Color.WHITE);
        label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 120, 50), 1),
                BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        label.setBackground(new Color(220, 120, 50));
        label.setOpaque(true);
        return label;
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 15, 15));
                super.paintComponent(g2);
                g2.dispose();
            }
        };

        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(bgColor);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setPreferredSize(new Dimension(120, 40));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor);
            }
        });

        return button;
    }

    private Object[][] buildLeaderboardData(List<MatchPlayer> leaderboard) {
        if (leaderboard == null || leaderboard.isEmpty()) {
            return new Object[][] {
                    {"1", "Không có dữ liệu", "-", "-"}
            };
        }

        Object[][] rows = new Object[leaderboard.size()][4];
        for (int i = 0; i < leaderboard.size(); i++) {
            MatchPlayer player = leaderboard.get(i);
            rows[i][0] = i + 1;
            rows[i][1] = player.getUser().getUsername();
            rows[i][2] = player.getTotalScore();
            rows[i][3] = String.format("%.2f", player.getTotalTimeMs() / 1000.0);
        }
        return rows;
    }

    public void setBackgroundImage(String imagePath) {
        repaint();
    }
}
