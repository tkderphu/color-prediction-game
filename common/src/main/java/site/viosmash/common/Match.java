package site.viosmash.common;

import java.time.LocalDateTime;

/**
 * @author Nguyen Quang Phu
 * @since 05/11/2025
 */
public class Match {
    private int id;
    private User roomOwner;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public User getRoomOwner() {
        return roomOwner;
    }

    public void setRoomOwner(User roomOwner) {
        this.roomOwner = roomOwner;
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
}
