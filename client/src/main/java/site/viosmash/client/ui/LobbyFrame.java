// client/src/main/java/com/cgo/client/ui/LobbyFrame.java
package site.viosmash.client.ui;

import site.viosmash.client.NetClient;
import site.viosmash.common.Json;
import site.viosmash.common.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;

public class LobbyFrame extends JFrame {
    private final NetClient net;
    private final DefaultListModel<String> onlineModel = new DefaultListModel<>();
    private final DefaultListModel<String> roomModel = new DefaultListModel<>();
    private final JButton inviteBtn = createStyledButton("🎮 Mời chơi", new Color(46, 204, 113));
    private final JButton startBtn = createStyledButton("🚀 Bắt đầu (chủ phòng)", new Color(52, 152, 219));
    private final JButton leaveBtn = createStyledButton("🚪 Thoát phòng", new Color(231, 76, 60));
    private final User user;
    private final JLabel statusLabel = new JLabel("Đang kết nối...", JLabel.CENTER);
    private JList<String> onlineList;
    private JList<String> roomList;

    public LobbyFrame(NetClient net, User user) {
        super("Phòng chờ - " + user.getUsername());
        this.user = user;
        this.net = net;

        setupFrame();
        initComponents();
        setupEventListeners();
    }

    private void setupFrame() {
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        getContentPane().setBackground(new Color(240, 242, 245));

        ImageIcon icon = createDefaultIcon();
        if (icon != null) {
            setIconImage(icon.getImage());
        }
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        ((JComponent) getContentPane()).setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        JPanel mainPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        mainPanel.setBackground(new Color(240, 242, 245));

        JPanel onlinePanel = createOnlinePanel();
        mainPanel.add(onlinePanel);

        JPanel roomPanel = createRoomPanel();
        mainPanel.add(roomPanel);

        add(mainPanel, BorderLayout.CENTER);

        JPanel controlPanel = createControlPanel();
        add(controlPanel, BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(52, 73, 94));
        header.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JLabel titleLabel = new JLabel("🎯 CGO Game Lobby", JLabel.LEFT);
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

    private JPanel createOnlinePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel title = new JLabel("👥 Người chơi trực tuyến");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(new Color(44, 62, 80));
        panel.add(title, BorderLayout.NORTH);

        onlineList = new JList<>(onlineModel);
        onlineList.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        onlineList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        onlineList.setBackground(new Color(250, 250, 250));
        onlineList.setCellRenderer(new PlayerListCellRenderer());

        JScrollPane scrollPane = new JScrollPane(onlineList);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        scrollPane.getViewport().setBackground(Color.WHITE);

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(inviteBtn, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createRoomPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel title = new JLabel("🏠 Thành viên phòng");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(new Color(44, 62, 80));
        panel.add(title, BorderLayout.NORTH);

        roomList = new JList<>(roomModel);
        roomList.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        roomList.setBackground(new Color(250, 250, 250));
        roomList.setCellRenderer(new RoomListCellRenderer());

        JScrollPane scrollPane = new JScrollPane(roomList);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        scrollPane.getViewport().setBackground(Color.WHITE);

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(240, 242, 245));
        panel.setBorder(new EmptyBorder(10, 0, 0, 0));

        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(127, 140, 141));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setBackground(new Color(240, 242, 245));
        buttonPanel.add(startBtn);
        buttonPanel.add(leaveBtn);

        panel.add(statusLabel, BorderLayout.NORTH);
        panel.add(buttonPanel, BorderLayout.CENTER);

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
        button.setPreferredSize(new Dimension(180, 40));

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

    private void setupEventListeners() {
        inviteBtn.addActionListener(e -> {
            String target = onlineList.getSelectedValue();
            if (target == null) {
                showMessage("Vui lòng chọn người chơi để mời!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                HashMap<String, String> payload = new HashMap<>();
                String username = target.split("\\s+")[0];
                payload.put("toUsername", username);
                net.send("INVITE", payload);
                showMessage("Đã gửi lời mời đến " + username, "Thành công", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ignored) {
                showMessage("Gửi lời mời thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        startBtn.addActionListener(e -> {
            try {
                HashMap<String, String> payload = new HashMap<>();
                net.send("START_GAME", payload);
            } catch (Exception ignored) {
                showMessage("Không thể bắt đầu trò chơi!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        leaveBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Bạn có chắc muốn rời phòng?",
                    "Xác nhận",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    HashMap<String, String> payload = new HashMap<>();
                    payload.put("username", user.getUsername());
                    net.send("LEAVE_ROOM", payload);
                } catch (Exception ignored) {}
                finally {
                    roomModel.clear();
                    statusLabel.setText("Đã rời phòng");
                }
            }
        });
    }

    private static class PlayerListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            label.setIcon(createStatusIcon(value.toString()));
            return label;
        }

        private Icon createStatusIcon(String text) {
            if (text.contains("trực tuyến")) {
                return new StatusIcon(new Color(46, 204, 113));
            } else if (text.contains("đang chơi")) {
                return new StatusIcon(new Color(230, 126, 34));
            } else {
                return new StatusIcon(new Color(149, 165, 166));
            }
        }
    }

    private static class RoomListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            if (value.toString().contains("chủ phòng")) {
                label.setIcon(new CrownIcon());
                label.setFont(label.getFont().deriveFont(Font.BOLD));
            } else {
                label.setIcon(new UserIcon());
            }
            return label;
        }
    }

