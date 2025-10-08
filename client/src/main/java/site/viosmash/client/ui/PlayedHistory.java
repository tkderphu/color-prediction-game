package site.viosmash.client.ui;

import site.viosmash.client.NetClient;
import site.viosmash.client.utils.User;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class PlayedHistory extends JFrame {
    private NetClient client;
    private User user;
    public PlayedHistory(NetClient netClient, User user) throws IOException {
        setTitle("Match Table Example");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 300);
        setLocationRelativeTo(null);

        // Table data
        Object[][] data = {
                {"M001"},
                {"M002"},
                {"M003"},
                {"M004"}
        };
        String[] columnNames = {"match_id", "action"};

        Map<String, Object> ob = new HashMap<>();
        ob.put("username", user.getUsername());
        netClient.send(
                "PLAYED_HISTORY",
                ob
        );

        // Create table model
        DefaultTableModel model = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 1; // Only "action" column is editable
            }
        };

        JTable table = new JTable(model);
        table.setRowHeight(30);

        // Add button renderer and editor for the "action" column
        table.getColumn("action").setCellRenderer(new ButtonRenderer());
        table.getColumn("action").setCellEditor(new ButtonEditor(new JCheckBox()));

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


    public void updateHistoryTable() {

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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PlayedHistory().setVisible(true));
    }
}
