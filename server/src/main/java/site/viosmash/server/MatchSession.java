package site.viosmash.server;

import java.net.Socket;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class MatchSession {
    private ConcurrentHashMap<Integer, List<Socket>> clientsJoinedMatches;
}
