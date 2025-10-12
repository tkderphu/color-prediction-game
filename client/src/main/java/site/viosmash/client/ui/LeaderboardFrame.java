package site.viosmash.client.ui;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * @author Nguyen Quang Phu
 * @since 12/10/2025
 */
public class LeaderboardFrame extends JFrame {

    public LeaderboardFrame(long matchId, List<Map<String, Object>> leaderboard) {
        setTitle("Leaderboard - Match " + matchId);
        setSize(500, 350);
        setLocationRelativeTo(null);

        // Table headers
        String[] columnNames = {"Rank", "Username", "Total Score", "Total Time (ms)"};

        // Convert leaderboard data to table rows
        Object[][] leaderboardData = buildLeaderboardData(leaderboard);

        // Create table
        JTable leaderboardTable = new JTable(leaderboardData, columnNames);
        leaderboardTable.setRowHeight(25);
        leaderboardTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        leaderboardTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        leaderboardTable.setEnabled(false); // read-only

        add(new JScrollPane(leaderboardTable), BorderLayout.CENTER);
    }

    /** Convert leaderboard list to Object[][] for JTable */
    private Object[][] buildLeaderboardData(List<Map<String, Object>> leaderboard) {
        if (leaderboard == null || leaderboard.isEmpty()) {
            return new Object[0][0];
        }

        Object[][] rows = new Object[leaderboard.size()][4];
        for (int i = 0; i < leaderboard.size(); i++) {
            Map<String, Object> entry = leaderboard.get(i);
            rows[i][0] = i + 1; // rank (already sorted)
            rows[i][1] = entry.getOrDefault("username", "Unknown");
            rows[i][2] = entry.getOrDefault("total_score", 0);
            rows[i][3] = entry.getOrDefault("total_time_ms", 0);
        }
        return rows;
    }

    // --- Example usage for testing ---
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            List<Map<String, Object>> mockData = java.util.Arrays.asList(
                    createRow("Alice", 120, 3500),
                    createRow("Bob", 100, 4200),
                    createRow("Charlie", 90, 3900)
            );

            new LeaderboardFrame(101, mockData).setVisible(true);
        });
    }

    private static Map<String, Object> createRow(String username, int score, int timeMs) {
        java.util.Map<String, Object> map = new java.util.HashMap<>();
        map.put("username", username);
        map.put("total_score", score);
        map.put("total_time_ms", timeMs);
        return map;
    }
}
