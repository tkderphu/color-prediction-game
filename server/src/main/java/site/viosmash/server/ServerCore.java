// server/src/main/java/site/viosmash/server/ServerCore.java
package site.viosmash.server;

import com.fasterxml.jackson.core.type.TypeReference;
import site.viosmash.common.*;
import site.viosmash.server.dao.*;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ServerCore {
    public final Lobby lobby = new Lobby();
    public final UserDao userDao = new UserDao();
    public final MatchDao matchDao = new MatchDao();
    public final MatchPlayerDao matchPlayerDao = new MatchPlayerDao();
    public final RoundResultDao roundResultDao = new RoundResultDao();
    private final RoundDao roundDao = new RoundDao();
//    public final MatchHistoryDao matchHistoryDao = new MatchHistoryDao();
    // Trạng thái trận đang diễn ra: matchId -> context
    private final Map<Integer, MatchContext> matches = new ConcurrentHashMap<>();

    public void startTcp(int port) throws Exception {
        InetAddress inetAddress = InetAddress.getByName("localhost");
        try (ServerSocket ss = new ServerSocket(port, 0, inetAddress)) {
            System.out.println("Server listening on " + port);
            while (true) {
                Socket s = ss.accept();
                ClientHandler h = new ClientHandler(s, lobby, this);
                new Thread(h, "Client-" + s.getRemoteSocketAddress()).start();
            }
        }
    }

    public void broadcastOnlineList() throws IOException {
        List<User> players = new ArrayList<>();
        for (Map.Entry<User, ClientHandler> e : lobby.online.entrySet()) {
            User u = e.getKey();
            String st = lobby.status.containsKey(u) ? lobby.status.get(u) : "IDLE";
            u.setStatus(st);
            players.add(u);
        }
        Map<String, String> payload = new HashMap<>();
        payload.put("players", Json.to(players));
        for (ClientHandler h : lobby.online.values()) {
            h.send("ONLINE_LIST", payload);
        }
    }

    // Bắt đầu 1 trận 15 vòng
    public void startMatch(Room room) throws Exception {
        Match match = new Match();
        match.setStartedAt(LocalDateTime.now());
        match.setRoomOwner(room.owner);

        int matchId = matchDao.createMatch(match);
        match.setId(matchId);

        MatchContext ctx = new MatchContext(matchId, new ArrayList<User>(room.members));
        matches.put(matchId, ctx);
        for (User u : room.members) {
            MatchPlayer matchPlayer = new MatchPlayer();
            User user = new User();
            user.setId(u.getId());

            matchPlayer.setUser(user);
            matchPlayer.setMatch(match);

            matchPlayerDao.savePlayer(matchPlayer);
            lobby.status.put(u, "PLAYING");
            ClientHandler h = lobby.online.get(u);
            if (h != null) {
                Map<String, String> payload = new HashMap<>();
                payload.put("matchId", matchId + "");
                payload.put("rounds", 15 + "");
                payload.put("players", Json.to(ctx.players));
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

                Round round = new Round();

                Match match = new Match();
                match.setId(ctx.matchId);

                round.setMatch(match);

                round.setRoundNo(roundNo);
                round.setLevel(spec.level);
                round.setColors(colors);
                round.setShowMs(spec.showMs);
                round.setCountDownMs(spec.countdownMs);

                int roundId = roundDao.createRound(round);
                round.setId(roundId);
                long serverEpoch = System.currentTimeMillis();
                ctx.currentRound = new LiveRound(roundId, roundNo, spec, colors, serverEpoch);

                Map<String, String> payload = new HashMap<>();
                payload.put("round", Json.to(round));
                payload.put("serverEpochMs", serverEpoch + "");

                for (User u : ctx.players) {
                    ClientHandler h = lobby.online.get(u);
                    if (h != null) h.send("ROUND_DATA", payload);
                }

                // Chờ hết thời lượng vòng (show + countdown) + đệm nhỏ 500ms
                Thread.sleep(spec.showMs + spec.countdownMs + 500);

                // Với những ai chưa gửi → tính 0 điểm, thời gian = countdownMs
                for (User u : ctx.players) {
                    if (!ctx.roundSubmittedUsers.contains(u)) {
                        float score = 0.0f;
                        long timeMs = spec.countdownMs;


                        RoundResult roundResult = new RoundResult();
                        Round round1 = new Round();
                        round1.setId(ctx.currentRound.roundId);

                        roundResult.setRound(round1);
                        roundResult.setUser(u);
                        roundResult.setSelectedColors(null);
                        roundResult.setScore(score);
                        roundResult.setTimeMs(timeMs);
                        roundResult.setSentAt(LocalDateTime.now());

                        roundResultDao.save(roundResult);

                        MatchPlayer matchPlayer = new MatchPlayer();
                        Match match1 = new Match();
                        match1.setId(ctx.matchId);

                        matchPlayer.setMatch(match1);
                        matchPlayer.setUser(u);
                        matchPlayer.setTotalScore(score);
                        matchPlayer.setTotalTimeMs(timeMs);

                        matchPlayerDao.updatePlayerTotals(matchPlayer);
                    }
                }

                // Gửi ROUND_RESULT tóm tắt
                List<MatchPlayer> lb = ctx.buildLeaderboard(matchPlayerDao);
                for (User u : ctx.players) {
                    ClientHandler h = lobby.online.get(u);
                    if (h != null) {
                        Map<String, String> rrPayload = new HashMap<>();
                        rrPayload.put("leaderboard", Json.to(lb));
                        h.send("UPDATE_TABLE_SCORE", rrPayload);
                    }
                }

                Thread.sleep(1000);

                // reset trạng thái tạm cho vòng tiếp
                ctx.roundSubmittedUsers.clear();
                ctx.currentRound = null;
            }

            // Kết thúc trận
            Match match = new Match();
            match.setId(ctx.matchId);
            matchDao.endMatch(match);
            List<MatchPlayer> finalRank = matchPlayerDao.finalRanking(ctx.matchId);
            for (User u : ctx.players) {
                ClientHandler h = lobby.online.get(u);
                if (h != null) {
                    Map<String, String> endPayload = new HashMap<>();
                    endPayload.put("matchId", ctx.matchId + "");
                    endPayload.put("finalRanking", Json.to(finalRank));
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
    public void handleSubmit(User user, Message m) throws Exception {
        RoundResult roundResult = Json.from(m.payload.get("roundResult"), new TypeReference<RoundResult>() {
        });


        long clientEpochMs = Long.parseLong(m.payload.get("clientEpochMs"));

        MatchContext ctx = matches.get(roundResult.getRound().getMatch().getId());
        if (ctx == null || ctx.currentRound == null || ctx.currentRound.roundNo != roundResult.getRound().getRoundNo()) return;

        long elapsed = Math.max(0, clientEpochMs - ctx.currentRound.serverEpochMs);
        long timeMs = Math.min(elapsed, ctx.currentRound.spec.countdownMs);

        float score = Score.calcScore(roundResult.getSelectedColors(), ctx.currentRound.colors);

        roundResult.setUser(user);
        roundResult.setScore(score);
        roundResult.setTimeMs(timeMs);
        roundResult.setSentAt(LocalDateTime.now());


        roundResultDao.save(roundResult);

        MatchPlayer matchPlayer = new MatchPlayer();
        Match match = roundResult.getRound().getMatch();

        matchPlayer.setMatch(match);
        matchPlayer.setUser(user);
        matchPlayer.setTotalScore(score);
        matchPlayer.setTotalTimeMs(timeMs);

        matchPlayerDao.updatePlayerTotals(matchPlayer);

        ctx.roundSubmittedUsers.add(user);
    }

    public void handleHistory(ClientHandler h, Message m) throws Exception {
        Map<String, String> payload = new HashMap<>();
        payload.put("items", Json.to(new ArrayList<>()));
        h.send("HISTORY_RESPONSE", payload);
    }

    public void handleLeaderboard(ClientHandler h, Message m) throws Exception {
        Map<String, String> payload = new HashMap<>();
        payload.put("items", Json.to(new ArrayList<>()));
        h.send("LEADERBOARD_RESPONSE", payload);
    }

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
            if (r <= 5) return new RoundSpec("EASY", 3, 8000);
            if (r <= 10) return new RoundSpec("MEDIUM", 5, 6500);
            return new RoundSpec("HARD", 6, 5500);
        }
    }

    public static class LiveRound {
        public final int roundId;
        public final int roundNo;
        public final RoundSpec spec;
        public final List<String> colors;
        public final long serverEpochMs;

        public LiveRound(int roundId, int roundNo, RoundSpec spec, List<String> colors, long serverEpochMs) {
            this.roundId = roundId;
            this.roundNo = roundNo;
            this.spec = spec;
            this.colors = colors;
            this.serverEpochMs = serverEpochMs;
        }
    }

    public static class MatchContext {
        public final int matchId;
        public final List<User> players;
        public volatile LiveRound currentRound;
        public final Set<User> roundSubmittedUsers = ConcurrentHashMap.newKeySet();

        public MatchContext(int matchId, List<User> players) {
            this.matchId = matchId;
            this.players = players;
        }

        public List<MatchPlayer> buildLeaderboard(MatchPlayerDao dao) {
            try {
                return dao.finalRanking(matchId);
            } catch (Exception e) {
                return new ArrayList<>();
            }
        }
    }
}
