

package site.viosmash.client.ui;

import site.viosmash.common.MatchPlayer;
import site.viosmash.common.RoundResult;

import javax.swing.*;
        import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
        import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Nguyen Quang Phu
 * @since 12/10/2025
 */
public class RoundDetailFrame2 extends JFrame {

    private long matchId;

    public RoundDetailFrame2(long matchId, List<RoundResult> roundDetails) {
        this.matchId = matchId;
        initComponents(roundDetails);
        setupFrame();
    }

    private void setupFrame() {
        setTitle("Lịch sử từng vòng đấu của trận đấu với mã: " + matchId);
        setSize(700, 450);
        setLocationRelativeTo(null);
        setResizable(true);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    private void initComponents(List<RoundResult> roundResults) {
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

        JLabel titleLabel = new JLabel("Lịch sử từng vòng - Trận #" + matchId, JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(new EmptyBorder(0, 0, 20, 0));

        String[] columnNames = {"Vòng", "Độ khó", "Các màu của vòng đấu", "Các màu đã gửi", "Số điểm", "Thời gian"};

        Object[][] leaderboardData = buildRoundResultData(roundResults);

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
        leaderboardTable.getColumnModel().getColumn(1).setPreferredWidth(80);
        leaderboardTable.getColumnModel().getColumn(2).setPreferredWidth(250);
        leaderboardTable.getColumnModel().getColumn(3).setPreferredWidth(250);
        leaderboardTable.getColumnModel().getColumn(4).setPreferredWidth(80);
        leaderboardTable.getColumnModel().getColumn(5).setPreferredWidth(80);

        JScrollPane scrollPane = new JScrollPane(leaderboardTable);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setOpaque(false);


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
        southPanel.add(buttonPanel, BorderLayout.SOUTH);

        mainPanel.add(southPanel, BorderLayout.SOUTH);

        add(mainPanel);
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

    private Object[][] buildRoundResultData(List<RoundResult> roundDetails) {
        if (roundDetails == null || roundDetails.isEmpty()) {
            return new Object[][] {
                    {"1", "Không có dữ liệu", "-", "-", "-", "-"}
            };
        }

        Object[][] rows = new Object[roundDetails.size()][6];
        for (int i = 0; i < roundDetails.size(); i++) {
            RoundResult roundResult = roundDetails.get(i);
            if(roundResult.getSelectedColors() == null) {
                roundResult.setSelectedColors(new ArrayList<>());
            }
            rows[i][0] = roundResult.getRound().getRoundNo();
            rows[i][1] = roundResult.getRound().getLevel();
            rows[i][2] = roundResult.getRound().getColors().stream().collect(Collectors.joining(", "));
            rows[i][3] = roundResult.getSelectedColors().stream().collect(Collectors.joining(", "));
            rows[i][4] = roundResult.getScore();
            rows[i][5] = (roundResult.getTimeMs() * 1.0)/1000;
        }
        return rows;
    }

    public void setBackgroundImage(String imagePath) {
        repaint();
    }
}
