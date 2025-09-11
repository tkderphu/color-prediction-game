package site.viosmash.common.model;

import java.time.LocalDateTime;

/**
 * @author Nguyen Quang Phu
 * @since 08/09/2025
 */
public class Session {
    private int id;
    private LocalDateTime startedAt;
    private int totalRound;



    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }


    public int getTotalRound() {
        return totalRound;
    }

    public void setTotalRound(int totalRound) {
        this.totalRound = totalRound;
    }
}
