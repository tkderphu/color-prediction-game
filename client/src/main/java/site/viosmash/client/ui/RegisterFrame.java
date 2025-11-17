package site.viosmash.client.ui;

import site.viosmash.client.NetClient;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author Nguyen
 * @since 02/10/2025
 */
public class RegisterFrame extends JFrame {
    private NetClient net;
    private LoginFrame loginFrame;
    private Runnable onRegisterSuccess;

    private JTextField registerUsernameField;
    private JPasswordField registerPasswordField;
    private JPasswordField registerConfirmPasswordField;
    private JButton registerButton;

    public RegisterFrame(NetClient net, LoginFrame loginFrame, Runnable onRegisterSuccess) {
        super("Đăng ký");
        this.net = net;
        this.loginFrame = loginFrame;
        this.onRegisterSuccess = onRegisterSuccess;
        initComponents();
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                loginFrame.setVisible(true);
            }
        });
    }

    private void initComponents() {
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

        mainPanel.setPreferredSize(new Dimension(800, 600));
        mainPanel.add(createRegisterPanel(), BorderLayout.CENTER);

        setContentPane(mainPanel);
        pack();
    }

    private JPanel createRegisterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JLabel titleLabel = new JLabel("ĐĂNG KÝ TÀI KHOẢN", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(30, 0, 30, 0));

        JPanel centerPanel = new JPanel();
        centerPanel.setOpaque(false);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(0, 150, 0, 150));

        JPanel formContainer = new JPanel();
        formContainer.setOpaque(false);
        formContainer.setLayout(new BoxLayout(formContainer, BoxLayout.Y_AXIS));
        formContainer.setMaximumSize(new Dimension(400, 350));
        formContainer.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel usernamePanel = createFormField("Tên đăng nhập");
        registerUsernameField = createTextField();
        addFieldToPanel(usernamePanel, registerUsernameField);

        JPanel passwordPanel = createFormField("Mật khẩu");
        registerPasswordField = createPasswordField();
        addFieldToPanel(passwordPanel, registerPasswordField);

        JPanel confirmPasswordPanel = createFormField("Xác nhận mật khẩu");
        registerConfirmPasswordField = createPasswordField();
        addFieldToPanel(confirmPasswordPanel, registerConfirmPasswordField);

        usernamePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        passwordPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        confirmPasswordPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        formContainer.add(usernamePanel);
        formContainer.add(Box.createRigidArea(new Dimension(0, 12)));
        formContainer.add(passwordPanel);
        formContainer.add(Box.createRigidArea(new Dimension(0, 12)));
        formContainer.add(confirmPasswordPanel);
        formContainer.add(Box.createRigidArea(new Dimension(0, 20)));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setOpaque(false);
        buttonPanel.setMaximumSize(new Dimension(400, 50));
        registerButton = createStyledButton("Đăng ký", new Color(200, 134, 11));
        registerButton.addActionListener(e -> handleRegister());
        buttonPanel.add(registerButton);

        JPanel switchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        switchPanel.setOpaque(false);
        switchPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        switchPanel.setMaximumSize(new Dimension(400, 30));

        JLabel switchLabel = new JLabel("Đã có tài khoản? ");
        switchLabel.setForeground(Color.WHITE);
        switchLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JLabel loginLink = new JLabel("Đăng nhập ngay");
        loginLink.setForeground(new Color(255, 255, 200));
        loginLink.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                dispose();
                loginFrame.setVisible(true);
            }
        });

        switchPanel.add(switchLabel);
        switchPanel.add(loginLink);

        centerPanel.add(Box.createVerticalGlue());
        centerPanel.add(formContainer);
        centerPanel.add(buttonPanel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        centerPanel.add(switchPanel);
        centerPanel.add(Box.createVerticalGlue());

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(centerPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createFormField(String labelText) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setMaximumSize(new Dimension(400, 65));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 16));
        label.setForeground(Color.WHITE);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));

        JPanel fieldContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        fieldContainer.setOpaque(false);
        fieldContainer.setMaximumSize(new Dimension(400, 40));
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

    private JTextField createTextField() {
        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        field.setBackground(new Color(255, 255, 255, 200));
        field.setForeground(Color.BLACK);
        field.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        field.setOpaque(true);
        Dimension size = new Dimension(400, 40);
        field.setMaximumSize(size);
        field.setPreferredSize(size);
        field.setMinimumSize(size);
        return field;
    }

    private JPasswordField createPasswordField() {
        JPasswordField field = new JPasswordField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        field.setBackground(new Color(255, 255, 255, 200));
        field.setForeground(Color.BLACK);
        field.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        field.setOpaque(true);
        Dimension size = new Dimension(400, 40);
        field.setMaximumSize(size);
        field.setPreferredSize(size);
        field.setMinimumSize(size);
        return field;
    }

    private void addFieldToPanel(JPanel fieldPanel, JComponent field) {
        JPanel fieldContainer = (JPanel) fieldPanel.getComponent(1);
        fieldContainer.add(field);
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

        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 16));
        button.setBorder(BorderFactory.createEmptyBorder(12, 40, 12, 40));
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        Dimension size = new Dimension(200, 50);
        button.setPreferredSize(size);
        button.setMaximumSize(size);
        button.setMinimumSize(size);

        return button;
    }

    private void handleRegister() {
        String username = registerUsernameField.getText().trim();
        String password = new String(registerPasswordField.getPassword()).trim();
        String confirmPassword = new String(registerConfirmPasswordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng điền đầy đủ thông tin", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (username.length() < 3) {
            JOptionPane.showMessageDialog(this, "Tên đăng nhập phải có ít nhất 3 ký tự", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Mật khẩu xác nhận không khớp", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (password.length() < 4) {
            JOptionPane.showMessageDialog(this, "Mật khẩu phải có ít nhất 4 ký tự", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            Map<String, String> payload = new HashMap<>();
            payload.put("username", username);
            payload.put("password", password);

            net.send("REGISTER", payload);
            registerButton.setEnabled(false);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            registerButton.setEnabled(true);
        }
    }

    public void onRegisterSuccess() {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, "Đăng ký thành công! Vui lòng đăng nhập.");
            registerButton.setEnabled(true);
            dispose();
            if (loginFrame != null) {
                loginFrame.setVisible(true);
            }
            if (onRegisterSuccess != null) {
                onRegisterSuccess.run();
            }
        });
    }

    public void onRegisterFailed(String errorMessage) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, "Đăng ký thất bại: " + errorMessage, "Lỗi", JOptionPane.ERROR_MESSAGE);
            registerButton.setEnabled(true);
        });
    }
}