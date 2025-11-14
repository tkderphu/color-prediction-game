package site.viosmash.client.ui;

import site.viosmash.client.NetClient;
import site.viosmash.common.*;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.*;
import java.util.List;

public class GameFrame extends JFrame {

    private final NetClient net;
    private Round round;
    private long serverEpoch;

    private final DefaultListModel<String> membersModel;
    private final JLabel infoLabel = new JLabel(" ", JLabel.CENTER);
    private final JPanel colorPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
    private final JPanel selectPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
    private final JButton submitBtn = createStyledButton("Gửi kết quả", new Color(46, 204, 113));
    private final JLabel timerLabel = new JLabel("0.0s", JLabel.CENTER);
    private final JButton chooseColorBtn = createStyledButton("Chọn màu", new Color(155, 89, 182));
    private final List<Color> selectedColors = new ArrayList<>();
    private Timer hideTimer, countTimer;

    private DefaultTableModel tableModel;
    private JTable table;
    private final List<User> players;

    public GameFrame(String username, List<User> players, NetClient net, DefaultListModel<String> membersModel) {
        super("🎮 " + username + " - Color Memory Game");
        this.net = net;
        this.players = players;
        this.membersModel = membersModel;

        setupFrame();
        initComponents();

        // Button actions
        chooseColorBtn.addActionListener(e -> openColorChooserDialog());
        submitBtn.addActionListener(e -> submit());
    }

    private void setupFrame() {
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(true);
    }

    private void initComponents() {
        getContentPane().setBackground(new Color(255, 246, 195));
        setLayout(new BorderLayout(10, 10));
        ((JComponent) getContentPane()).setBorder(new EmptyBorder(15, 15, 15, 15));

        // Header panel
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Main content
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, createLeftPanel(), createRightPanel());
        splitPane.setDividerLocation(350);
        splitPane.setDividerSize(8);
        splitPane.setBackground(new Color(255, 246, 195));
        add(splitPane, BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(52, 73, 94));
        header.setBorder(new EmptyBorder(10, 15, 10, 15));

