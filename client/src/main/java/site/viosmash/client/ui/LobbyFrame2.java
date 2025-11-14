package site.viosmash.client.ui;

import site.viosmash.client.NetClient;
import site.viosmash.common.Json;
import site.viosmash.common.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;
import java.util.Map;
import java.util.Timer;

public class LobbyFrame2 extends JFrame {

    private final NetClient net;
    private User user;
    private final DefaultListModel<User> onlineModel = new DefaultListModel<>();
    private final DefaultListModel<String> acceptedModel = new DefaultListModel<>();
    private Timer refreshTimer;
    private boolean isLoading = false;
    private JList<User> onlineList;
    private JList<String> acceptedList;
    private JButton inviteBtn;
    private JButton refreshButton;
    private JButton startBtn;
    private JButton leaveBtn;

    public LobbyFrame2(NetClient net, User user) {
        this.net = net;
        this.user = user;
        initComponents();
        setTitle("Phòng Chờ - Color Memory Game");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
    }

    public void onOnlineList(java.util.List<Map<String,Object>> players) {
        SwingUtilities.invokeLater(() -> {
            onlineModel.clear();
            for (Map<String, Object> p : players) {
                String u = (String)p.get("username");
                String st = (String)p.get("status");
                User user = new User();
                user.setUsername(u);
                user.setStatus(st);
                if (!u.equals(this.user.getUsername())) onlineModel.addElement(user);
            }
            updateStatusLabel();
        });
    }

    public void onRoomUpdate(String owner, List<String> members) {
        SwingUtilities.invokeLater(() -> {
            acceptedModel.clear();
            for (String m : members) {
                if (owner != null && owner.equals(m)) {
                    acceptedModel.addElement("👑 " + m + " (Chủ phòng)");
                } else {
                    acceptedModel.addElement("👤 " + m);
                }
            }
            updateStatusLabel();
        });
    }

