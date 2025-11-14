package site.viosmash.client.ui;

import site.viosmash.client.NetClient;
import site.viosmash.common.User;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class HomeFrame extends javax.swing.JFrame {

    private NetClient netClient;
    private User user;
    private LobbyFrame lobbyFrame;

    public HomeFrame() {
        initComponents();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    public HomeFrame(NetClient netClient, User user, LobbyFrame lobbyFrame) {
        this();
        this.lobbyFrame = lobbyFrame;
        setTitle("Color Memory Game - Trang chủ");
        setVisible(true);
        this.netClient = netClient;
        this.user = user;
        setUsername(user.getUsername());
    }

    private void setUsername(String username) {
        if (username != null && !username.isEmpty()) {
            jLabel1.setText("Chào, " + username);
        } else {
            jLabel1.setText("Người dùng");
        }
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {

        Color backgroundColor = new Color(255, 246, 195);
        Color buttonColor = new Color(220, 120, 50);
        Color buttonHoverColor = new Color(200, 100, 40);
        Color titleColor = new Color(60, 60, 60);
        Color textColor = new Color(80, 80, 80);

        // --------------------------------------------------------
        // UPDATED PANEL: Background image + dark overlay
        // --------------------------------------------------------
        jPanel1 = new javax.swing.JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();

                // 1. Draw the background image normally (no opacity change)
                try {
                    Image image = new ImageIcon(getClass().getResource("/background.jpg")).getImage();
                    g2.drawImage(image, 0, 0, getWidth(), getHeight(), this);
                } catch (Exception e) {
                    g2.setColor(backgroundColor);
                    g2.fillRect(0, 0, getWidth(), getHeight());
                }

                // 2. Add dark overlay to highlight foreground text
                float overlayOpacity = 0.35f;  // change to 0.25–0.45 depending on preference
                g2.setComposite(AlphaComposite.SrcOver.derive(overlayOpacity));
                g2.setColor(Color.BLACK);
                g2.fillRect(0, 0, getWidth(), getHeight());

                g2.dispose();
            }
        };
        // --------------------------------------------------------

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();

        jButton1 = new javax.swing.JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
                super.paintComponent(g2);
                g2.dispose();
            }
        };

        jButton3 = new javax.swing.JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
                super.paintComponent(g2);
                g2.dispose();
            }
        };

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(backgroundColor);
        setPreferredSize(new java.awt.Dimension(800, 500));

        jPanel1.setBackground(backgroundColor);
        jPanel1.setLayout(new java.awt.BorderLayout());

        JPanel mainPanel = new JPanel();
        mainPanel.setOpaque(false);
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(50, 100, 50, 100));

        jLabel1.setFont(new java.awt.Font("Segoe UI", Font.BOLD, 20));
        jLabel1.setForeground(Color.WHITE);
        jLabel1.setAlignmentX(Component.CENTER_ALIGNMENT);
        jLabel1.setText("tên đăng nhập");

        jLabel2.setFont(new java.awt.Font("Segoe UI", Font.BOLD, 36));
        jLabel2.setForeground(Color.WHITE);
        jLabel2.setAlignmentX(Component.CENTER_ALIGNMENT);
        jLabel2.setText("Trang chủ");
        jLabel2.setBorder(BorderFactory.createEmptyBorder(20, 0, 50, 0));

        jButton1.setBackground(buttonColor);
        jButton1.setFont(new java.awt.Font("Segoe UI", Font.BOLD, 20));
        jButton1.setForeground(Color.WHITE);
        jButton1.setText("Lịch sử chơi game");
        jButton1.setBorderPainted(false);
        jButton1.setFocusPainted(false);
        jButton1.setContentAreaFilled(false);
        jButton1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButton1.setPreferredSize(new java.awt.Dimension(400, 60));
        jButton1.setMaximumSize(new java.awt.Dimension(400, 60));
        jButton1.setAlignmentX(Component.CENTER_ALIGNMENT);

        jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jButton1.setBackground(buttonHoverColor);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jButton1.setBackground(buttonColor);
            }
        });

        jButton1.addActionListener((e) -> {
            Map<String, String> map = new HashMap<>();
            map.put("username", user.getUsername());
            try {
                netClient.send("PLAYED_HISTORY", map);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

        jButton3.setBackground(buttonColor);
        jButton3.setFont(new java.awt.Font("Segoe UI", Font.BOLD, 20));
        jButton3.setForeground(Color.WHITE);
        jButton3.setText("Chơi game");
        jButton3.setBorderPainted(false);
        jButton3.setFocusPainted(false);
        jButton3.setContentAreaFilled(false);
        jButton3.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButton3.setPreferredSize(new java.awt.Dimension(400, 60));
        jButton3.setMaximumSize(new java.awt.Dimension(400, 60));
        jButton3.setAlignmentX(Component.CENTER_ALIGNMENT);

        jButton3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jButton3.setBackground(buttonHoverColor);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jButton3.setBackground(buttonColor);
            }
        });

        jButton3.addActionListener((e) -> {
            jButton3ActionPerformed(e);
        });

        mainPanel.add(jLabel1);
        mainPanel.add(jLabel2);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        mainPanel.add(jButton1);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        mainPanel.add(jButton3);

        jPanel1.add(mainPanel, BorderLayout.CENTER);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {
        JOptionPane.showMessageDialog(this, "Chức năng lịch sử chưa implement!");
    }

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {
        lobbyFrame.setVisible(true);
        this.dispose();
    }

    public void setBackgroundImage(String imagePath) {
        jPanel1.repaint();
    }

    private javax.swing.JPanel jPanel1;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
}
