package site.viosmash.common.model;

import java.time.LocalDateTime;

/**
 * @author Nguyen Quang Phu
 * @since 08/09/2025
 */
public class Session {
    private int id;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private User owner;


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

    public LocalDateTime getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(LocalDateTime endedAt) {
        this.endedAt = endedAt;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }
}
