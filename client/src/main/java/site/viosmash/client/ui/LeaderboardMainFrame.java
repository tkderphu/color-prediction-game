package site.viosmash.client.ui;

import site.viosmash.client.NetClient;
import site.viosmash.client.utils.User;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class LeaderboardMainFrame extends JFrame {
    private DefaultTableModel tableModel;
    private JTable table;
    private JComboBox<String> sortComboBox;
    private List<Map<String, Object>> leaderboardData;
    
    public LeaderboardMainFrame(NetClient netClient, User user) {
        setTitle("Bảng xếp hạng");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(700, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Top panel with title and sort options
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel titleLabel = new JLabel("Bảng xếp hạng", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        topPanel.add(titleLabel, BorderLayout.NORTH);
        
        // Sort panel
        JPanel sortPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        JLabel sortLabel = new JLabel("Sắp xếp theo:");
        sortLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        String[] sortOptions = {"Điểm số (cao → thấp)", "Thời gian (nhanh → chậm)"};
        sortComboBox = new JComboBox<>(sortOptions);
        sortComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        sortComboBox.addActionListener(e -> sortTable());
        
        sortPanel.add(sortLabel);
        sortPanel.add(sortComboBox);
        topPanel.add(sortPanel, BorderLayout.CENTER);
        
        add(topPanel, BorderLayout.NORTH);

        // Table setup
        String[] columnNames = {"Hạng", "Tên người chơi", "Tổng điểm", "Tổng thời gian (giây)", "Số trận"};
        Object[][] data = {};
        
        tableModel = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Read-only table
            }
            
            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 0 || column == 4) return Integer.class;
                if (column == 2) return Double.class;
                if (column == 3) return Double.class;
                return String.class;
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(35);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(0, 102, 102));
        table.getTableHeader().setForeground(Color.WHITE);
        
        // Column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(60);  // Hạng
        table.getColumnModel().getColumn(1).setPreferredWidth(200); // Tên
        table.getColumnModel().getColumn(2).setPreferredWidth(120); // Điểm
        table.getColumnModel().getColumn(3).setPreferredWidth(150); // Thời gian
        table.getColumnModel().getColumn(4).setPreferredWidth(100); // Số trận
        
        // Center align for numeric columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        
        // Custom renderer for rank column (highlight top 3)
        table.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(JLabel.CENTER);
                
                if (row == 0) {
                    c.setBackground(new Color(255, 215, 0)); // Gold
                    c.setForeground(Color.BLACK);
                } else if (row == 1) {
                    c.setBackground(new Color(192, 192, 192)); // Silver
                    c.setForeground(Color.BLACK);
                } else if (row == 2) {
                    c.setBackground(new Color(205, 127, 50)); // Bronze
                    c.setForeground(Color.BLACK);
                } else {
                    c.setBackground(Color.WHITE);
                    c.setForeground(Color.BLACK);
                }
                
                if (isSelected) {
                    c.setBackground(table.getSelectionBackground());
                }
                
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        add(scrollPane, BorderLayout.CENTER);
    }
    
    @SuppressWarnings("unchecked")
    public void updateLeaderboard(Object data) {
        tableModel.setRowCount(0);
        
        if (data == null) {
            JOptionPane.showMessageDialog(this, "Không có dữ liệu bảng xếp hạng");
            return;
        }
        
        try {
            if (data instanceof List) {
                leaderboardData = (List<Map<String, Object>>) data;
                
                if (leaderboardData.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Chưa có dữ liệu bảng xếp hạng");
                    return;
                }
                
                // Sort by default (score)
                sortTable();
            } else {
                JOptionPane.showMessageDialog(this, "Dữ liệu bảng xếp hạng không hợp lệ");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi hiển thị bảng xếp hạng: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void sortTable() {
        if (leaderboardData == null || leaderboardData.isEmpty()) {
            return;
        }
        
        tableModel.setRowCount(0);
        
        // Create a copy to sort
        List<Map<String, Object>> sortedData = new ArrayList<>(leaderboardData);
        
        int selectedIndex = sortComboBox.getSelectedIndex();
        
        if (selectedIndex == 0) {
            // Sort by score (descending)
            sortedData.sort((a, b) -> {
                double scoreA = getDoubleValue(a.get("total_score"));
                double scoreB = getDoubleValue(b.get("total_score"));
                return Double.compare(scoreB, scoreA);
            });
        } else {
            // Sort by time (ascending - faster is better)
            sortedData.sort((a, b) -> {
                double timeA = getDoubleValue(a.get("total_time_ms"));
                double timeB = getDoubleValue(b.get("total_time_ms"));
                return Double.compare(timeA, timeB);
            });
        }
        
        // Add rows to table
        int rank = 1;
        for (Map<String, Object> player : sortedData) {
            String username = (String) player.get("username");
            double totalScore = getDoubleValue(player.get("total_score"));
            long totalTimeMs = getLongValue(player.get("total_time_ms"));
            int matchCount = getIntValue(player.get("match_count"));
            
            double totalTimeSec = totalTimeMs / 1000.0;
            
            tableModel.addRow(new Object[]{
                rank++,
                username,
                String.format("%.2f", totalScore),
                String.format("%.2f", totalTimeSec),
                matchCount
            });
        }
    }
    
    private double getDoubleValue(Object obj) {
        if (obj == null) return 0.0;
        if (obj instanceof Number) {
            return ((Number) obj).doubleValue();
        }
        try {
            return Double.parseDouble(obj.toString());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
    
    private long getLongValue(Object obj) {
        if (obj == null) return 0L;
        if (obj instanceof Number) {
            return ((Number) obj).longValue();
        }
        try {
            return Long.parseLong(obj.toString());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
    
    private int getIntValue(Object obj) {
        if (obj == null) return 0;
        if (obj instanceof Number) {
            return ((Number) obj).intValue();
        }
        try {
            return Integer.parseInt(obj.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}

