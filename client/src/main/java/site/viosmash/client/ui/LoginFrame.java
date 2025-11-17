package site.viosmash.client.ui;

import site.viosmash.client.NetClient;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author Nguyen Quang Phu
 * @since 02/10/2025
 */
public class LoginFrame extends JFrame {
    private NetClient net;
    private JPanel cardPanel;
    private CardLayout cardLayout;

    private JTextField loginUsernameField;
    private JPasswordField loginPasswordField;
    private JButton loginButton;

    public LoginFrame(NetClient net, Consumer<Void> onLoggedIn) {
        super("Đăng nhập");
        this.net = net;
        initComponents();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
    }

    private void initComponents() {
        Color primaryColor = new Color(200, 134, 11);
        Color secondaryColor = Color.WHITE;
        Color accentColor = new Color(255, 255, 200);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setOpaque(false);

        JPanel loginPanel = createLoginPanel();

        cardPanel.add(loginPanel, "LOGIN");

        JPanel mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();

                try {
                    Image image = new ImageIcon(getClass().getResource("/background.jpg")).getImage();
                    g2.drawImage(image, 0, 0, getWidth(), getHeight(), this);
                } catch (Exception e) {
                    g2.setColor(new Color(248, 249, 250));
                    g2.fillRect(0, 0, getWidth(), getHeight());
                }

                float overlayOpacity = 0.45f;
                g2.setComposite(AlphaComposite.SrcOver.derive(overlayOpacity));
                g2.setColor(Color.BLACK);
                g2.fillRect(0, 0, getWidth(), getHeight());

                g2.dispose();
            }
        };

        mainPanel.setPreferredSize(new Dimension(800, 500));
        mainPanel.add(cardPanel, BorderLayout.CENTER);

        setContentPane(mainPanel);
        pack();
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        JLabel titleLabel = new JLabel("ĐĂNG NHẬP");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(40, 0, 50, 0));

        JPanel centerPanel = new JPanel();
        centerPanel.setOpaque(false);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(0, 150, 0, 150));

        JPanel formContainer = new JPanel();
        formContainer.setOpaque(false);
        formContainer.setLayout(new BoxLayout(formContainer, BoxLayout.Y_AXIS));
        formContainer.setMaximumSize(new Dimension(400, 300));
        formContainer.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel usernamePanel = createFormField("Tên đăng nhập");
        loginUsernameField = new JTextField();
        setupTextField(loginUsernameField);
        JPanel usernameFieldContainer = (JPanel) usernamePanel.getComponent(1);
        usernameFieldContainer.add(loginUsernameField);

        JPanel passwordPanel = createFormField("Mật khẩu");
        loginPasswordField = new JPasswordField();
        setupTextField(loginPasswordField);
        JPanel passwordFieldContainer = (JPanel) passwordPanel.getComponent(1);
        passwordFieldContainer.add(loginPasswordField);

        loginButton = createStyledButton("Đăng nhập", new Color(200, 134, 11));
        loginButton.addActionListener(e -> handleLogin());

        JPanel switchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        switchPanel.setOpaque(false);

        JLabel switchLabel = new JLabel("Chưa có tài khoản? ");
        switchLabel.setForeground(Color.WHITE);
        switchLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JLabel registerLink = new JLabel("Đăng ký ngay");
        registerLink.setForeground(new Color(255, 255, 200));
        registerLink.setFont(new Font("Segoe UI", Font.BOLD, 14));
        registerLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openRegisterFrame();
            }
        });

        switchPanel.add(switchLabel);
        switchPanel.add(registerLink);

        formContainer.add(usernamePanel);
        formContainer.add(Box.createRigidArea(new Dimension(0, 25)));
        formContainer.add(passwordPanel);
        formContainer.add(Box.createRigidArea(new Dimension(0, 40)));

        centerPanel.add(Box.createVerticalGlue());
        centerPanel.add(formContainer);
        centerPanel.add(Box.createVerticalGlue());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
        buttonPanel.add(loginButton);

        JPanel mainContainer = new JPanel();
        mainContainer.setOpaque(false);
        mainContainer.setLayout(new BoxLayout(mainContainer, BoxLayout.Y_AXIS));
        mainContainer.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainContainer.add(centerPanel);
        mainContainer.add(buttonPanel);
        mainContainer.add(Box.createRigidArea(new Dimension(0, 20)));
        mainContainer.add(switchPanel);

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(mainContainer, BorderLayout.CENTER);

        return panel;
    }

    private void openRegisterFrame() {
        RegisterFrame registerFrame = new RegisterFrame(net, this, () -> {
            showLoginPanel();
        });
        registerFrame.setVisible(true);
        this.setVisible(false);
    }

    public void showLoginPanel() {
        loginUsernameField.setText("");
        loginPasswordField.setText("");
    }

    private JPanel createFormField(String labelText) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setMaximumSize(new Dimension(400, 70));
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 16));
        label.setForeground(Color.WHITE);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        JPanel fieldContainer = new JPanel();
        fieldContainer.setOpaque(false);
        fieldContainer.setLayout(new BoxLayout(fieldContainer, BoxLayout.X_AXIS));
        fieldContainer.setMaximumSize(new Dimension(400, 45));
        fieldContainer.setAlignmentX(Component.LEFT_ALIGNMENT);

        JSeparator separator = new JSeparator();
        separator.setForeground(new Color(255, 255, 255, 180));
        separator.setAlignmentX(Component.LEFT_ALIGNMENT);
        separator.setMaximumSize(new Dimension(400, 2));

        panel.add(label);
        panel.add(fieldContainer);
        panel.add(separator);

        return panel;
    }

    private void setupTextField(JComponent field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        field.setBackground(new Color(255, 255, 255, 200));
        field.setForeground(Color.BLACK);
        field.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));
        field.setOpaque(true);
        field.setMaximumSize(new Dimension(400, 45));
        field.setPreferredSize(new Dimension(400, 45));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (getModel().isPressed()) {
                    g2.setColor(new Color(
                            Math.max(color.getRed() - 40, 0),
                            Math.max(color.getGreen() - 40, 0),
                            Math.max(color.getBlue() - 40, 0)
                    ));
                } else {
                    g2.setColor(color);
                }

                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
                g2.dispose();
                super.paintComponent(g);
            }
        };

        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 16));
        button.setBorder(BorderFactory.createEmptyBorder(12, 40, 12, 40));
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setPreferredSize(new Dimension(200, 45));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);

        return button;
    }

    private void handleLogin() {
        String username = loginUsernameField.getText().trim();
        String password = new String(loginPasswordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập cả tên đăng nhập và mật khẩu", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            Map<String, String> payload = new HashMap<>();
            payload.put("username", username);
            payload.put("password", password);
            net.send("LOGIN", payload);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}