// client/src/main/java/com/cgo/client/ui/LobbyFrame.java
package site.viosmash.client.ui;


import site.viosmash.client.NetClient;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class LobbyFrame extends JFrame {
    private final NetClient net;
    private final DefaultListModel<String> onlineModel = new DefaultListModel<>();
    private final DefaultListModel<String> roomModel = new DefaultListModel<>();
    private final JButton inviteBtn = new JButton("Mời chơi");
    private final JButton startBtn  = new JButton("Bắt đầu (chủ phòng)");
    private final JButton leaveBtn  = new JButton("Thoát phòng");
    private String myName;

    public LobbyFrame(NetClient net, String myName) {
        super("Phòng chờ - " + myName);
        this.net = net; this.myName = myName;
        setSize(640, 400); setLocationRelativeTo(null); setDefaultCloseOperation(EXIT_ON_CLOSE);

        JList<String> online = new JList<>(onlineModel);
        JList<String> room   = new JList<>(roomModel);

        JPanel left = new JPanel(new BorderLayout());
        left.add(new JLabel("Người chơi trực tuyến"), BorderLayout.NORTH);
        left.add(new JScrollPane(online), BorderLayout.CENTER);
        left.add(inviteBtn, BorderLayout.SOUTH);

        JPanel right = new JPanel(new BorderLayout());
        right.add(new JLabel("Thành viên phòng"), BorderLayout.NORTH);
        right.add(new JScrollPane(room), BorderLayout.CENTER);

        JPanel bottom = new JPanel();
        bottom.add(startBtn); bottom.add(leaveBtn);

        add(left, BorderLayout.WEST);
        add(right, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);


        inviteBtn.addActionListener(e -> {
            String target = online.getSelectedValue();
            if (target == null) return;
            try {
                java.util.Map<String, Object> payload = new java.util.HashMap<>();
                String username = target.split("\\s+")[0];
                payload.put("fromUsername", myName);
                payload.put("toUsername", username);
                net.send("INVITE", payload);
            } catch (Exception ignored) {}
        });

        startBtn.addActionListener(e -> {
            try {
                java.util.Map<String, Object> payload = new java.util.HashMap<>();
                net.send("START_GAME", payload);
            } catch (Exception ignored) {}
        });
        leaveBtn.addActionListener(e -> {
            try {
                java.util.Map<String, Object> payload = new java.util.HashMap<>();
                payload.put("username", myName);
                net.send("LEAVE_ROOM", payload);
            } catch (Exception ignored) {}
        });
    }

    public void onOnlineList(List<Map<String,Object>> players) {
        SwingUtilities.invokeLater(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            onlineModel.clear();
            for (Map<String, Object> p : players) {
                String u = (String)p.get("username");
                String st = (String)p.get("status");
                if (!u.equals(myName)) onlineModel.addElement(u + " ("+st+")");
            }
        });
    }

    public DefaultListModel<String> getRoomModel() {
        return roomModel;
    }

    public void onRoomUpdate(String owner, List<String> members) {
        SwingUtilities.invokeLater(() -> {
            roomModel.clear();
            for (String m : members) {
                if(owner.equals(m)) {
                    roomModel.addElement(m + " - " + "owner");
                } else {
                    roomModel.addElement(m);
                }
            };
        });
    }

    public void onInviteIncoming(NetClient net, String from) {
        SwingUtilities.invokeLater(() -> {
            int res = JOptionPane.showConfirmDialog(this,
                    "Bạn có nhận lời mời từ "+from+" không?", "Mời chơi",
                    JOptionPane.YES_NO_OPTION);
            boolean accepted = (res == JOptionPane.YES_OPTION);
            try {
                java.util.Map<String, Object> payload = new java.util.HashMap<>();
                payload.put("fromUsername", from);
                payload.put("invitedUsername", myName);
                payload.put("accepted", accepted);
                net.send("INVITE_RESPONSE", payload);
            } catch (Exception ignored) {}
        });
    }
}
