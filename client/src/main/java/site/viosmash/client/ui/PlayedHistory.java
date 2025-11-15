package site.viosmash.client.ui;

import site.viosmash.client.NetClient;
import site.viosmash.common.Match;
import site.viosmash.common.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayedHistory extends JFrame {
    private NetClient client;
    private User user;
    private DefaultTableModel model;
    private JTable table;

    public PlayedHistory(NetClient netClient, List<Match> histories) throws IOException {
        this.client = netClient;
        initComponents(histories);
        setupFrame();
    }

    private void setupFrame() {
        setTitle("Lịch sử trận đấu");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
        setResizable(true);
    }

    private void initComponents(List<Match> histories) {
        Color backgroundColor = new Color(255, 246, 195);
        Color headerColor = new Color(220, 120, 50);
        Color headerTextColor = Color.WHITE;
        Color rowColor = new Color(255, 255, 255, 230);
        Color alternateRowColor = new Color(248, 249, 250);
        Color buttonColor = new Color(74, 144, 226);
        Color buttonHoverColor = new Color(65, 130, 210);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10)) {
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
                float overlayOpacity = 0.45f;  // change to 0.25–0.45 depending on preference
                g2.setComposite(AlphaComposite.SrcOver.derive(overlayOpacity));
                g2.setColor(Color.BLACK);
                g2.fillRect(0, 0, getWidth(), getHeight());

                g2.dispose();
            }
        };
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Lịch sử trận đấu", JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE); // DARKER TEXT FOR BETTER VISIBILITY
        titleLabel.setBorder(new EmptyBorder(0, 0, 20, 0));

        String[] columnNames = {"ID Trận", "Chủ phòng", "Bắt đầu", "Kết thúc", "Bảng xếp hạng", "Chi tiết vòng đấu"};

        model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Only the "Action" column is editable (for button)
                return column == 4 || column == 5;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return (columnIndex == 4 || columnIndex == 5) ? JButton.class : String.class;
            }
        };

        if (histories != null && !histories.isEmpty()) {
            for (Match history : histories) {
                model.addRow(new Object[]{
                        "#" + history.getId(),
                        history.getRoomOwner().getUsername(),
                        formatDateTime(history.getStartedAt()),
                        history.getEndedAt() != null ? formatDateTime(history.getEndedAt()) : "Đang chơi",
                        "Xem",
                        "Xem"
                });
            }
        } else {
            model.addRow(new Object[]{"", "Không có dữ liệu", "", "", "", ""});
        }

        table = new JTable(model);
        table.setRowHeight(40);
        table.setFont(new Font("Segoe UI", Font.BOLD, 15)); // MORE BOLD
        table.setForeground(new Color(20, 20, 20)); // MUCH DARKER TEXT
        table.setSelectionBackground(new Color(220, 120, 50, 100));
        table.setSelectionForeground(Color.BLACK);
        table.setGridColor(new Color(200, 200, 200));
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(1, 1));

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 15));
        header.setBackground(headerColor);
        header.setForeground(headerTextColor);
        header.setPreferredSize(new Dimension(header.getWidth(), 40));

        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setBackground(headerColor);
                setForeground(headerTextColor);
                setHorizontalAlignment(JLabel.CENTER);
                setBorder(BorderFactory.createMatteBorder(0, 0, 2, 1, new Color(200, 200, 200)));
                return this;
            }
        });

        // -------- STRONGER TEXT HIGHLIGHT --------
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);

                label.setHorizontalAlignment(JLabel.CENTER);
                label.setFont(new Font("Segoe UI", Font.BOLD, 15));
                label.setForeground(new Color(10, 10, 10)); // SUPER DARK → more visible

                // add subtle shadow (improves readability even on bright backgrounds)
