// client/src/main/java/com/cgo/client/ClientApp.java
package site.viosmash.client;



import site.viosmash.client.ui.*;
import site.viosmash.client.utils.User;
import site.viosmash.common.Message;

import javax.swing.*;
import java.util.List;
import java.util.Map;

public class ClientApp {
    private final NetClient net = new NetClient();
    private LoginFram2 login;
    private LobbyFrame2 lobby;
    private GameFrame game;
    private HomeFrame homeFrame;
    private User user;

    public void start() throws Exception {
        SwingUtilities.invokeLater(() -> {
            try {
                net.connect("127.0.0.1", 6000, this::onMessage);
                login = new LoginFram2(net, v -> {});
                login.setVisible(true);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Không kết nối được server: " + e.getMessage());
            }
        });
    }

    private void onMessage(Message m) {
        switch (m.type) {
            case "LOGIN_OK" :
                String u = (String)m.payload.get("username");
                String st = (String)m.payload.get("status");
                user = new User();
                user.setStatus(st);
                user.setUsername(u);
                SwingUtilities.invokeLater(() -> {
                    login.setVisible(false);
                    lobby = new LobbyFrame2(net, user);
                    homeFrame = new HomeFrame(net, user, lobby);
                    homeFrame.setVisible(true);
                });
                break;
            case "ERROR":
                SwingUtilities.invokeLater(() ->
                    JOptionPane.showMessageDialog(null, "Lỗi: "+m.payload.get("msg")));
                break;
            case "ONLINE_LIST": {
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                List<Map<String,Object>> players = (List<Map<String,Object>>) m.payload.get("players");
                if (lobby != null) lobby.onOnlineList(players);
                break;
            }
            case "INVITE_INCOMING": {
                String from = (String)m.payload.get("fromUsername");
                if (lobby != null) lobby.onInviteIncoming(net, from);
                break;
            }
            case "ROOM_UPDATE": {
                String owner = (String)m.payload.get("owner");
                List<String> members = (List<String>) m.payload.get("members");
                if (lobby != null) lobby.onRoomUpdate(owner, members);
                if (game != null) game.setMembers(members);
                break;
            }
            case "MATCH_BEGIN": {
                SwingUtilities.invokeLater(() -> {
                    game = new GameFrame(net);
                    game.setVisible(true);
                });
                break;
            }
            case "ROUND_DATA": {
                long matchId = ((Number)m.payload.get("matchId")).longValue();
                int roundNo = ((Number)m.payload.get("roundNo")).intValue();
                String level = (String)m.payload.get("level");
                List<String> colors = (List<String>) m.payload.get("colors");
                int showMs = ((Number)m.payload.get("showMs")).intValue();
                int countdownMs = ((Number)m.payload.get("countdownMs")).intValue();
                long serverEpochMs = ((Number)m.payload.get("serverEpochMs")).longValue();
                if (game != null) game.onRoundData(matchId, roundNo, level, colors, showMs, countdownMs, serverEpochMs);
                break;
            }
            case "ROUND_RESULT": {
                if (game != null) game.onRoundResult(m.payload);
                break;
            }
            case "MATCH_END": {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(null, "Kết thúc trận! Xem bảng xếp hạng ở server log/DB.");
                    if (game != null) game.dispose();
                });
                break;
            }
            default: {
                System.out.println("Unknown message type: " + m.type);
                break;
            }
        }
    }




    public static void main(String[] args) throws Exception {
        new ClientApp().start();
    }
}
