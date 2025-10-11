package site.viosmash.client.ui;

import site.viosmash.client.NetClient;
import site.viosmash.client.utils.User;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class PlayedHistoryFrame extends JFrame {
    private DefaultTableModel tableModel;
    private JTable table;
    private NetClient netClient;
    private User user;
    // Track open leaderboard frames by matchId
    private static final Map<String, LeaderboardFrame> openLeaderboards = new HashMap<>();
    
    public PlayedHistoryFrame(NetClient netClient, User user) throws IOException {
        this.netClient = netClient;
        this.user = user;
        
        setTitle("Lịch sử chơi game");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null);

        // Table data
        Object[][] data = {};
        String[] columnNames = {"Match ID", "Thao tác"};

        // Create table model
        tableModel = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 1; // Only "action" column is editable
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(30);

        // Add button renderer and editor for the "action" column
        table.getColumn("Thao tác").setCellRenderer(new ButtonRenderer());
        table.getColumn("Thao tác").setCellEditor(new ButtonEditor(new JCheckBox()));

        // Scroll pane
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);
    }

    // Button Renderer
    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setText("View Detail");
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            return this;
        }
    }


    @SuppressWarnings("unchecked")
    public void updateHistoryTable(Object history) {
        // Clear existing data
        tableModel.setRowCount(0);
        
        if (history == null) {
            JOptionPane.showMessageDialog(this, "Không có dữ liệu lịch sử");
            return;
        }
        
        try {
            // Parse history data - expecting List<Map<String, Object>>
            if (history instanceof List) {
                List<Map<String, Object>> historyList = (List<Map<String, Object>>) history;
                
                if (historyList.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Bạn chưa chơi trận nào");
                    return;
                }
                
                // Add each match to the table
                for (Map<String, Object> match : historyList) {
                    Object matchId = match.get("match_id");
                    if (matchId == null) {
                        matchId = match.get("matchId");
                    }
                    
                    // Add row with match_id and a placeholder for the button column
                    tableModel.addRow(new Object[]{
                        matchId != null ? matchId.toString() : "N/A",
                        "View Detail" // This will be rendered as a button
                    });
                }
            } else {
                JOptionPane.showMessageDialog(this, "Dữ liệu lịch sử không hợp lệ");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi hiển thị lịch sử: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Public method to handle leaderboard response from server
     */
    public static void handleLeaderboardResponse(String matchId, Object leaderboardData) {
        LeaderboardFrame frame = openLeaderboards.get(matchId);
        if (frame != null) {
            SwingUtilities.invokeLater(() -> frame.updateLeaderboard(leaderboardData));
        }
    }
    
    // Button Editor
    class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private String matchId;
        private boolean clicked;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton("View Detail");
            button.setOpaque(true);
            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            matchId = (String) table.getValueAt(row, 0);
            clicked = true;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (clicked) {
                // Close existing leaderboard for this match if any
                LeaderboardFrame existing = openLeaderboards.get(matchId);
                if (existing != null) {
                    existing.dispose();
                }
                // Create and show new leaderboard frame
                LeaderboardFrame frame = new LeaderboardFrame(matchId, netClient);
                openLeaderboards.put(matchId, frame);
                frame.setVisible(true);
            }
            clicked = false;
            return "View Detail";
        }

        @Override
        public boolean stopCellEditing() {
            clicked = false;
            return super.stopCellEditing();
        }
    }

    // Separate UI for displaying leaderboard
    class LeaderboardFrame extends JFrame {
        private DefaultTableModel leaderboardTableModel;
        private JTable leaderboardTable;
        private String matchId;
        
        public LeaderboardFrame(String matchId, NetClient netClient) {
            this.matchId = matchId;
            setTitle("Bảng xếp hạng - Match " + matchId);
            setSize(500, 400);
            setLocationRelativeTo(null);
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            
            // Remove from map when closed
            addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosed(java.awt.event.WindowEvent e) {
                    openLeaderboards.remove(matchId);
                }
            });

            // Create table model
            String[] columnNames = {"Hạng", "Tên người chơi", "Điểm", "Thời gian (ms)"};
            leaderboardTableModel = new DefaultTableModel(columnNames, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false; // read-only
                }
            };

            leaderboardTable = new JTable(leaderboardTableModel);
            leaderboardTable.setRowHeight(25);

            JScrollPane scrollPane = new JScrollPane(leaderboardTable);
            add(scrollPane, BorderLayout.CENTER);
            
            // Add loading label
            JLabel loadingLabel = new JLabel("Đang tải dữ liệu...", SwingConstants.CENTER);
            add(loadingLabel, BorderLayout.NORTH);

            // Request leaderboard data from server
            try {
                Map<String, Object> payload = new HashMap<>();
                payload.put("matchId", Long.parseLong(matchId));
                netClient.send("MATCH_LEADERBOARD_REQUEST", payload);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                    "Lỗi khi yêu cầu dữ liệu: " + e.getMessage(),
                    "Lỗi", 
                    JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
        
        @SuppressWarnings("unchecked")
        public void updateLeaderboard(Object leaderboardData) {
            // Clear existing data
            leaderboardTableModel.setRowCount(0);
            
            try {
                if (leaderboardData instanceof List) {
                    List<Map<String, Object>> leaderboard = (List<Map<String, Object>>) leaderboardData;
                    
                    if (leaderboard.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "Không có dữ liệu xếp hạng");
                        return;
                    }
                    
                    // Add each player to the table
                    for (Map<String, Object> player : leaderboard) {
                        Object rank = player.get("rank");
                        Object username = player.get("username");
                        Object score = player.get("total_score");
                        if (score == null) score = player.get("totalScore");
                        Object timeMs = player.get("total_time_ms");
                        if (timeMs == null) timeMs = player.get("totalTimeMs");
                        
                        leaderboardTableModel.addRow(new Object[]{
                            rank != null ? rank.toString() : "N/A",
                            username != null ? username.toString() : "N/A",
                            score != null ? String.format("%.2f", ((Number)score).doubleValue()) : "0.00",
                            timeMs != null ? timeMs.toString() : "0"
                        });
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Dữ liệu xếp hạng không hợp lệ");
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                    "Lỗi khi hiển thị xếp hạng: " + e.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

}
