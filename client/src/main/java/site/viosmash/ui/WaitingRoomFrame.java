package site.viosmash.ui;

import site.viosmash.common.instruction.Instruction;
import site.viosmash.common.instruction.Message;
import site.viosmash.common.instruction.ErrorResponse;
import site.viosmash.common.model.User;
import site.viosmash.network.ClientConnection;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class WaitingRoomFrame extends JFrame {

    private ClientConnection clientConnection;
    private User currentUser;
    private JList<User> onlineList;
    private JList<String> acceptedList;
    private DefaultListModel<User> onlineModel;
    private DefaultListModel<String> acceptedModel;
    private Timer refreshTimer;
    private boolean isLoading = false;

    public WaitingRoomFrame(ClientConnection connection, User currentUser) {
        this.clientConnection = connection;
        this.currentUser = currentUser;
        initComponents();
        setTitle("Phòng Chờ - Color Memory Game");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
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

        onlineModel = new DefaultListModel<>();
        onlineList = new JList<>(onlineModel);
        onlineList.setCellRenderer(new OnlinePlayerRenderer());
        onlineList.setFixedCellHeight(40);
        onlineList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollLeft = new JScrollPane(onlineList);
        scrollLeft.setPreferredSize(new Dimension(400, 400));
        leftPanel.add(scrollLeft, BorderLayout.CENTER);

        JPanel refreshPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton refreshButton = new JButton("Refresh Danh Sách");
        refreshButton.setBackground(new Color(0, 102, 102));
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        refreshButton.addActionListener(e -> {
            System.out.println("Manual refresh - User: " + currentUser.getUsername());
        });
        refreshPanel.add(refreshButton);
        leftPanel.add(refreshPanel, BorderLayout.SOUTH);

        return leftPanel;
    }

    private JPanel createRightPanel() {
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(new Color(255, 204, 204));
        rightPanel.setBorder(BorderFactory.createTitledBorder("Thành Viên Phòng"));

        acceptedModel = new DefaultListModel<>();
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
        public Component getListCellRendererComponent(JList<? extends User> list, User user, int index, boolean isSelected, boolean cellHasFocus) {
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

        User testUser = new User(); testUser.setUsername("TestUser");
        ClientConnection testConn = null;
        java.awt.EventQueue.invokeLater(() -> new WaitingRoomFrame(testConn, testUser).setVisible(true));
    }
}