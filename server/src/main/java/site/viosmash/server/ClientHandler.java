// server/src/main/java/site/viosmash/server/ClientHandler.java
package site.viosmash.server;

import site.viosmash.common.Json;
import site.viosmash.common.Message;

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
    private String username = null;
    private BufferedReader in;
    private BufferedWriter out;

    public ClientHandler(Socket socket, Lobby lobby, ServerCore core) {
        this.socket = socket;
        this.lobby = lobby;
        this.core = core;
    }

    public String getUsername() {
        return username;
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
            // e.printStackTrace();
        } finally {
            logoutCleanup();
            try {
                socket.close();
            } catch (IOException ignored) {}
        }
    }

    private void handle(Message m) throws Exception {
        switch (m.type) {
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

    private void handleMatchDetail(Message m) {
        long matcId = (long) m.payload.get("matchId");

        try {
            List<Map<String, Object>> maps = core.matchDao.finalRanking(matcId);
            Map<String, Object> map = new HashMap<>();
            map.put("leaderboard", maps);
            map.put("matchId", matcId);
            send("MATCH_DETAIL_RESPONSE", map);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void handlePlayedHistory(Message m) throws IOException, SQLException {
        //lay danh sach lich su
        String username = (String) m.payload.get("username");
        List<Map<String, Object>> listMatchPlayed = core.matchDao.getListMatchPlayed(username);
        Map<String, Object> map = new HashMap<>();
        map.put("matchsPlayed", listMatchPlayed);
        send(
                "PLAYED_HISTORY_RESPONSE",
                map
        );

    }

    private void handleLogin(Message m) throws Exception {
        if (username != null) {
            sendError("ALREADY_LOGGED", "Already logged in");
            return;
        }
        String u = (String) m.payload.get("username");
        String p = (String) m.payload.get("password");
        if (!core.userDao.verifyLogin(u, p)) {
            sendError("BAD_CREDENTIALS", "Invalid username/password");
            return;
        }
        username = u;
        lobby.online.put(username, this);
        lobby.status.put(username, "IDLE");

        Map<String, Object> payload = new HashMap<>();
        payload.put("username", username);
        send("LOGIN_OK", payload);

        core.broadcastOnlineList();
    }

    private void handleInvite(Message m) throws Exception {
        requireLogin();
        String from = (String)m.payload.get("fromUsername");
        String to = (String) m.payload.get("toUsername");
        ClientHandler target = lobby.online.get(to);
        if (target == null) {
            sendError("USER_OFFLINE", "Target offline");
            return;
        }
        Room room = null;
        for(Room r : lobby.rooms.values()) {
            if(r.members.contains(from)) {
                room = r;
                break;
            }
        }
        // tạo phòng theo owner: là người mời
        if(room == null) {
            room = lobby.getOrCreateRoom(username);
            room.members.add(username);
            sendRoomUpdate(room, null);
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("fromUsername", from);
        target.send("INVITE_INCOMING", payload);
    }

    private void handleInviteResponse(Message m) throws Exception {
        requireLogin();
        String from = (String) m.payload.get("fromUsername");
        String invitedUsername = (String) m.payload.get("invitedUsername");
        boolean accepted = (Boolean) m.payload.get("accepted");
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
            Map<String, Object> payload = new HashMap<>();
            payload.put("msg", "You declined invite");
            send("INFO", payload);
            return;
        }

        //leave previous room
        Room theRoom = null;
        for (Room r : lobby.rooms.values()) {
            if (r.members.contains(invitedUsername)) {
                theRoom = r;
                break;
            }
        }
//        theRoom.members.remove(invitedUsername);
//        sendRoomUpdate(theRoom, invitedUsername);
//        lobby.dissolveIfEmpty(theRoom.owner);

        room.members.add(username);
        sendRoomUpdate(room, null);
    }

    private void handleLeaveRoom(Message m) throws Exception {
        requireLogin();
        Room theRoom = null;
        for (Room r : lobby.rooms.values()) {
            if (r.members.contains(username)) {
                theRoom = r;
                break;
            }
        }
        if (theRoom == null) return;



        theRoom.members.remove(username);
        sendRoomUpdate(theRoom, username);
        lobby.dissolveIfEmpty(theRoom.owner);
    }

    private void handleStartGame(Message m) throws Exception {
        requireLogin();
        Room room = lobby.rooms.get(username);
        if (room == null) {
            sendError("NOT_OWNER", "Only owner can start");
            return;
        }
        core.startMatch(room);
    }

    private void handleSubmitAnswer(Message m) throws Exception {
        requireLogin();
        core.handleSubmit(username, m);
    }

    private void logoutCleanup() {
        if (username != null) {
            lobby.online.remove(username);
            lobby.status.remove(username);
            for (Room r : lobby.rooms.values()) {
                r.members.remove(username);
            }
            lobby.rooms.entrySet().removeIf(e -> e.getValue().members.isEmpty());
            try {
                core.broadcastOnlineList();
            } catch (Exception ignored) {}
        }
    }

    public synchronized void send(String type, Map<String, Object> payload) throws IOException {
        Message m = new Message();
        m.type = type;
        m.payload = payload;
        String s = Json.to(m);
        out.write(s);
        out.write("\n");
        out.flush();
    }

    public void sendError(String code, String msg) throws IOException {
        Map<String, Object> payload = new HashMap<>();
        payload.put("code", code);
        payload.put("msg", msg);
        send("ERROR", payload);
    }

    private void requireLogin() throws Exception {
        if (username == null) throw new Exception("NOT_LOGGED_IN");
    }

    private void sendRoomUpdate(Room room, String usernameLeave) throws IOException {
        Map<String, Object> payload = new HashMap<>();
        payload.put("owner", room.owner);
        payload.put("members", new ArrayList<>(room.members));
        for (String u : room.members) {
            ClientHandler h = lobby.online.get(u);
            if (h != null) h.send("ROOM_UPDATE", payload);
        }
        if(username != null) {
            ClientHandler h = lobby.online.get(username);
            if (h != null) h.send("ROOM_UPDATE", payload);
        }
    }
}
