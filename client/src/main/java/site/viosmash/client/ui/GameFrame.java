package site.viosmash.client.ui;


import site.viosmash.client.NetClient;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GameFrame extends JFrame {
    private final NetClient net;
    private long matchId;
    private int roundNo;
    private List<String> colors;
    private int showMs;
    private int countdownMs;
    private long serverEpoch;

    private final DefaultListModel<String> membersModel = new DefaultListModel<>();
    private final JLabel info = new JLabel(" ");
    private final JPanel colorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    private final JPanel selectPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    private final List<JButton> selectButtons = new ArrayList<>();
    private final JButton submitBtn = new JButton("Gửi");
    private final JLabel timerLabel = new JLabel(" ");

    private final java.util.List<String> selected = new ArrayList<>();
    private javax.swing.Timer hideTimer, countTimer;

    public GameFrame(NetClient net) {
        super("Đang chơi");
        this.net = net;
        setSize(800, 520); setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JList<String> members = new JList<>(membersModel);
        JPanel left = new JPanel(new BorderLayout());
        left.add(new JLabel("Người chơi"), BorderLayout.NORTH);
        left.add(new JScrollPane(members), BorderLayout.CENTER);

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

    public void setMembers(List<String> ms) {
        SwingUtilities.invokeLater(() -> {
            membersModel.clear();
            ms.forEach(membersModel::addElement);
        });
    }

    public void onRoundData(long matchId, int roundNo, String level, List<String> colors,
                            int showMs, int countdownMs, long serverEpoch) {
        this.matchId=matchId; this.roundNo=roundNo; this.colors=colors;
        this.showMs=showMs; this.countdownMs=countdownMs; this.serverEpoch=serverEpoch;
        this.selected.clear();
        SwingUtilities.invokeLater(() -> {
            info.setText("Vòng "+roundNo+" ["+level+"] — hiển thị "+(showMs/1000.0)+"s, trả lời "+(countdownMs/1000.0)+"s");
            colorPanel.removeAll();
            for (String c : colors) {
                JLabel l = new JLabel(c);
                l.setOpaque(true);
                l.setBackground(mapColor(c));
                l.setForeground(Color.BLACK);
                l.setBorder(BorderFactory.createEmptyBorder(8,12,8,12));
                colorPanel.add(l);
            }
            // sau showMs thì ẩn:
            if (hideTimer != null) hideTimer.stop();
            hideTimer = new javax.swing.Timer(showMs, ev -> {
                colorPanel.removeAll();
                colorPanel.add(new JLabel("ĐÃ ẨN — hãy chọn lại thứ tự!"));
                colorPanel.revalidate(); colorPanel.repaint();
            });
            hideTimer.setRepeats(false); hideTimer.start();

            selectPanel.removeAll();
            selectButtons.clear();
            for (String c : colors) {
                JButton b = new JButton(c);
                b.addActionListener(e -> {
                    if (selected.size() < colors.size()) {
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
                long left = countdownMs - elapsed;
                timerLabel.setText("Còn: " + Math.max(0, left/1000.0) + "s");
                if (left <= 0) countTimer.stop();
            });
            countTimer.start();

            revalidate(); repaint();
        });
    }

    private void submit() {
        try {
            java.util.Map<String, Object> payload = new java.util.HashMap<>();
            payload.put("matchId", matchId);
            payload.put("roundNo", roundNo);
            payload.put("selected", new ArrayList<>(selected));
            payload.put("clientEpochMs", System.currentTimeMillis());
            net.send("SUBMIT_ANSWER", payload);

            submitBtn.setEnabled(false);
        } catch (Exception ignored) {}
    }

    public void onRoundResult(Map<String,Object> payload) {
        SwingUtilities.invokeLater(() -> {
            Object ys = payload.get("yourScore");
            Object yt = payload.get("yourTimeMs");
            if (ys!=null || yt!=null) {
                JOptionPane.showMessageDialog(this,
                        "Điểm vòng: "+ys+" | Thời gian: "+yt+" ms");
                submitBtn.setEnabled(true);
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
