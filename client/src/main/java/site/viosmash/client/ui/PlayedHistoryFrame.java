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
    
    public PlayedHistoryFrame(NetClient netClient, User user) throws IOException {
        
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
                new LeaderboardFrame(matchId).setVisible(true);
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
    static class LeaderboardFrame extends JFrame {
        public LeaderboardFrame(String matchId) {
            setTitle("Leaderboard - Match " + matchId);
            setSize(400, 300);
            setLocationRelativeTo(null);

            // Dummy leaderboard data (you can replace with DB query)
            String[] columnNames = {"Rank", "Player Name", "Score"};
            Object[][] leaderboardData = getLeaderboardData(matchId);

            JTable leaderboardTable = new JTable(leaderboardData, columnNames);
            leaderboardTable.setRowHeight(25);
            leaderboardTable.setEnabled(false); // read-only

            JScrollPane scrollPane = new JScrollPane(leaderboardTable);
            add(scrollPane, BorderLayout.CENTER);
        }

        private Object[][] getLeaderboardData(String matchId) {
            // Mock data - you can replace this with DB logic later
            Random random = new Random(matchId.hashCode());
            List<Object[]> rows = new ArrayList<>();

            for (int i = 1; i <= 5; i++) {
                rows.add(new Object[]{
                        i, // rank
                        "Player_" + i,
                        50 + random.nextInt(50) // random score
                });
            }

            // Sort descending by score
            rows.sort((a, b) -> ((Integer) b[2]) - ((Integer) a[2]));
            for (int i = 0; i < rows.size(); i++) {
                rows.get(i)[0] = i + 1; // update rank
            }

            return rows.toArray(new Object[0][]);
        }
    }

}
