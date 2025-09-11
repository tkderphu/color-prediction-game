package site.viosmash.common.instruction;

import java.io.Serializable;
import java.util.List;

/**
 * @author Nguyen Quang Phu
 * @since 08/09/2025
 */
public class PlayGameRequest implements Serializable {
    private int totalRound;
    private List<Integer> playerIds;

    public List<Integer> getPlayerIds() {
        return playerIds;
    }

    public void setPlayerIds(List<Integer> playerIds) {
        this.playerIds = playerIds;
    }

    public int getTotalRound() {
        return totalRound;
    }

    public void setTotalRound(int totalRound) {
        this.totalRound = totalRound;
    }

}
