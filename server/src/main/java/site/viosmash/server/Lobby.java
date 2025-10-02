// server/src/main/java/com/cgo/server/Lobby.java
package site.viosmash.server;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Lobby {
    // username -> handler
    public final Map<String, ClientHandler> online = new ConcurrentHashMap<>();
    // username -> status ("IDLE" | "PLAYING")
    public final Map<String, String> status = new ConcurrentHashMap<>();
    // owner -> room
    public final Map<String, Room> rooms = new ConcurrentHashMap<>();

    public synchronized Room getOrCreateRoom(String owner) {
        return rooms.computeIfAbsent(owner, Room::new);
    }
    public synchronized void dissolveIfEmpty(String owner) {
        Room r = rooms.get(owner);
        if (r != null && r.members.isEmpty()) rooms.remove(owner);
    }
}
