package site.viosmash.client.ui;

import site.viosmash.common.MatchPlayer;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * @author Nguyen Quang Phu
 * @since 12/10/2025
 */
public class LeaderboardFrame extends JFrame {

    public LeaderboardFrame(long matchId, List<MatchPlayer> leaderboard) {
        setTitle("Leaderboard - Match " + matchId);
        setSize(500, 350);
        setLocationRelativeTo(null);

        // Table headers
        String[] columnNames = {"Rank", "Username", "Total Score", "Total Time (s)"};

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
    private Object[][] buildLeaderboardData(List<MatchPlayer> leaderboard) {
        if (leaderboard == null || leaderboard.isEmpty()) {
            return new Object[0][0];
        }

        Object[][] rows = new Object[leaderboard.size()][4];
        for (int i = 0; i < leaderboard.size(); i++) {
            MatchPlayer player = leaderboard.get(i);
            rows[i][0] = i + 1; // rank (already sorted)
            rows[i][1] = player.getUser().getUsername();
            rows[i][2] = player.getTotalScore();
            rows[i][3] = (player.getTotalTimeMs() * 1.0)/1000;
        }
        return rows;
    }
}
