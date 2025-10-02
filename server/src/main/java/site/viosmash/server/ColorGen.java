// server/src/main/java/com/cgo/server/ColorGen.java
package site.viosmash.server;
import site.viosmash.common.Colors;

import java.util.*;

public class ColorGen {
    private static final Random RND = new Random();
    public static List<String> generate(int count) {
        List<String> pool = new ArrayList<>(Colors.BASE);
        Collections.shuffle(pool, RND);
        return pool.subList(0, count);
    }
}