    private static class StatusIcon implements Icon {
        private final Color color;

        public StatusIcon(Color color) {
            this.color = color;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.fillOval(x, y + 2, 8, 8);
            g2.dispose();
        }

        @Override
        public int getIconWidth() { return 12; }

        @Override
        public int getIconHeight() { return 12; }
    }

    private static class CrownIcon implements Icon {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(241, 196, 15));

            int[] xPoints = {x, x+3, x+6, x+9, x+12};
            int[] yPoints = {y+8, y+2, y+6, y+2, y+8};
            g2.fillPolygon(xPoints, yPoints, 5);
            g2.dispose();
        }

        @Override
        public int getIconWidth() { return 14; }

        @Override
        public int getIconHeight() { return 12; }
    }

    private static class UserIcon implements Icon {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(52, 152, 219));
            g2.fillOval(x, y, 10, 10);
            g2.dispose();
        }

        @Override
        public int getIconWidth() { return 12; }

        @Override
        public int getIconHeight() { return 12; }
    }

    private ImageIcon createDefaultIcon() {
        BufferedImage image = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(52, 152, 219));
        g2.fillOval(0, 0, 32, 32);
        g2.dispose();
        return new ImageIcon(image);
    }

    private void showMessage(String message, String title, int messageType) {
        JOptionPane.showMessageDialog(this, message, title, messageType);
    }


    public void onOnlineList(List<User> players) {
        SwingUtilities.invokeLater(() -> {
            onlineModel.clear();
            for (User p : players) {
                String u = p.getUsername();
                String st = p.getStatus();
                if (!u.equals(user.getUsername())) {
                    String statusText = "online".equals(st) ? "trực tuyến" :
                            "ingame".equals(st) ? "đang chơi" : "không hoạt động";
                    onlineModel.addElement(u + " (" + statusText + ")");
                }
            }
            statusLabel.setText("Cập nhật danh sách người chơi - " + players.size() + " người online");
        });
    }

    public DefaultListModel<String> getRoomModel() {
        return roomModel;
    }

    public void onRoomUpdate(User owner, List<User> members) {
        SwingUtilities.invokeLater(() -> {
            roomModel.clear();
            for (User m : members) {
                if (owner != null && owner.equals(m)) {
                    roomModel.addElement(owner.getUsername() + " - chủ phòng");
                } else {
                    roomModel.addElement(m.getUsername());
                }
            }
            statusLabel.setText("Phòng có " + members.size() + " thành viên");
        });
    }

    public void onInviteIncoming(NetClient net, User from) {
        SwingUtilities.invokeLater(() -> {

            JPanel panel = new JPanel(new BorderLayout(10, 10));
            panel.setBorder(new EmptyBorder(10, 10, 10, 10));

            JLabel messageLabel = new JLabel("<html><div style='text-align: center;'>" +
                    "<b>" + from.getUsername() + "</b> mời bạn chơi game!<br>" +
                    "Bạn có đồng ý không?</div></html>", JLabel.CENTER);
            messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));

            panel.add(messageLabel, BorderLayout.CENTER);

            int res = JOptionPane.showOptionDialog(this,
                    panel,
                    "🎮 Lời mời chơi game",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    new Object[]{"✅ Đồng ý", "❌ Từ chối"},
                    "✅ Đồng ý");

            boolean accepted = (res == JOptionPane.YES_OPTION);
            try {
                HashMap<String, String> payload = new HashMap<>();
                payload.put("fromUser", Json.to(from));
                payload.put("invitedUser", Json.to(user));
                payload.put("accepted", Json.to(accepted));
                net.send("INVITE_RESPONSE", payload);
            } catch (Exception ignored) {}
        });
    }
}