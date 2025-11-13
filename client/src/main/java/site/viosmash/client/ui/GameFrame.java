package site.viosmash.client.ui;

import site.viosmash.client.NetClient;
import site.viosmash.common.*;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.util.List;

public class GameFrame extends JFrame {

    private final NetClient net;
    private Round round;
    private long serverEpoch;

    private final DefaultListModel<String> membersModel;
    private final JLabel infoLabel = new JLabel(" ");
    private final JPanel colorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    private final JPanel selectPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    private final JButton submitBtn = new JButton("Gửi kết quả");
    private final JLabel timerLabel = new JLabel(" ");
    private final JButton chooseColorBtn = new JButton("Chọn màu");
    private final List<Color> selectedColors = new ArrayList<>();
    private Timer hideTimer, countTimer;

    private DefaultTableModel tableModel;
    private JTable table;
    private final List<User> players;

    public GameFrame(String username, List<User> players, NetClient net, DefaultListModel<String> membersModel) {
        super(username + " đang chơi");
        this.net = net;
        this.players = players;
        this.membersModel = membersModel;

        setSize(800, 520);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // Left panel: leaderboard
        JPanel leftPanel = createLeftPanel(players);

        // Right panel: game UI
        JPanel rightPanel = createRightPanel();

        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.CENTER);