        JLabel titleLabel = new JLabel("Color Memory Game", JLabel.LEFT);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);

        // Info and timer panel
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 0));
        infoPanel.setBackground(new Color(52, 73, 94));

        infoLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        infoLabel.setForeground(Color.WHITE);

        timerLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        timerLabel.setForeground(new Color(255, 255, 255, 200));
        timerLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 100), 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        infoPanel.add(infoLabel);
        infoPanel.add(timerLabel);

        header.add(titleLabel, BorderLayout.WEST);
        header.add(infoPanel, BorderLayout.EAST);

        return header;
    }

    private JPanel createLeftPanel() {
        JPanel left = new JPanel(new BorderLayout(0, 10));
        left.setBackground(Color.WHITE);
        left.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel title = new JLabel("Bảng xếp hạng");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(new Color(44, 62, 80));
        left.add(title, BorderLayout.NORTH);

        String[] columns = {"Người chơi", "Điểm", "Thời gian (s)"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(35);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setBackground(new Color(250, 250, 250));
        table.setGridColor(new Color(200, 200, 200));
        table.setShowGrid(true);

        // Table header styling
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(new Color(220, 120, 50));
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getWidth(), 40));

        // Custom cell renderer
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(JLabel.CENTER);
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

                if (!isSelected) {
                    if (row % 2 == 0) {
                        setBackground(Color.WHITE);
                    } else {
                        setBackground(new Color(248, 249, 250));
                    }
                }
                return this;
            }
        };

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        left.add(scrollPane, BorderLayout.CENTER);

        return left;
    }

    private JPanel createRightPanel() {
        JPanel right = new JPanel(new BorderLayout(10, 10));
        right.setBackground(Color.WHITE);
        right.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                new EmptyBorder(15, 15, 15, 15)
        ));

        // Control buttons panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        controlPanel.setBackground(Color.WHITE);
        controlPanel.add(chooseColorBtn);
        controlPanel.add(submitBtn);

        // Color display panel
        JPanel gamePanel = new JPanel(new BorderLayout(10, 15));
        gamePanel.setBackground(Color.WHITE);

        JPanel colorDisplayPanel = wrapPanel("Dãy màu cần nhớ", colorPanel);
        JPanel selectedDisplayPanel = wrapPanel("Màu đã chọn", selectPanel);

        gamePanel.add(colorDisplayPanel, BorderLayout.CENTER);
        gamePanel.add(selectedDisplayPanel, BorderLayout.SOUTH);

        right.add(controlPanel, BorderLayout.NORTH);
        right.add(gamePanel, BorderLayout.CENTER);

        return right;
    }

    private JPanel wrapPanel(String title, JComponent comp) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(200, 200, 200)),
                        title,
                        0, 0,
                        new Font("Segoe UI", Font.BOLD, 14)
                ),
                new EmptyBorder(10, 10, 10, 10)
        ));
        panel.setBackground(new Color(250, 250, 250));
        panel.add(comp, BorderLayout.CENTER);
        return panel;
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
        button.setPreferredSize(new Dimension(160, 45));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Hover effect
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

    // -------------------- Round Handling --------------------
    public void onRoundData(Round round, long serverEpoch) {
        this.round = round;
        this.serverEpoch = serverEpoch;
        selectedColors.clear();

        SwingUtilities.invokeLater(() -> {
            infoLabel.setText(String.format(
                    "Vòng %d [%s] • Hiển thị: %.1fs • Trả lời: %.1fs",
                    round.getRoundNo(),
                    round.getLevel(),
                    round.getShowMs() / 1000.0,
                    round.getCountDownMs() / 1000.0
            ));

            displayColors(round.getColors());
            startHideTimer(round.getShowMs());
            startCountdown(round.getCountDownMs());

            submitBtn.setText("Gửi kết quả");
            submitBtn.setEnabled(true);
            chooseColorBtn.setEnabled(true);
            revalidate();
            repaint();
        });
    }

    private void displayColors(List<String> colors) {
        colorPanel.removeAll();
        selectPanel.removeAll();

        int labelSize = 70;
        int borderRadius = 15;

        for (String c : colors) {
            JLabel label = new JLabel("", JLabel.CENTER) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(getBackground());
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), borderRadius, borderRadius));
                    super.paintComponent(g2);
                    g2.dispose();
                }
            };
            label.setOpaque(false);
            label.setBackground(mapColor(c));
            label.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(100, 100, 100), 2),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)
            ));
            label.setPreferredSize(new Dimension(labelSize, labelSize));
            label.setToolTipText(c);
            colorPanel.add(label);
        }

        colorPanel.revalidate();
        colorPanel.repaint();
    }

    private void startHideTimer(long showMs) {
        if (hideTimer != null) hideTimer.stop();

        // Progress bar for show time
        JProgressBar progressBar = new JProgressBar(0, (int) showMs);
        progressBar.setValue((int) showMs);
        progressBar.setStringPainted(true);
        progressBar.setString("Đang hiển thị...");
        progressBar.setForeground(new Color(46, 204, 113));

        Timer progressTimer = new Timer(50, null);
        progressTimer.addActionListener(e -> {
            long elapsed = System.currentTimeMillis() - progressTimer.getInitialDelay();
            int remaining = (int) Math.max(0, showMs - elapsed);
            progressBar.setValue(remaining);
            if (remaining <= 0) {
                progressTimer.stop();
            }
        });
        progressTimer.setInitialDelay(0);

        hideTimer = new Timer((int) showMs, e -> {
            colorPanel.removeAll();
            JLabel hiddenLabel = new JLabel("ĐÃ ẨN - Hãy chọn lại thứ tự!", JLabel.CENTER);
            hiddenLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
            hiddenLabel.setForeground(new Color(231, 76, 60));
            colorPanel.add(hiddenLabel);
            colorPanel.revalidate();
            colorPanel.repaint();
            progressTimer.stop();
        });
        hideTimer.setRepeats(false);
        hideTimer.start();
        progressTimer.start();
    }

    private void startCountdown(long countDownMs) {
        if (countTimer != null) countTimer.stop();
        long start = System.currentTimeMillis();
        countTimer = new Timer(100, e -> {
            long elapsed = System.currentTimeMillis() - start;
            long left = countDownMs - elapsed;
            double secondsLeft = Math.max(0, left / 1000.0);

            // Change color based on time left
            if (secondsLeft < 5) {
                timerLabel.setForeground(new Color(231, 76, 60)); // Red
            } else if (secondsLeft < 10) {
                timerLabel.setForeground(new Color(230, 126, 34)); // Orange
            } else {
                timerLabel.setForeground(new Color(255, 255, 255, 200)); // White
            }

            timerLabel.setText(String.format("%.1fs", secondsLeft));
            if (left <= 0) {
                countTimer.stop();
                timerLabel.setText("Hết giờ!");
                timerLabel.setForeground(new Color(231, 76, 60));
            }
        });
        countTimer.start();
    }

    // -------------------- Submit --------------------
    private void submit() {
        if (selectedColors.isEmpty()) {
            showMessage("Vui lòng chọn ít nhất một màu!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Map<String, String> payload = new HashMap<>();
            RoundResult roundResult = new RoundResult();
            roundResult.setRound(round);
            List<String> selectedNames = new ArrayList<>();
            for (Color c : selectedColors) selectedNames.add(getColorName(c));
            roundResult.setSelectedColors(selectedNames);

            payload.put("roundResult", Json.to(roundResult));
            payload.put("clientEpochMs", String.valueOf(System.currentTimeMillis()));

            net.send("SUBMIT_ANSWER", payload);
            submitBtn.setEnabled(false);
            chooseColorBtn.setEnabled(false);

            // Show loading state
            submitBtn.setText("Đang gửi...");

        } catch (Exception ex) {
            showMessage("Lỗi khi gửi kết quả!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // -------------------- Update Rank --------------------
    public void updateRank(List<MatchPlayer> leaderboard) {
        SwingUtilities.invokeLater(() -> {
            tableModel.setRowCount(0);
            for (MatchPlayer r : leaderboard) {
                tableModel.addRow(new Object[]{
                        r.getUser().getUsername(),
                        r.getTotalScore(),
                        String.format("%.2f", r.getTotalTimeMs() / 1000.0)
                });
            }
        });
    }

    public void onRoundResult(Map<String, Object> payload) {
        SwingUtilities.invokeLater(() -> {
            submitBtn.setText("Gửi kết quả");
            submitBtn.setEnabled(true);
            chooseColorBtn.setEnabled(true);

            Object score = payload.get("yourScore");
            Object time = payload.get("yourTimeMs");
            if (score != null || time != null) {
                String message = String.format(
                        "<html><div style='text-align: center;'>" +
                                "<b>🎊 Kết quả vòng</b><br><br>" +
                                "📊 Điểm: <b>%s</b><br>" +
                                "⏱️ Thời gian: <b>%s ms</b>" +
                                "</div></html>",
                        score, time
                );

                JOptionPane.showMessageDialog(this, message, "Kết quả", JOptionPane.INFORMATION_MESSAGE);
            }
        });
    }

    private void showMessage(String message, String title, int messageType) {
        JOptionPane.showMessageDialog(this, message, title, messageType);
    }

    // -------------------- Color Mapping --------------------
    private Color mapColor(String name) {
        switch (name.toUpperCase()) {
            case "RED": return Color.RED;
            case "GREEN": return Color.GREEN;
            case "BLUE": return Color.BLUE;
            case "YELLOW": return Color.YELLOW;
            case "ORANGE": return Color.ORANGE;
            case "CYAN": return Color.CYAN;
            case "MAGENTA": return Color.MAGENTA;
            case "PINK": return Color.PINK;
            case "GRAY": return Color.GRAY;
            case "LIGHT_GRAY": return Color.LIGHT_GRAY;
            case "DARK_GRAY": return Color.DARK_GRAY;
            case "BLACK": return Color.BLACK;
            case "WHITE": return Color.WHITE;
            case "BROWN": return new Color(139, 69, 19);
            case "LIME": return new Color(0, 255, 0);
            case "OLIVE": return new Color(128, 128, 0);
            case "PURPLE": return new Color(128, 0, 128);
            case "NAVY": return new Color(0, 0, 128);
            case "TEAL": return new Color(0, 128, 128);
            case "MAROON": return new Color(128, 0, 0);
            case "SALMON": return new Color(250, 128, 114);
            case "CORAL": return new Color(255, 127, 80);
            case "GOLD": return new Color(255, 215, 0);
            case "INDIGO": return new Color(75, 0, 130);
            case "TURQUOISE": return new Color(64, 224, 208);
            case "VIOLET": return new Color(238, 130, 238);
            default: return Color.LIGHT_GRAY;
        }
    }

    private void openColorChooserDialog() {
        List<Color> colors = showCustomColorDialog(this, selectedColors);
        if (!colors.isEmpty()) {
            selectedColors.clear();
            selectedColors.addAll(colors);
            updateSelectedColorsDisplay();
        }
    }

    private void updateSelectedColorsDisplay() {
        selectPanel.removeAll();

        int labelSize = 50;
        int borderRadius = 10;

        for (int i = 0; i < selectedColors.size(); i++) {
            Color color = selectedColors.get(i);
            JPanel colorCard = new JPanel(new BorderLayout());
            colorCard.setOpaque(false);

            JLabel colorLabel = new JLabel("", JLabel.CENTER) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(color);
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), borderRadius, borderRadius));
                    g2.setColor(Color.BLACK);
                    g2.draw(new RoundRectangle2D.Float(0, 0, getWidth()-1, getHeight()-1, borderRadius, borderRadius));
                    g2.dispose();
                }
            };
            colorLabel.setOpaque(false);
            colorLabel.setPreferredSize(new Dimension(labelSize, labelSize));

            JLabel indexLabel = new JLabel(String.valueOf(i + 1), JLabel.CENTER);
            indexLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
            indexLabel.setForeground(Color.BLACK);

            colorCard.add(colorLabel, BorderLayout.CENTER);
            colorCard.add(indexLabel, BorderLayout.SOUTH);

            selectPanel.add(colorCard);
        }

        selectPanel.revalidate();
        selectPanel.repaint();
    }

    private static List<Color> showCustomColorDialog(JFrame parent, List<Color> preSelectedColors) {
        JDialog dialog = new JDialog(parent, "🎨 Chọn màu theo thứ tự", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.getContentPane().setBackground(Color.WHITE);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(Color.WHITE);

        JLabel instruction = new JLabel("<html><div style='text-align: center;'>Chọn màu theo đúng thứ tự đã hiển thị<br><small>Click để chọn, click lại để bỏ chọn</small></div></html>", JLabel.CENTER);
        instruction.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JPanel colorGrid = new JPanel(new GridLayout(4, 7, 8, 8));
        colorGrid.setBackground(Color.WHITE);
        List<JToggleButton> buttons = new ArrayList<>();

        for (int i = 0; i < Colors.COLORS.length; i++) {
            final Color color = Colors.COLORS[i];
            final String colorName = Colors.BASE[i];

            JToggleButton btn = new JToggleButton() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(color);
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));

                    if (isSelected()) {
                        g2.setColor(Color.WHITE);
                        g2.setStroke(new BasicStroke(3));
                        g2.draw(new RoundRectangle2D.Float(2, 2, getWidth()-4, getHeight()-4, 8, 8));

                        g2.setStroke(new BasicStroke(2));
                        g2.drawLine(8, getHeight()/2, getWidth()/3, getHeight()-8);
                        g2.drawLine(getWidth()/3, getHeight()-8, getWidth()-8, 8);
                    }

                    g2.dispose();
                }
            };

            btn.setOpaque(false);
            btn.setBorderPainted(false);
            btn.setContentAreaFilled(false);
            btn.setPreferredSize(new Dimension(50, 50));
            btn.setToolTipText(colorName);

            if (preSelectedColors.contains(color)) {
                btn.setSelected(true);
            }

            buttons.add(btn);
            colorGrid.add(btn);
        }

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setBackground(Color.WHITE);

        JButton okButton = new JButton("Xong");
        JButton cancelButton = new JButton("Hủy");

        okButton.addActionListener(e -> dialog.dispose());
        cancelButton.addActionListener(e -> {
            buttons.forEach(btn -> btn.setSelected(false));
            dialog.dispose();
        });

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        mainPanel.add(instruction, BorderLayout.NORTH);
        mainPanel.add(colorGrid, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(mainPanel);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);

        List<Color> selected = new ArrayList<>();
        for (JToggleButton btn : buttons) {
            if (btn.isSelected()) selected.add(Colors.COLORS[buttons.indexOf(btn)]);
        }
        return selected;
    }

    private static String getColorName(Color color) {
        for (int i = 0; i < Colors.COLORS.length; i++) {
            if (Colors.COLORS[i].equals(color)) return Colors.BASE[i];
        }
        return "Custom";
    }

    // -------------------- Members List --------------------
    public void setMembers(List<User> ms) {
        SwingUtilities.invokeLater(() -> {
            membersModel.clear();
            ms.forEach(r -> membersModel.addElement(r.getUsername()));
        });
    }
}