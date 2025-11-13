
package site.viosmash.client.ui;

import site.viosmash.common.RoundResult;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Nguyen Quang Phu
 * @since 12/10/2025
 */
public class RoundDetailFrame extends JFrame {

    public RoundDetailFrame(long matchId, List<RoundResult> roundDetails) {
        setTitle("Lịch sử từng vòng đấu của trận đấu với mã: " + matchId);
        setSize(700, 450);
        setLocationRelativeTo(null);

        // Table headers
        String[] columnNames = {"Vòng", "Độ khó", "Các màu của vòng đấu", "Các màu đã gửi", "Số điểm", "Thời gian"};

        Object[][] roundDetailData = buildRoundDetailData(roundDetails);

        // Create table
        JTable leaderboardTable = new JTable(roundDetailData, columnNames);
        leaderboardTable.setRowHeight(25);
        leaderboardTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        leaderboardTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        leaderboardTable.setEnabled(false); // read-only

        add(new JScrollPane(leaderboardTable), BorderLayout.CENTER);
    }

    private Object[][] buildRoundDetailData(List<RoundResult> roundDetails) {
        if (roundDetails == null || roundDetails.isEmpty()) {
            return new Object[0][0];
        }

        Object[][] rows = new Object[roundDetails.size()][6];
        for (int i = 0; i < roundDetails.size(); i++) {
            RoundResult roundResult = roundDetails.get(i);
            if(roundResult.getSelectedColors() == null) {
                roundResult.setSelectedColors(new ArrayList<>());
            }
            rows[i][0] = roundResult.getRound().getRoundNo();
            rows[i][1] = roundResult.getRound().getLevel();
            rows[i][2] = roundResult.getRound().getColors().stream().collect(Collectors.joining(", "));
            rows[i][3] = roundResult.getSelectedColors().stream().collect(Collectors.joining(", "));
            rows[i][4] = roundResult.getScore();
            rows[i][5] = (roundResult.getTimeMs() * 1.0)/1000;
        }
        return rows;
    }
}
