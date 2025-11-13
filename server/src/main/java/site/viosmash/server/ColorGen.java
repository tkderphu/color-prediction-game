// server/src/main/java/com/cgo/server/ColorGen.java
package site.viosmash.server;
import site.viosmash.common.Colors;

import java.util.*;

public class ColorGen {
    private static final Random RND = new Random();
    public static List<String> generate(int count) {
        List<String> pool = new ArrayList<>();
        for(int i = 0; i < Colors.BASE.length; i++) {
            pool.add(Colors.BASE[i]);
        }
        Collections.shuffle(pool, RND);
        return pool.subList(0, count);
    }
}
