package site.viosmash.common.instruction;

import site.viosmash.common.model.Round;

import java.io.Serializable;
import java.util.List;

/**
 * @author Nguyen Quang Phu
 * @since 08/09/2025
 */
public class ServerResponseColor implements Serializable {
    private final Round round;
    private final int displayMillis = 3000;
    private final int selectionMillis = 5000;

    public ServerResponseColor(Round round) {
        this.round = round;
    }


    public int getDisplayMillis() {
        return displayMillis;
    }

    public int getSelectionMillis() {
        return selectionMillis;
    }

    public Round getRound() {
        return round;
    }
}