    public void onInviteIncoming(NetClient net, String from) {
        SwingUtilities.invokeLater(() -> {
            JPanel panel = new JPanel(new BorderLayout(10, 10));
            panel.setBorder(new EmptyBorder(15, 15, 15, 15));

            JLabel iconLabel = new JLabel("🎮", JLabel.CENTER);
            iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 48));

            JLabel messageLabel = new JLabel("<html><div style='text-align: center;'>" +
                    "<b>" + from + "</b> mời bạn chơi game!<br>" +
                    "Bạn có đồng ý không?</div></html>", JLabel.CENTER);
            messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));

            panel.add(iconLabel, BorderLayout.NORTH);
            panel.add(messageLabel, BorderLayout.CENTER);

            int res = JOptionPane.showOptionDialog(this,
                    panel,
                    "🎯 Lời mời chơi game",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    new Object[]{"✅ Đồng ý", "❌ Từ chối"},
                    "✅ Đồng ý");

            boolean accepted = (res == JOptionPane.YES_OPTION);
            try {
                Map<String, String> payload = new java.util.HashMap<>();
                payload.put("fromUsername", Json.to(from));
                payload.put("accepted", Json.to(accepted));
                net.send("INVITE_RESPONSE", payload);

                if (accepted) {
                    showMessage("Đã chấp nhận lời mời từ " + from, "Thành công", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception ignored) {}
        });
    }

    private void initComponents() {
        setPreferredSize(new Dimension(1000, 700));
        getContentPane().setBackground(new Color(255, 246, 195));
        setLayout(new BorderLayout(10, 10));
        ((JComponent) getContentPane()).setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, createLeftPanel(), createRightPanel());
        splitPane.setDividerLocation(500);
        splitPane.setDividerSize(8);
        splitPane.setBackground(new Color(255, 246, 195));
        add(splitPane, BorderLayout.CENTER);

        JPanel controlPanel = createControlPanel();
        add(controlPanel, BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(52, 73, 94));
        header.setBorder(new EmptyBorder(10, 15, 10, 15));

        JLabel titleLabel = new JLabel("Phòng Chờ Color Memory", JLabel.LEFT);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);

        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        userPanel.setBackground(new Color(52, 73, 94));

        JLabel userLabel = new JLabel("👤 " + user.getUsername());
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        userLabel.setForeground(Color.WHITE);

        userPanel.add(userLabel);

        header.add(titleLabel, BorderLayout.WEST);
        header.add(userPanel, BorderLayout.EAST);

        return header;
    }

    private JPanel createLeftPanel() {
        JPanel leftPanel = new JPanel(new BorderLayout(0, 10));
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel title = new JLabel("👥 Người chơi trực tuyến");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(new Color(44, 62, 80));
        leftPanel.add(title, BorderLayout.NORTH);

        onlineList = new JList<>(onlineModel);
        onlineList.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        onlineList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        onlineList.setBackground(new Color(250, 250, 250));
        onlineList.setCellRenderer(new OnlinePlayerRenderer());
        onlineList.setFixedCellHeight(50);

        JScrollPane scrollLeft = new JScrollPane(onlineList);
        scrollLeft.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        scrollLeft.getViewport().setBackground(Color.WHITE);
        leftPanel.add(scrollLeft, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setBackground(Color.WHITE);

        refreshButton = createStyledButton("🔄 Làm mới", new Color(108, 117, 125));
        inviteBtn = createStyledButton("🎮 Mời chơi", new Color(46, 204, 113));

        refreshButton.addActionListener(e -> refreshOnlineList());
        inviteBtn.addActionListener(e -> invitePlayer());

        buttonPanel.add(refreshButton);
        buttonPanel.add(inviteBtn);

        leftPanel.add(buttonPanel, BorderLayout.SOUTH);

        return leftPanel;
    }

    private JPanel createRightPanel() {
        JPanel rightPanel = new JPanel(new BorderLayout(0, 10));
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel title = new JLabel("🏠 Thành viên phòng");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(new Color(44, 62, 80));
        rightPanel.add(title, BorderLayout.NORTH);

        acceptedList = new JList<>(acceptedModel);
        acceptedList.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        acceptedList.setBackground(new Color(250, 250, 250));
        acceptedList.setCellRenderer(new RoomMemberRenderer());
        acceptedList.setFixedCellHeight(45);

        JScrollPane scrollRight = new JScrollPane(acceptedList);
        scrollRight.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        scrollRight.getViewport().setBackground(Color.WHITE);
        rightPanel.add(scrollRight, BorderLayout.CENTER);

        return rightPanel;
    }

    private JPanel createControlPanel() {
        JPanel controlPanel = new JPanel(new BorderLayout());
        controlPanel.setBackground(new Color(255, 246, 195));
        controlPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        JLabel statusLabel = new JLabel("🟢 Đang kết nối - " + onlineModel.size() + " người online");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(127, 140, 141));

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        actionPanel.setBackground(new Color(255, 246, 195));

        startBtn = createStyledButton("🚀 Bắt đầu game", new Color(52, 152, 219));
        leaveBtn = createStyledButton("🚪 Rời phòng", new Color(231, 76, 60));

        startBtn.addActionListener(e -> startGame());
        leaveBtn.addActionListener(e -> leaveRoom());

        actionPanel.add(startBtn);
        actionPanel.add(leaveBtn);

        controlPanel.add(statusLabel, BorderLayout.NORTH);
        controlPanel.add(actionPanel, BorderLayout.CENTER);

        return controlPanel;
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
        button.setPreferredSize(new Dimension(180, 45));
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

    private void refreshOnlineList() {
        refreshButton.setText("⏳ Đang tải...");
        refreshButton.setEnabled(false);

        Timer timer = new Timer();
        timer.schedule(new java.util.TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> {
                    refreshButton.setText("🔄 Làm mới");
                    refreshButton.setEnabled(true);
                });
            }
        }, 1000);

        try {
            Map<String, String> payload = new java.util.HashMap<>();
            net.send("REFRESH_ONLINE", payload);
        } catch (Exception ex) {
            showMessage("Lỗi khi làm mới danh sách", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void invitePlayer() {
        User selectedUser = onlineList.getSelectedValue();
        if (selectedUser == null) {
            showMessage("Vui lòng chọn người chơi để mời!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if ("ingame".equals(selectedUser.getStatus())) {
            showMessage(selectedUser.getUsername() + " đang trong trận đấu!", "Không thể mời", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Map<String, String> payload = new java.util.HashMap<>();
            payload.put("toUsername", selectedUser.getUsername());
            net.send("INVITE", payload);
            showMessage("Đã gửi lời mời đến " + selectedUser.getUsername(), "Thành công", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            showMessage("Gửi lời mời thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void startGame() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Bắt đầu trận đấu?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                Map<String, String> payload = new java.util.HashMap<>();
                net.send("START_GAME", payload);
            } catch (Exception ex) {
                showMessage("Không thể bắt đầu trò chơi!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void leaveRoom() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn rời phòng?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                Map<String, String> payload = new java.util.HashMap<>();
                payload.put("username", user.getUsername());
                net.send("LEAVE_ROOM", payload);
            } catch (Exception ignored) {}
        }
    }

    private void updateStatusLabel() {

    }

    private void showMessage(String message, String title, int messageType) {
        JOptionPane.showMessageDialog(this, message, title, messageType);
    }

    @Override
    public void dispose() {
        if (refreshTimer != null) {
            refreshTimer.cancel();
            refreshTimer = null;
        }
        super.dispose();
    }

    class OnlinePlayerRenderer extends JPanel implements ListCellRenderer<User> {
        private JLabel nameLabel;
        private JLabel statusLabel;
        private JLabel statusIcon;

        public OnlinePlayerRenderer() {
            setLayout(new BorderLayout(10, 0));
            setBackground(Color.WHITE);
            setBorder(new EmptyBorder(8, 10, 8, 10));

            statusIcon = new JLabel();
            statusIcon.setPreferredSize(new Dimension(12, 12));

            JPanel textPanel = new JPanel(new BorderLayout());
            textPanel.setOpaque(false);

            nameLabel = new JLabel();
            nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));

            statusLabel = new JLabel();
            statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            statusLabel.setForeground(new Color(127, 140, 141));

            textPanel.add(nameLabel, BorderLayout.NORTH);
            textPanel.add(statusLabel, BorderLayout.SOUTH);

            add(statusIcon, BorderLayout.WEST);
            add(textPanel, BorderLayout.CENTER);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends User> list,
                                                      User user, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            if (user != null) {
                nameLabel.setText(user.getUsername());
                String status = user.getStatus() != null ? user.getStatus() : "free";
                String statusText = "online".equals(status) ? "🟢 Trực tuyến" :
                        "ingame".equals(status) ? "🎮 Đang chơi" : "⚫ Không hoạt động";
                statusLabel.setText(statusText);

                if ("online".equals(status)) {
                    statusIcon.setText("●");
                    statusIcon.setForeground(new Color(46, 204, 113));
                } else if ("ingame".equals(status)) {
                    statusIcon.setText("●");
                    statusIcon.setForeground(new Color(230, 126, 34));
                } else {
                    statusIcon.setText("●");
                    statusIcon.setForeground(new Color(149, 165, 166));
                }
            }

            if (isSelected) {
                setBackground(new Color(220, 240, 255));
                setBorder(BorderFactory.createLineBorder(new Color(52, 152, 219), 1));
            } else {
                setBackground(index % 2 == 0 ? Color.WHITE : new Color(250, 250, 250));
                setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
            }
            return this;
        }
    }

    class RoomMemberRenderer extends JPanel implements ListCellRenderer<String> {
        private JLabel memberLabel;

        public RoomMemberRenderer() {
            setLayout(new BorderLayout());
            setBackground(Color.WHITE);
            setBorder(new EmptyBorder(8, 10, 8, 10));

            memberLabel = new JLabel();
            memberLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            add(memberLabel, BorderLayout.WEST);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends String> list,
                                                      String member, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            memberLabel.setText(member);

            if (member.contains("👑")) {
                memberLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
                memberLabel.setForeground(new Color(230, 126, 34));
            } else {
                memberLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                memberLabel.setForeground(Color.BLACK);
            }

            if (isSelected) {
                setBackground(new Color(220, 240, 255));
                setBorder(BorderFactory.createLineBorder(new Color(52, 152, 219), 1));
            } else {
                setBackground(index % 2 == 0 ? Color.WHITE : new Color(250, 250, 250));
                setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
            }
            return this;
        }
    }

    public static void main(String args[]) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        User testUser = new User();
        testUser.setUsername("TestUser");
        testUser.setStatus("free");
        java.awt.EventQueue.invokeLater(() -> new LobbyFrame2(null, testUser).setVisible(true));
    }
}