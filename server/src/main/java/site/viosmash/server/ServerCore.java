// server/src/main/java/site/viosmash/server/ServerCore.java
package site.viosmash.server;

import com.fasterxml.jackson.core.type.TypeReference;
import site.viosmash.common.Json;
import site.viosmash.common.Message;
import site.viosmash.server.dao.MatchDao;
import site.viosmash.server.dao.UserDao;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ServerCore {
    public final Lobby lobby = new Lobby();
    public final UserDao userDao = new UserDao();
    public final MatchDao matchDao = new MatchDao();
//    public final MatchHistoryDao matchHistoryDao = new MatchHistoryDao();
    // Trạng thái trận đang diễn ra: matchId -> context
    private final Map<Long, MatchContext> matches = new ConcurrentHashMap<>();

    public void startTcp(int port) throws Exception {
        try (ServerSocket ss = new ServerSocket(port)) {
            System.out.println("Server listening on " + port);
            while (true) {
                Socket s = ss.accept();
                ClientHandler h = new ClientHandler(s, lobby, this);
                new Thread(h, "Client-" + s.getRemoteSocketAddress()).start();
            }
        }
    }

    public void broadcastOnlineList() throws IOException {
        List<Map<String, Object>> players = new ArrayList<>();
        for (Map.Entry<String, ClientHandler> e : lobby.online.entrySet()) {
            String u = e.getKey();
            String st = lobby.status.containsKey(u) ? lobby.status.get(u) : "IDLE";
            Map<String, Object> entry = new HashMap<>();
            entry.put("username", u);
            entry.put("status", st);
            players.add(entry);
        }
        Map<String, Object> payload = new HashMap<>();
        payload.put("players", players);
        for (ClientHandler h : lobby.online.values()) {
            h.send("ONLINE_LIST", payload);
        }
    }

    // Bắt đầu 1 trận 15 vòng
    public void startMatch(Room room) throws Exception {
        long matchId = matchDao.createMatch(room.owner);
        MatchContext ctx = new MatchContext(matchId, new ArrayList<String>(room.members));
        matches.put(matchId, ctx);
        for (String u : room.members) {
            matchDao.addPlayer(matchId, u);
            lobby.status.put(u, "PLAYING");
            ClientHandler h = lobby.online.get(u);
            if (h != null) {
                Map<String, Object> payload = new HashMap<>();
                payload.put("matchId", matchId);
                payload.put("rounds", 15);
                payload.put("players", ctx.players);
                h.send("MATCH_BEGIN", payload);
            }
        }
        broadcastOnlineList();
        lobby.rooms.remove(room.owner);

        // Chạy 15 vòng
        new Thread(new Runnable() {
            public void run() {
                runMatch(ctx);
            }
        }).start();
    }

    private void runMatch(MatchContext ctx) {
        try {
            for (int roundNo = 1; roundNo <= 15; roundNo++) {
                RoundSpec spec = RoundSpec.forRound(roundNo);
                List<String> colors = ColorGen.generate(spec.colorCount);
                String colorsJson = Json.mapper().writeValueAsString(colors);

                long roundId = matchDao.createRound(ctx.matchId, roundNo, spec.level,
                        colorsJson, spec.showMs, spec.countdownMs);

                long serverEpoch = System.currentTimeMillis();
                ctx.currentRound = new LiveRound(roundId, roundNo, spec, colors, serverEpoch);

                // Gửi ROUND_DATA cho tất cả
                Map<String, Object> payload = new HashMap<>();
                payload.put("matchId", ctx.matchId);
                payload.put("roundNo", roundNo);
                payload.put("level", spec.level);
                payload.put("colors", colors);
                payload.put("showMs", spec.showMs);
                payload.put("countdownMs", spec.countdownMs);
                payload.put("serverEpochMs", serverEpoch);

                for (String u : ctx.players) {
                    ClientHandler h = lobby.online.get(u);
                    if (h != null) h.send("ROUND_DATA", payload);
                }

                // Chờ hết thời lượng vòng (show + countdown) + đệm nhỏ 500ms
                Thread.sleep(spec.showMs + spec.countdownMs + 500);

                // Với những ai chưa gửi → tính 0 điểm, thời gian = countdownMs
                for (String u : ctx.players) {
                    if (!ctx.roundSubmittedUsers.contains(u)) {
                        double score = 0.0;
                        long timeMs = spec.countdownMs;
                        matchDao.saveRoundResult(ctx.currentRound.roundId, u, null, score, timeMs, null);
                        matchDao.updatePlayerTotals(ctx.matchId, u, score, timeMs);
                    }
                }

                // Gửi ROUND_RESULT tóm tắt
                List<Map<String, Object>> lb = ctx.buildLeaderboard(matchDao);
                for (String u : ctx.players) {
                    ClientHandler h = lobby.online.get(u);
                    if (h != null) {
                        Map<String, Object> rrPayload = new HashMap<>();
                        rrPayload.put("leaderboard", lb);
                        h.send("UPDATE_TABLE_SCORE", rrPayload);
                    }
                }

                // reset trạng thái tạm cho vòng tiếp
                ctx.roundSubmittedUsers.clear();
                ctx.currentRound = null;
            }

            // Kết thúc trận
            matchDao.endMatch(ctx.matchId);
            List<Map<String, Object>> finalRank = matchDao.finalRanking(ctx.matchId);
            for (String u : ctx.players) {
                ClientHandler h = lobby.online.get(u);
                if (h != null) {
                    Map<String, Object> endPayload = new HashMap<>();
                    endPayload.put("matchId", ctx.matchId);
                    endPayload.put("finalRanking", finalRank);
                    h.send("MATCH_END", endPayload);
                }
                lobby.status.put(u, "IDLE");
            }
            broadcastOnlineList();
            matches.remove(ctx.matchId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Người chơi nộp bài cho vòng hiện tại
    public void handleSubmit(String username, Message m) throws Exception {
        long matchId = ((Number) m.payload.get("matchId")).longValue();
        int roundNo = ((Number) m.payload.get("roundNo")).intValue();
        List<String> selected = Json.mapper().convertValue(
                m.payload.get("selected"), new TypeReference<List<String>>() {});
        long clientEpochMs = ((Number) m.payload.get("clientEpochMs")).longValue();

        MatchContext ctx = matches.get(matchId);
        if (ctx == null || ctx.currentRound == null || ctx.currentRound.roundNo != roundNo) return;

        long elapsed = Math.max(0, clientEpochMs - ctx.currentRound.serverEpochMs);
        long timeMs = Math.min(elapsed, ctx.currentRound.spec.countdownMs);

        double score = Score.calcScore(selected, ctx.currentRound.colors);

        String selJson = Json.mapper().writeValueAsString(selected);
        matchDao.saveRoundResult(ctx.currentRound.roundId, username, selJson, score, timeMs,
                new Timestamp(System.currentTimeMillis()));
        matchDao.updatePlayerTotals(matchId, username, score, timeMs);

        ctx.roundSubmittedUsers.add(username);

        ClientHandler h = lobby.online.get(username);
//        if (h != null) {
//            Map<String, Object> resultPayload = new HashMap<>();
//            resultPayload.put("roundNo", roundNo);
//            resultPayload.put("yourScore", score);
//            resultPayload.put("yourTimeMs", timeMs);
//            h.send("ROUND_RESULT", resultPayload);
//        }
    }

    public void handleHistory(ClientHandler h, Message m) throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("items", new ArrayList<Object>());
        h.send("HISTORY_RESPONSE", payload);
    }

    public void handleLeaderboard(ClientHandler h, Message m) throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("items", new ArrayList<Object>());
        h.send("LEADERBOARD_RESPONSE", payload);
    }

    // --- Helper classes ---
    public static class RoundSpec {
        public final String level;
        public final int colorCount;
        public final int showMs;
        public final int countdownMs;

        private RoundSpec(String level, int colorCount, int showMs) {
            this.level = level;
            this.colorCount = colorCount;
            this.showMs = showMs;
            this.countdownMs = showMs + 5000;
        }

        public static RoundSpec forRound(int r) {
            if (r <= 5) return new RoundSpec("EASY", 3, 3000);
            if (r <= 10) return new RoundSpec("MEDIUM", 5, 2000);
            return new RoundSpec("HARD", 6, 1000);
        }
    }

    public static class LiveRound {
        public final long roundId;
        public final int roundNo;
        public final RoundSpec spec;
        public final List<String> colors;
        public final long serverEpochMs;

        public LiveRound(long roundId, int roundNo, RoundSpec spec, List<String> colors, long serverEpochMs) {
            this.roundId = roundId;
            this.roundNo = roundNo;
            this.spec = spec;
            this.colors = colors;
            this.serverEpochMs = serverEpochMs;
        }
    }

    public static class MatchContext {
        public final long matchId;
        public final List<String> players;
        public volatile LiveRound currentRound;
        public final Set<String> roundSubmittedUsers = ConcurrentHashMap.newKeySet();

        public MatchContext(long matchId, List<String> players) {
            this.matchId = matchId;
            this.players = players;
        }

        public List<Map<String, Object>> buildLeaderboard(MatchDao dao) {
            try {
                return dao.finalRanking(matchId);
            } catch (Exception e) {
                return new ArrayList<Map<String, Object>>();
            }
        }
    }
}
