// server/src/main/java/com/cgo/server/Room.java
package site.viosmash.server;
import site.viosmash.common.User;

import java.util.*;
public class Room {
    public  User owner;
    public final Set<User> members = new LinkedHashSet<>();
    public Room(User user) {
        this.owner = owner;
        this.members.add(user);
    }
}