        // Button actions
        chooseColorBtn.addActionListener(e -> openColorChooserDialog());
        submitBtn.addActionListener(e -> submit());
    }

    // -------------------- UI Creation --------------------
    private JPanel createLeftPanel(List<User> players) {
        JPanel left = new JPanel(new BorderLayout());
        left.add(new JLabel("Người chơi"), BorderLayout.NORTH);

        String[] columns = {"Username", "Tổng điểm", "Tổng thời gian (s)"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(25);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));

        for (User player : players) {
            tableModel.addRow(new Object[]{player.getUsername(), 0, 0});
        }

        left.add(new JScrollPane(table), BorderLayout.CENTER);
        left.setPreferredSize(new Dimension(400, 0));

        return left;
    }

    private JPanel createRightPanel() {
        JPanel right = new JPanel(new BorderLayout(5, 5));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(infoLabel, BorderLayout.CENTER);
        topPanel.add(timerLabel, BorderLayout.EAST);
        topPanel.add(chooseColorBtn, BorderLayout.SOUTH);

        JPanel centerPanel = new JPanel(new GridLayout(2, 1));
        centerPanel.add(wrapPanel("Dãy màu", colorPanel));
//        centerPanel.add(wrapPanel("Chọn lại", selectPanel));

        right.add(topPanel, BorderLayout.NORTH);
        right.add(centerPanel, BorderLayout.CENTER);
        right.add(submitBtn, BorderLayout.SOUTH);

        return right;
    }

    private JPanel wrapPanel(String title, JComponent comp) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));
        panel.add(comp, BorderLayout.CENTER);
        return panel;
    }

    // -------------------- Round Handling --------------------
    public void onRoundData(Round round, long serverEpoch) {
        this.round = round;
        this.serverEpoch = serverEpoch;
        selectedColors.clear();

        SwingUtilities.invokeLater(() -> {
            infoLabel.setText(String.format(
                    "Vòng %d [%s] — hiển thị %.1fs, trả lời %.1fs",
                    round.getRoundNo(),
                    round.getLevel(),
                    round.getShowMs() / 1000.0,
                    round.getCountDownMs() / 1000.0
            ));

            displayColors(round.getColors());
            startHideTimer(round.getShowMs());
            startCountdown(round.getCountDownMs());

            submitBtn.setEnabled(true);
            revalidate();
            repaint();
        });
    }

    private void displayColors(List<String> colors) {
        colorPanel.removeAll();

        int labelWidth = 60;
        int labelHeight = 60;

        for (String c : colors) {
            JLabel label = new JLabel("");
            label.setOpaque(true);
            label.setBackground(mapColor(c));
            label.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
            label.setPreferredSize(new Dimension(labelWidth, labelHeight));
            colorPanel.add(label);
        }

        colorPanel.revalidate();
        colorPanel.repaint();
    }

    private void startHideTimer(long showMs) {
        if (hideTimer != null) hideTimer.stop();
        hideTimer = new Timer((int) showMs, e -> {
            colorPanel.removeAll();
            colorPanel.add(new JLabel("ĐÃ ẨN — hãy chọn lại thứ tự!"));
            colorPanel.revalidate();
            colorPanel.repaint();
        });
        hideTimer.setRepeats(false);
        hideTimer.start();
    }

    private void startCountdown(long countDownMs) {
        if (countTimer != null) countTimer.stop();
        long start = System.currentTimeMillis();
        countTimer = new Timer(100, e -> {
            long elapsed = System.currentTimeMillis() - start;
            long left = countDownMs - elapsed;
            timerLabel.setText(String.format("Còn: %.1fs", Math.max(0, left / 1000.0)));
            if (left <= 0) countTimer.stop();
        });
        countTimer.start();
    }

    // -------------------- Submit --------------------
    private void submit() {
        try {
            Map<String, String> payload = new HashMap<>();
            RoundResult roundResult = new RoundResult();
            roundResult.setRound(round);
            // Convert selected colors to String names for sending
            List<String> selectedNames = new ArrayList<>();
            for (Color c : selectedColors) selectedNames.add(getColorName(c));
            roundResult.setSelectedColors(selectedNames);

            payload.put("roundResult", Json.to(roundResult));
            payload.put("clientEpochMs", String.valueOf(System.currentTimeMillis()));

            net.send("SUBMIT_ANSWER", payload);
            submitBtn.setEnabled(false);
        } catch (Exception ignored) {}
    }

    // -------------------- Update Rank --------------------
    public void updateRank(List<MatchPlayer> leaderboard) {
        SwingUtilities.invokeLater(() -> {
            tableModel.setRowCount(0);
            for (MatchPlayer r : leaderboard) {
                tableModel.addRow(new Object[]{
                        r.getUser().getUsername(),
                        r.getTotalScore(),
                        r.getTotalTimeMs() / 1000.0
                });
            }
        });
    }

    public void onRoundResult(Map<String, Object> payload) {
        SwingUtilities.invokeLater(() -> {
            Object score = payload.get("yourScore");
            Object time = payload.get("yourTimeMs");
            if (score != null || time != null) {
                JOptionPane.showMessageDialog(this,
                        "Điểm vòng: " + score + " | Thời gian: " + time + " ms");
                submitBtn.setEnabled(true);
            }
        });
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

    // -------------------- Custom Color Chooser --------------------
    private void openColorChooserDialog() {
        List<Color> colors = showCustomColorDialog(this, selectedColors);
        if (!colors.isEmpty()) {
            getContentPane().setBackground(colors.get(0)); // preview first color
            selectedColors.clear();
            selectedColors.addAll(colors);

            // Print names of selected colors
            System.out.println("Selected colors:");
            for (Color c : selectedColors) {
                System.out.println(getColorName(c));
            }
        }
    }

    private static List<Color> showCustomColorDialog(JFrame parent, List<Color> preSelectedColors) {
        JPanel panel = new JPanel(new GridLayout(2, 13, 5, 5));
        List<JToggleButton> buttons = new ArrayList<>();

        for (int i = 0; i < Colors.COLORS.length; i++) {
            JToggleButton btn = new JToggleButton();
            btn.setBackground(Colors.COLORS[i]);
            btn.setToolTipText(Colors.BASE[i]);
            btn.setOpaque(true);
            btn.setBorderPainted(false);
            btn.setPreferredSize(new Dimension(40, 40));

            if (preSelectedColors.contains(Colors.COLORS[i])) {
                btn.setSelected(true);
                btn.setText("selected");
                btn.setForeground(Color.WHITE);
            }

            btn.addItemListener(e -> {
                if (btn.isSelected()) {
                    btn.setText("selected");
                    btn.setForeground(Color.WHITE);
                } else {
                    btn.setText("");
                }
            });

            buttons.add(btn);
            panel.add(btn);
        }

        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setPreferredSize(new Dimension(600, 200));

        int result = JOptionPane.showConfirmDialog(parent, scrollPane,
                "Select Colors", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        List<Color> selected = new ArrayList<>();
        if (result == JOptionPane.OK_OPTION) {
            for (JToggleButton btn : buttons) {
                if (btn.isSelected()) selected.add(btn.getBackground());
            }
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
