package site.viosmash.client.ui;


import site.viosmash.client.NetClient;
import site.viosmash.common.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GameFrame extends JFrame {
    private final NetClient net;
    private Round round;
    private long serverEpoch;

    private  final DefaultListModel<String> membersModel;
    private final JLabel info = new JLabel(" ");
    private final JPanel colorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    private final JPanel selectPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    private final List<JButton> selectButtons = new ArrayList<>();
    private final JButton submitBtn = new JButton("Gửi");
    private final JLabel timerLabel = new JLabel(" ");

    private final java.util.List<String> selected = new ArrayList<>();
    private javax.swing.Timer hideTimer, countTimer;

    private final DefaultTableModel tableModel;
    private final JTable table;
    private final List<User> players;

    public GameFrame(String username, List<User> players, NetClient net, DefaultListModel<String> membersModel) {
        super(username + " đang chơi");
        this.net = net;
        this.players = players;
        this.membersModel = membersModel;
        setSize(800, 520); setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel left = new JPanel(new BorderLayout());
        left.add(new JLabel("Người chơi"), BorderLayout.NORTH);

        String[] columns = {"Username", "Total Score", "Total Time (ms)"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // make table read-only
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(25);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));

        left.add(new JScrollPane(table), BorderLayout.CENTER);
        left.setPreferredSize(new Dimension(300, 0)); // fixed width for left side

        for(User player: players) {
            tableModel.addRow(new Object[]{player.getUsername(), 0, 0});
        }


//        JList<String> members = new JList<>(membersModel);
//        left.add(new JScrollPane(members), BorderLayout.CENTER);

        JPanel right = new JPanel(new BorderLayout(5,5));
        JPanel top = new JPanel(new BorderLayout());
        top.add(info, BorderLayout.CENTER);
        top.add(timerLabel, BorderLayout.EAST);

        JPanel center = new JPanel(new GridLayout(2,1));
        center.add(wrap("Dãy màu", colorPanel));
        center.add(wrap("Chọn lại", selectPanel));

        right.add(top, BorderLayout.NORTH);
        right.add(center, BorderLayout.CENTER);
        right.add(submitBtn, BorderLayout.SOUTH);

        add(left, BorderLayout.WEST);
        add(right, BorderLayout.CENTER);

        submitBtn.addActionListener(e -> submit());
    }

    private JPanel wrap(String title, JComponent comp) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(title));
        p.add(comp, BorderLayout.CENTER);
        return p;
    }

    public void setMembers(List<User> ms) {
        SwingUtilities.invokeLater(() -> {
            membersModel.clear();
            ms.forEach(r -> {
                membersModel.addElement(r.getUsername());
            });
        });
    }

    public void onRoundData(Round round,  long serverEpoch) {
        this.round = round;
         this.serverEpoch=serverEpoch;
        this.selected.clear();
        SwingUtilities.invokeLater(() -> {
            info.setText("Vòng "+round.getRoundNo()+" ["+round.getLevel()+"] — hiển thị "+(round.getShowMs()/1000.0)+"s, trả lời "+(round.getCountDownMs()/1000.0)+"s");
            colorPanel.removeAll();
            for (String c : round.getColors()) {
                JLabel l = new JLabel(c);
                l.setOpaque(true);
                l.setBackground(mapColor(c));
                l.setForeground(Color.BLACK);
                l.setBorder(BorderFactory.createEmptyBorder(8,12,8,12));
                colorPanel.add(l);
            }
            // sau showMs thì ẩn:
            if (hideTimer != null) hideTimer.stop();
            hideTimer = new javax.swing.Timer(round.getShowMs(), ev -> {
                colorPanel.removeAll();
                colorPanel.add(new JLabel("ĐÃ ẨN — hãy chọn lại thứ tự!"));
                colorPanel.revalidate(); colorPanel.repaint();
            });
            hideTimer.setRepeats(false); hideTimer.start();

            selectPanel.removeAll();
            selectButtons.clear();
            for (String c : round.getColors()) {
                JButton b = new JButton(c);
                b.addActionListener(e -> {
                    if (selected.size() < round.getColors().size()) {
                        selected.add(c);
                        b.setEnabled(false);
                    }
                });
                selectPanel.add(b); selectButtons.add(b);
            }
            // đếm ngược
            if (countTimer != null) countTimer.stop();
            long start = System.currentTimeMillis();
            countTimer = new javax.swing.Timer(100, ev -> {
                long elapsed = System.currentTimeMillis() - start;
                long left = round.getCountDownMs() - elapsed;
                timerLabel.setText("Còn: " + Math.max(0, left/1000.0) + "s");
                if (left <= 0) {
                    countTimer.stop();
                    submitBtn.addActionListener(e -> submit());
                }
            });
            countTimer.start();


            submitBtn.setEnabled(true);
            revalidate(); repaint();
        });
    }

    private void submit() {
        try {
            java.util.Map<String, String> payload = new java.util.HashMap<>();
            RoundResult roundResult = new RoundResult();
            roundResult.setRound(this.round);
            roundResult.setSelectedColors(new ArrayList<>(selected));
            payload.put("roundResult", Json.to(roundResult));
            payload.put("clientEpochMs", System.currentTimeMillis() + "");
            net.send("SUBMIT_ANSWER", payload);

            submitBtn.setEnabled(false);
        } catch (Exception ignored) {}
    }

    public void updateRank(List<MatchPlayer> leaderboard) {
        SwingUtilities.invokeLater(() -> {
            tableModel.setRowCount(0); // clear existing rows
            for (MatchPlayer r : leaderboard) {
                String username = r.getUser().getUsername();
                Object score = r.getTotalScore();
                Object time =r.getTotalTimeMs();
                tableModel.addRow(new Object[]{username, score, time});
            }
        });
    }


    public void onRoundResult(Map<String,Object> payload) {
        SwingUtilities.invokeLater(() -> {
            Object ys = payload.get("yourScore");
            Object yt = payload.get("yourTimeMs");
            if (ys!=null || yt!=null) {
                JOptionPane.showMessageDialog(this,
                        "Điểm vòng: "+ys+" | Thời gian: "+yt+" ms");
                submitBtn.setEnabled(true);
                System.out.println("fuck");
                // reset buttons
                for (JButton b : selectButtons) b.setEnabled(true);
            }
        });
    }

    private Color mapColor(String name) {
        switch (name) {
            case "RED":
                return Color.RED;
            case "GREEN":
                return Color.GREEN;
            case "BLUE":
                return Color.BLUE;
            case "YELLOW":
                return Color.YELLOW;
            case "CYAN":
                return Color.CYAN;
            case "MAGENTA":
                return Color.MAGENTA;
            case "ORANGE":
                return Color.ORANGE;
            case "PURPLE":
                return new Color(128, 0, 128);
            case "PINK":
                return Color.PINK;
            case "BROWN":
                return new Color(150, 75, 0);
            default:
                return Color.LIGHT_GRAY;
        }
    }

}
