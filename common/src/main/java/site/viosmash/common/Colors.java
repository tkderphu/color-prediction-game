package site.viosmash.common;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
public class Colors {
    public static final String[] BASE = {
            "RED","GREEN","BLUE","YELLOW","ORANGE","CYAN","PINK","GRAY",
            "BLACK","WHITE","MAGENTA","BROWN",
            "OLIVE","PURPLE","NAVY","TEAL","MAROON","SALMON",
            "CORAL","GOLD","INDIGO","TURQUOISE","VIOLET"
    };

    public static final Color[] COLORS = {
            Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.ORANGE,
            Color.CYAN, Color.PINK, Color.GRAY, Color.BLACK, Color.WHITE,
            Color.MAGENTA, new Color(139,69,19), // BROWN
            new Color(128,128,0), // OLIVE
            new Color(128,0,128), // PURPLE
            new Color(0,0,128), // NAVY
            new Color(0,128,128), // TEAL
            new Color(128,0,0), // MAROON
            new Color(250,128,114), // SALMON
            new Color(255,127,80), // CORAL
            new Color(255,215,0), // GOLD
            new Color(75,0,130), // INDIGO
            new Color(64,224,208), // TURQUOISE
            new Color(238,130,238) // VIOLET
    };
}