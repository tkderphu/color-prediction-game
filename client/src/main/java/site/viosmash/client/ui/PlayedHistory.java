package site.viosmash.client.ui;

import site.viosmash.client.NetClient;
import site.viosmash.common.Match;
import site.viosmash.common.User;

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

    public PlayedHistory(NetClient netClient, List<Match> histories) throws IOException {
        this.client = netClient;
        setTitle("Played Match History");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(700, 400);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // --- Table columns ---
        String[] columnNames = {"Mã", "Chủ phòng", "Thời gian bắt đầu", "Thời gian kết thúc", "Bảng xếp hạng", "Chi tiết vòng đấu"};

        // --- Table model ---
        model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Only the "Action" column is editable (for button)
                return column == 4 || column == 5;
            }
        };

        // --- Table setup ---
        table = new JTable(model);
        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));

        // --- Populate data ---
        if (histories != null) {
            for (Match history : histories) {
                model.addRow(new Object[]{
                        history.getId(),
                        history.getRoomOwner().getUsername(),
                        history.getStartedAt(),
                        history.getEndedAt(),
                        "Xem",
                        "Xem"
                });
            }
        }

        // --- Button Renderer & Editor ---
        table.getColumn("Bảng xếp hạng").setCellRenderer(new ButtonRenderer());
        table.getColumn("Bảng xếp hạng").setCellEditor(new ButtonEditor(new JCheckBox(), netClient, "leaderboard"));


        // --- Button Renderer & Editor ---
        table.getColumn("Chi tiết vòng đấu").setCellRenderer(new ButtonRenderer());
        table.getColumn("Chi tiết vòng đấu").setCellEditor(new ButtonEditor(new JCheckBox(), netClient, "round_detail"));


        // --- Scroll pane ---
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);
    }


    // --- Button Renderer ---
    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText(value == null ? "Chi tiết" : value.toString());
            return this;
        }
    }

    // --- Button Editor ---
    class ButtonEditor extends DefaultCellEditor {
        private final JButton button;
        private int matchId;
        private boolean clicked;
        private NetClient netClient;
        private String type;
        public ButtonEditor(JCheckBox checkBox, NetClient netClient, String type) {
            super(checkBox);
            button = new JButton("Chi tiết");
            button.setOpaque(true);
            this.netClient = netClient;
            this.type = type;
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
//                new LeaderboardFrame(matchId).setVisible(true);
            }
            clicked = false;
            return "Chi tiết";
        }

        @Override
        public boolean stopCellEditing() {
            clicked = false;
            return super.stopCellEditing();
        }
    }

}
