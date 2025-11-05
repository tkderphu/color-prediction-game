// server/src/main/java/com/cgo/server/Lobby.java
package site.viosmash.server;
import site.viosmash.common.User;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Lobby {
    // username -> handler
    public final Map<User, ClientHandler> online = new ConcurrentHashMap<>();
    // username -> status ("IDLE" | "PLAYING")
    public final Map<User, String> status = new ConcurrentHashMap<>();
    // owner -> room
    public final Map<User, Room> rooms = new ConcurrentHashMap<>();

    public synchronized Room getOrCreateRoom(User owner) {
        return rooms.computeIfAbsent(owner, Room::new);
    }
    public synchronized void dissolveIfEmpty(User owner) {
        Room r = rooms.get(owner);
        if (r != null && !r.members.isEmpty()) {
            for(User member: r.members) {
                r.owner = member;
               return;
            }
        }
        if(r != null && r.members.isEmpty()) {
            rooms.remove(owner);
        }
    }
}
