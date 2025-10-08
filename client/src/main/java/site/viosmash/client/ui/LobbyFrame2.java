package site.viosmash.client.ui;

import site.viosmash.client.NetClient;
import site.viosmash.client.utils.User;
import site.viosmash.common.UserStatus;

import javax.swing.*;
import java.awt.*;
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

    public LobbyFrame2(NetClient net, User user) {
        this.net = net;
        this.user = user;
        initComponents();
        setTitle("Phòng Chờ - Color Memory Game");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
    }

    public void onOnlineList(java.util.List<Map<String, Object>> players) {
        SwingUtilities.invokeLater(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            onlineModel.clear();
            for (Map<String, Object> p : players) {
                String u = (String) p.get("username");
                String st = (String) p.get("status");
                User user = new User();
                user.setUsername(u);
                user.setStatus(st);
                if (!u.equals(this.user.getUsername()))
                    onlineModel.addElement(user);
            }
        });
    }

    public void onRoomUpdate(String owner, List<String> members) {
        SwingUtilities.invokeLater(() -> {
            acceptedModel.clear();
            for (String m : members)
                acceptedModel.addElement(m);
        });
    }

    public void onInviteIncoming(NetClient net, String from) {
        SwingUtilities.invokeLater(() -> {
            int res = JOptionPane.showConfirmDialog(this,
                    "Bạn có nhận lời mời từ " + from + " không?", "Mời chơi",
                    JOptionPane.YES_NO_OPTION);
            boolean accepted = (res == JOptionPane.YES_OPTION);
            try {
                java.util.Map<String, Object> payload = new java.util.HashMap<>();
                payload.put("fromUsername", from);
                payload.put("accepted", accepted);
                net.send("INVITE_RESPONSE", payload);
            } catch (Exception ignored) {
            }
        });
    }

    private void initComponents() {
        setPreferredSize(new Dimension(900, 600));
        setLayout(new BorderLayout());

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, createLeftPanel(), createRightPanel());
        splitPane.setDividerLocation(450);
        add(splitPane, BorderLayout.CENTER);

        JLabel titleLabel = new JLabel("Phòng Chờ - Tìm Đối Thủ", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(0, 102, 102));
        add(titleLabel, BorderLayout.NORTH);
    }

    private JPanel createLeftPanel() {
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(new Color(255, 204, 204));
        leftPanel.setBorder(BorderFactory.createTitledBorder("Người Chơi Trực Tuyến"));

        onlineList = new JList<>(onlineModel);
        onlineList.setCellRenderer(new OnlinePlayerRenderer());
        onlineList.setFixedCellHeight(40);
        onlineList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollLeft = new JScrollPane(onlineList);
        scrollLeft.setPreferredSize(new Dimension(400, 400));
        leftPanel.add(scrollLeft, BorderLayout.CENTER);

        JPanel refreshPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton refreshButton = new JButton("Refresh Danh Sách");

        JButton inviteBtn = new JButton("Mời chơi");

        refreshButton.setBackground(new Color(0, 102, 102));
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setFont(new Font("Segoe UI", Font.BOLD, 14));

        inviteBtn.setBackground(new Color(0, 102, 102));
        inviteBtn.setForeground(Color.WHITE);
        inviteBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));

        inviteBtn.addActionListener(e -> {
            User user = onlineList.getSelectedValue();
            if (user == null)
                return;
            try {
                java.util.Map<String, Object> payload = new java.util.HashMap<>();
                String username = user.getUsername();
                payload.put("toUsername", username);
                net.send("INVITE", payload);
            } catch (Exception ignored) {
            }
        });

        refreshButton.addActionListener(e -> {
            System.out.println("Manual refresh - User: " + user.getUsername());
        });
        refreshPanel.add(refreshButton);
        refreshPanel.add(inviteBtn);
        leftPanel.add(refreshPanel, BorderLayout.SOUTH);

        return leftPanel;
    }

    private JPanel createRightPanel() {
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(new Color(255, 204, 204));
        rightPanel.setBorder(BorderFactory.createTitledBorder("Thành Viên Phòng"));

        acceptedList = new JList<>(acceptedModel);
        acceptedList.setBackground(Color.WHITE);
        acceptedList.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JScrollPane scrollRight = new JScrollPane(acceptedList);
        scrollRight.setPreferredSize(new Dimension(400, 400));
        rightPanel.add(scrollRight, BorderLayout.CENTER);

        return rightPanel;
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

        public OnlinePlayerRenderer() {
            setLayout(new BorderLayout());
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

            JPanel textPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            textPanel.setOpaque(false);
            nameLabel = new JLabel();
            nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            statusLabel = new JLabel();
            statusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
            statusLabel.setForeground(Color.GRAY);
            textPanel.add(nameLabel);
            textPanel.add(statusLabel);
            add(textPanel, BorderLayout.CENTER);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends User> list,
                User user, int index,
                boolean isSelected, boolean cellHasFocus) {
            if (user != null) {
                nameLabel.setText(user.getUsername());
                statusLabel.setText("      " + (user.getStatus() != null ? user.getStatus() : "free"));
            }
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                nameLabel.setForeground(list.getSelectionForeground());
                statusLabel.setForeground(list.getSelectionForeground());
            } else {
                setBackground(Color.WHITE);
                nameLabel.setForeground(Color.BLACK);
                statusLabel.setForeground(Color.GRAY);
            }
            return this;
        }
    }

    public static void main(String args[]) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        User testUser = new User();
        testUser.setUsername("TestUser");
        testUser.setStatus("free");
        java.awt.EventQueue.invokeLater(() -> new LobbyFrame2(null, testUser).setVisible(true));
    }
}