//                label.setUI(new javax.swing.plaf.basic.BasicLabelUI() {
//                    @Override
//                    public void paint(Graphics g, JComponent c) {
//                        Graphics2D g2 = (Graphics2D) g.create();
//                        g2.setColor(new Color(255, 255, 255, 140)); // white shadow
//                        g2.drawString(label.getText(), 1, c.getHeight() - 11);
//                        g2.dispose();
//                        super.paint(g, c);
//                    }
//                });

                if (!isSelected) {
                    label.setBackground(row % 2 == 0 ? rowColor : alternateRowColor);
                }
                return label;
            }
        };

        for (int i = 0; i < table.getColumnCount() - 1; i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        // --- Button Renderer & Editor ---
        table.getColumn("Bảng xếp hạng").setCellRenderer(new ButtonRenderer(buttonColor, buttonHoverColor));
        table.getColumn("Bảng xếp hạng").setCellEditor(new ButtonEditor(new JCheckBox(), client, buttonColor, buttonHoverColor, "leaderboard"));


        // --- Button Renderer & Editor ---
        table.getColumn("Chi tiết vòng đấu").setCellRenderer(new ButtonRenderer(buttonColor, buttonHoverColor));
        table.getColumn("Chi tiết vòng đấu").setCellEditor(new ButtonEditor(new JCheckBox(), client, buttonColor, buttonHoverColor, "round_detail"));


        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        table.getColumnModel().getColumn(1).setPreferredWidth(150);
        table.getColumnModel().getColumn(2).setPreferredWidth(180);
        table.getColumnModel().getColumn(3).setPreferredWidth(180);
        table.getColumnModel().getColumn(4).setPreferredWidth(120);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setOpaque(false);

        JButton closeButton = createStyledButton("Đóng", new Color(108, 117, 125));
        closeButton.addActionListener(e -> dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        buttonPanel.add(closeButton);

        mainPanel.add(titleLabel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return dateTime.format(formatter);
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                super.paintComponent(g2);
                g2.dispose();
            }
        };

        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(bgColor);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setPreferredSize(new Dimension(120, 40));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.darker());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });

        return button;
    }

    class ButtonRenderer extends JButton implements TableCellRenderer {
        private Color normalColor;
        private Color hoverColor;

        public ButtonRenderer(Color normalColor, Color hoverColor) {
            this.normalColor = normalColor;
            this.hoverColor = hoverColor;
            setOpaque(true);
            setContentAreaFilled(true);
            setBorderPainted(false);
            setFocusPainted(false);
            setFont(new Font("Segoe UI", Font.BOLD, 12));
            setForeground(Color.WHITE);
            setBackground(normalColor);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText(value == null ? "Xem chi tiết" : value.toString());
//            setBackground(hasFocus ? hoverColor : normalColor);
            return this;
        }
    }

    class ButtonEditor extends DefaultCellEditor {
        private final JButton button;
        private int matchId;
        private boolean clicked;
        private NetClient netClient;
        private Color normalColor;
        private Color hoverColor;
        private String type;
        public ButtonEditor(JCheckBox checkBox, NetClient netClient, Color normalColor, Color hoverColor, String type) {
            super(checkBox);
            this.normalColor = normalColor;
            this.hoverColor = hoverColor;
            this.netClient = netClient;

            button = new JButton("Xem chi tiết");
            button.setOpaque(true);
            button.setContentAreaFilled(true);
            button.setBorderPainted(false);
            button.setFocusPainted(false);
            button.setFont(new Font("Segoe UI", Font.BOLD, 12));
            button.setForeground(Color.WHITE);
            button.setBackground(normalColor);
            button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            this.type = type;
            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(
                JTable table, Object value, boolean isSelected, int row, int column) {
            matchId = Integer.parseInt(table.getValueAt(row, 0).toString().replace("#", ""));
            clicked = true;
//            button.setBackground(hoverColor);
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (clicked) {
                if(type.equals("leaderboard")) {
                    Map<String, String> map = new HashMap<>();
                    map.put("matchId", matchId + "");
                    try {
                        netClient.send("MATCH_DETAIL", map);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    Map<String, String> map = new HashMap<>();
                    map.put("matchId", matchId + "");
                    try {
                        netClient.send("ROUND_DETAIL", map);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            clicked = false;
            button.setBackground(normalColor);
            return "Xem chi tiết";
        }

        @Override
        public boolean stopCellEditing() {
            clicked = false;
            button.setBackground(normalColor);
            return super.stopCellEditing();
        }
    }

    public void setBackgroundImage(String imagePath) {
        repaint();
    }
}
