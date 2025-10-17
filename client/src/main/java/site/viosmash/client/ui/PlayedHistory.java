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
    private DefaultTableModel model;
    private JTable table;

    public PlayedHistory(NetClient netClient, List<Map<String, Object>> histories) throws IOException {
        this.client = netClient;
        setTitle("Played Match History");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(700, 400);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // --- Table columns ---
        String[] columnNames = {"Match ID", "Room Owner", "Started At", "Action"};

        // --- Table model ---
        model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Only the "Action" column is editable (for button)
                return column == 3;
            }
        };

        // --- Table setup ---
        table = new JTable(model);
        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));

        // --- Populate data ---
        if (histories != null) {
            for (Map<String, Object> history : histories) {
                model.addRow(new Object[]{
                        history.getOrDefault("id", "N/A"),
                        history.getOrDefault("room_owner", "Unknown"),
                        history.getOrDefault("started_at", "Unknown"),
                        "View Detail"
                });
            }
        }

        // --- Button Renderer & Editor ---
        table.getColumn("Action").setCellRenderer(new ButtonRenderer());
        table.getColumn("Action").setCellEditor(new ButtonEditor(new JCheckBox(), netClient));

        // --- Scroll pane ---
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);
    }

    /** Re-render table if histories updated dynamically */
    public void updateHistoryTable(List<Map<String, Object>> newHistories) {
        model.setRowCount(0); // Clear table
        for (Map<String, Object> history : newHistories) {
            model.addRow(new Object[]{
                    history.getOrDefault("id", "N/A"),
                    history.getOrDefault("room_owner", "Unknown"),
                    history.getOrDefault("started_at", "Unknown"),
                    "View Detail"
            });
        }
    }

    // --- Button Renderer ---
    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText(value == null ? "View Detail" : value.toString());
            return this;
        }
    }

    // --- Button Editor ---
    class ButtonEditor extends DefaultCellEditor {
        private final JButton button;
        private int matchId;
        private boolean clicked;
        private NetClient netClient;
        public ButtonEditor(JCheckBox checkBox, NetClient netClient) {
            super(checkBox);
            button = new JButton("View Detail");
            button.setOpaque(true);
            this.netClient = netClient;
            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(
                JTable table, Object value, boolean isSelected, int row, int column) {
            matchId = (int) table.getValueAt(row, 0);
            clicked = true;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (clicked) {
                Map<String, Object> map = new HashMap<>();
                map.put("matchId", matchId);
                try {
                    netClient.send("MATCH_DETAIL", map);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
//                new LeaderboardFrame(matchId).setVisible(true);
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

    // --- Separate Leaderboard Window ---

    // --- For quick testing ---
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                List<Map<String, Object>> mockHistories = new ArrayList<>();

                Map<String, Object> m1 = new HashMap<>();
                m1.put("id", 1);
                m1.put("room_owner", "Alice");
                m1.put("started_at", "2025-10-10 15:30");
                mockHistories.add(m1);

                Map<String, Object> m2 = new HashMap<>();
                m2.put("id", 2);
                m2.put("room_owner", "Bob");
                m2.put("started_at", "2025-10-11 12:00");
                mockHistories.add(m2);

                Map<String, Object> m3 = new HashMap<>();
                m3.put("id", 3);
                m3.put("room_owner", "Charlie");
                m3.put("started_at", "2025-10-12 09:15");
                mockHistories.add(m3);

                new PlayedHistory(null, mockHistories).setVisible(true);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

}
