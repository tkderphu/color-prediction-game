// server/src/main/java/site/viosmash/server/ClientHandler.java
package site.viosmash.server;

import com.fasterxml.jackson.core.type.TypeReference;
import site.viosmash.common.*;

import java.io.*;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final Lobby lobby;
    private final ServerCore core;
    private volatile boolean running = true;
    private User user;
    private BufferedReader in;
    private BufferedWriter out;

    public ClientHandler(Socket socket, Lobby lobby, ServerCore core) {
        this.socket = socket;
        this.lobby = lobby;
        this.core = core;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
            String line;
            while (running && (line = in.readLine()) != null) {
                Message msg = Json.from(line, Message.class);
                handle(msg);
            }
        } catch (Exception e) {
             e.printStackTrace();
        } finally {
            logoutCleanup();
            try {
                socket.close();
            } catch (IOException ignored) {}
        }
    }

    private void handle(Message m) throws Exception {
        switch (m.type) {
            case "ROUND_DETAIL":
                handleGetRoundHistoryDetail(m);
                break;
            case "MATCH_DETAIL":
                handleMatchDetail(m);
                break;
            case "PLAYED_HISTORY":
                handlePlayedHistory(m);
                break;
            case "LOGIN":
                handleLogin(m);
                break;
            case "INVITE":
                handleInvite(m);
                break;
            case "INVITE_RESPONSE":
                handleInviteResponse(m);
                break;
            case "LEAVE_ROOM":
                handleLeaveRoom(m);
                break;
            case "START_GAME":
                handleStartGame(m);
                break;
            case "SUBMIT_ANSWER":
                handleSubmitAnswer(m);
                break;
            case "HISTORY_REQUEST":
                core.handleHistory(this, m);
                break;
            case "LEADERBOARD_REQUEST":
                core.handleLeaderboard(this, m);
                break;
            default:
                sendError("UNKNOWN_TYPE", "Unknown message type: " + m.type);
                break;
        }
    }

    private void handleGetRoundHistoryDetail(Message m) {
        int matchId  = Integer.parseInt(m.payload.get("matchId"));
        List<RoundResult> roundResults = core.roundResultDao.getListByMatchId(matchId, this.user.getId());

        try {
            Map<String, String> map = new HashMap<>();
            map.put("roundDetail", Json.to(roundResults));
            map.put("matchId", matchId + "");
            send("ROUND_DETAIL_RESPONSE", map);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleMatchDetail(Message m) {
        int matchId  = Integer.parseInt(m.payload.get("matchId"));

        try {
            List<MatchPlayer> matchPlayers = core.matchPlayerDao.finalRanking(matchId);

            Map<String, String> map = new HashMap<>();
            map.put("leaderboard", Json.to(matchPlayers));
            map.put("matchId", matchId + "");
            send("MATCH_DETAIL_RESPONSE", map);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void handlePlayedHistory(Message m) throws IOException, SQLException {
        //lay danh sach lich su
        String username = (String) m.payload.get("username");
        List<Match> listMatchPlayed = core.matchDao.getListMatchPlayed(this.user.getId());
        Map<String, String> map = new HashMap<>();
        map.put("matchsPlayed", Json.to(listMatchPlayed));
        send(
                "PLAYED_HISTORY_RESPONSE",
                map
        );

    }

    private void handleLogin(Message m) throws Exception {
        if (user != null) {
            sendError("ALREADY_LOGGED", "Already logged in");
            return;
        }
        String u = (String) m.payload.get("username");
        String p = (String) m.payload.get("password");
        User user = new User();
        user.setUsername(u);
        user.setPassword(p);

        this.user = core.userDao.verifyLogin(user);
        if (this.user == null) {
            sendError("BAD_CREDENTIALS", "Invalid username/password");
            return;
        }
        lobby.online.put(this.user, this);
        lobby.status.put(this.user, "IDLE");

        Map<String, String> payload = new HashMap<>();

        payload.put("status", "IDLE");
        payload.put("user", Json.to(this.user));
        send("LOGIN_OK", payload);

        core.broadcastOnlineList();
    }

    private void handleInvite(Message m) throws Exception {
        requireLogin();
        String toUsername =  m.payload.get("toUsername");
        User to = lobby.online.entrySet().stream().filter(entry -> {
            return entry.getKey().getUsername().equals(toUsername);
        }).findFirst().get().getKey();
        ClientHandler target = lobby.online.get(to);
        if (target == null) {
            sendError("USER_OFFLINE", "Target offline");
            return;
        }
        Room room = null;
        for(Room r : lobby.rooms.values()) {
            if(r.members.contains(this.user)) {
                room = r;
                break;
            }
        }
        // tạo phòng theo owner: là người mời
        if(room == null) {
            room = lobby.getOrCreateRoom(this.user);
            room.members.add(this.user);
            sendRoomUpdate(room, null);
        }

        Map<String, String> payload = new HashMap<>();
        payload.put("fromUser", Json.to(this.user));
        target.send("INVITE_INCOMING", payload);
    }

    private void handleInviteResponse(Message m) throws Exception {
        requireLogin();
        User from = Json.from(m.payload.get("fromUser"), new TypeReference<User>() {
        });
        User invitedUser = Json.from(m.payload.get("invitedUser"), new TypeReference<User>() {
        });
        boolean accepted = Boolean.parseBoolean( m.payload.get("accepted"));
        Room room = null;
        for(Room r : lobby.rooms.values()) {
            if(r.members.contains(from)) {
                room = r;
                break;
            }
        }
        if (room == null) {
            sendError("ROOM_MISSING", "Room not found");
            return;
        }
        if (!accepted) {
            Map<String, String> payload = new HashMap<>();
            payload.put("msg", "You declined invite");
            send("INFO", payload);
            return;
        }

        //leave previous room
        Room theRoom = null;
        for (Room r : lobby.rooms.values()) {
            if (r.members.contains(invitedUser)) {
                theRoom = r;
                break;
            }
        }
        if(theRoom != null) {
            theRoom.members.remove(invitedUser);
            sendRoomUpdate(theRoom, null);
        }
        room.members.add(invitedUser);
        sendRoomUpdate(room, null);
    }

    private void handleLeaveRoom(Message m) throws Exception {
        requireLogin();
        Room theRoom = null;
        for (Room r : lobby.rooms.values()) {
            if (r.members.contains(this.user)) {
                theRoom = r;
                break;
            }
        }
        if (theRoom == null) {
            Map<String, String> payload = new HashMap<>();
            payload.put("userLeave", Json.to(this.user));
            ClientHandler h = lobby.online.get(this.user);
            if (h != null) {
                h.send("ROOM_UPDATE", payload);
            }
            return;
        };



        theRoom.members.remove(this.user);
        if(this.user.equals(theRoom.owner)) {
            if(!theRoom.members.isEmpty()) {
                Room room = lobby.rooms.get(this.user);
                room.owner = room.members.stream().findFirst().get();

                lobby.rooms.remove(this.user);
                lobby.rooms.put(room.owner, room);
            }
        }
        lobby.dissolveIfEmpty(theRoom.owner);
        sendRoomUpdate(theRoom, this.user);
    }

    private void handleStartGame(Message m) throws Exception {
        requireLogin();
        Room room = lobby.rooms.get(this.user);
        if (room == null) {
            sendError("NOT_OWNER", "Only owner can start");
            return;
        }
        core.startMatch(room);
    }

    private void handleSubmitAnswer(Message m) throws Exception {
        requireLogin();
        core.handleSubmit(this.user, m);
    }

    private void logoutCleanup() {
        if (this.user != null) {
            lobby.online.remove(this.user);
            lobby.status.remove(this.user);
            for (Room r : lobby.rooms.values()) {
                r.members.remove(this.user);
            }
            lobby.rooms.entrySet().removeIf(e -> e.getValue().members.isEmpty());
            try {
                core.broadcastOnlineList();
            } catch (Exception ignored) {}
        }
    }

    public synchronized void send(String type, Map<String, String> payload) throws IOException {
        Message m = new Message();
        m.type = type;
        m.payload = payload;
        String s = Json.to(m);
        out.write(s);
        out.write("\n");
        out.flush();
    }

    public void sendError(String code, String msg) throws IOException {
        Map<String, String> payload = new HashMap<>();
        payload.put("code", code);
        payload.put("msg", msg);
        send("ERROR", payload);
    }

    private void requireLogin() throws Exception {
        if (this.user == null) throw new Exception("NOT_LOGGED_IN");
    }

    private void sendRoomUpdate(Room room, User userLeave) throws IOException {
        Map<String, String> payload = new HashMap<>();
        payload.put("owner", Json.to(room.owner));
        payload.put("members", Json.to(new ArrayList<>(room.members)));
        for (User u : room.members) {
            ClientHandler h = lobby.online.get(u);
            if (h != null) {
                h.send("ROOM_UPDATE", payload);
            }
        }
        if(userLeave != null) {
            payload.put("userLeave", Json.to(userLeave));
            ClientHandler h = lobby.online.get(userLeave);
            if (h != null) {
                h.send("ROOM_UPDATE", payload);
            }
        }
    }
}
