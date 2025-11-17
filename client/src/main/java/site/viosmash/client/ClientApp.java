// client/src/main/java/com/cgo/client/ClientApp.java
package site.viosmash.client;


import com.fasterxml.jackson.core.type.TypeReference;
import site.viosmash.client.ui.*;
import site.viosmash.common.*;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ClientApp {
    private final NetClient net = new NetClient();
    private LoginFrame login;
    private LobbyFrame lobby;
    private GameFrame game;
    private HomeFrame homeFrame;
    private User user;
    private PlayedHistory playedHistory;
    private LeaderboardFrame leaderboardFrame;
    private RoundDetailFrame roundDetailFrame;
    public void start() throws Exception {
        SwingUtilities.invokeLater(() -> {
            try {
                net.connect("localhost", 6000, this::onMessage);
                login = new LoginFrame(net, v -> {});
                login.setVisible(true);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Không kết nối được server: " + e.getMessage());
            }
        });
    }

    private void onMessage(Message m) {
        switch (m.type) {
            case "ROUND_DETAIL_RESPONSE":
                List<RoundResult> results = Json.from(m.payload.get("roundDetail"), new TypeReference<List<RoundResult>>() {});
                roundDetailFrame = new RoundDetailFrame(
                        Integer.parseInt(m.payload.get("matchId")),
                        results
                );
                roundDetailFrame.setVisible(true);
                break;
            case "MATCH_DETAIL_RESPONSE":
                List<MatchPlayer> leaderboardObject = Json.from(m.payload.get("leaderboard"), new TypeReference<List<MatchPlayer>>() {});
                leaderboardFrame = new LeaderboardFrame(
                        Integer.parseInt(m.payload.get("matchId")),
                        leaderboardObject
                );
                leaderboardFrame.setVisible(true);
                break;
            case "PLAYED_HISTORY_RESPONSE":
                List<Match> object = Json.from(m.payload.get("matchsPlayed"), new TypeReference<List<Match>>() {
                });
                try {
                    playedHistory = new PlayedHistory(net, object);
                    playedHistory.setVisible(true);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;
            case "LOGIN_OK" :
                this.user = Json.from(m.payload.get("user"), new TypeReference<User>() {
                });
                this.user.setStatus((String) m.payload.get("status"));
                SwingUtilities.invokeLater(() -> {
                    login.setVisible(false);
                    lobby = new LobbyFrame(net, user);
                    homeFrame = new HomeFrame(net, user, lobby);
                    homeFrame.setVisible(true);
                });
                break;
            case "REGISTER_OK":
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(null, "Đăng ký thành công! Vui lòng đăng nhập.");
                    Window[] windows = Window.getWindows();
                    for (Window window : windows) {
                        if (window instanceof RegisterFrame) {
                            ((RegisterFrame) window).onRegisterSuccess();
                        }
                    }
                });
                break;
            case "LOGOUT_OK":
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(null, "Đăng xuất thành công!");
                    handleLogout();
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
                List<User> players = Json.from(m.payload.get("players"), new TypeReference<List<User>>() {
                });
                if (lobby != null) lobby.onOnlineList(players);
                break;
            }
            case "INVITE_INCOMING": {
                User from = Json.from(m.payload.get("fromUser"), new TypeReference<User>() {
                });
                if (lobby != null) lobby.onInviteIncoming(net, from);
                break;
            }
            case "ROOM_UPDATE": {
                if(m.payload.containsKey("userLeave")) {
                    User userLeave = Json.from(m.payload.get("userLeave"), new TypeReference<User>() {
                    });
                    if(userLeave.equals(this.user)) {
                        lobby.dispose();
                        homeFrame.setVisible(true);
                        return;
                    }
                }
                User owner = Json.from(m.payload.get("owner"), new TypeReference<User>() {
                });
                List<User> members = Json.from(m.payload.get("members"), new TypeReference<List<User>>() {
                });
                if (lobby != null) {
                    lobby.onRoomUpdate(owner, members);
                    if(!lobby.isVisible()) {
                        lobby.setVisible(true);
                        homeFrame.dispose();
                    }
                }
                if (game != null) game.setMembers(members);
                break;
            }
            case "MATCH_BEGIN": {
                SwingUtilities.invokeLater(() -> {
                    List<User> players = Json.from(m.payload.get("players"), new TypeReference<List<User>>() {
                    });
                    game = new GameFrame(user.getUsername(), players, net, lobby.getRoomModel());
                    game.setVisible(true);
                    lobby.setVisible(false);
                    lobby.onRoomUpdate(null, new ArrayList<>());
                });
                break;
            }
            case "ROUND_DATA": {
                Round round = Json.from(m.payload.get("round"), new TypeReference<Round>() {
                });
                long serverEpochMs = Long.parseLong(m.payload.get("serverEpochMs"));
                if (game != null) game.onRoundData(round, serverEpochMs);
                break;
            }
            case "UPDATE_TABLE_SCORE": {
                List<MatchPlayer> leaderboard = Json.from(m.payload.get("leaderboard"), new TypeReference<List<MatchPlayer>>() {
                });
                if(game != null) {
                    game.updateRank(leaderboard);
                }
                break;
            }
            case "MATCH_END": {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(null, "Kết thúc trận! Xem bảng xếp hạng ở server log/DB.");
                    if (game != null) game.dispose();
                    homeFrame.setVisible(true);
                });
                break;
            }
            default: {
                System.out.println("Unknown message type: " + m.type);
                break;
            }
        }
    }

    private void handleLogout() {
        if (homeFrame != null) homeFrame.dispose();
        if (lobby != null) lobby.dispose();
        if (game != null) game.dispose();
        if (playedHistory != null) playedHistory.dispose();
        if (leaderboardFrame != null) leaderboardFrame.dispose();
        if (roundDetailFrame != null) roundDetailFrame.dispose();

        login = new LoginFrame(net, v -> {});
        login.setVisible(true);

        this.user = null;
    }

    public static void main(String[] args) throws Exception {
        new ClientApp().start();
    }
}