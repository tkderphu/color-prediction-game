// server/src/main/java/com/cgo/server/Room.java
package site.viosmash.server;
import java.util.*;
public class Room {
    public  String owner;
    public final Set<String> members = new LinkedHashSet<>();
    public Room(String owner) {
        this.owner = owner;
        this.members.add(owner);
    }
}
