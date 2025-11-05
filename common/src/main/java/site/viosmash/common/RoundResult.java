package site.viosmash.common;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Nguyen Quang Phu
 * @since 05/11/2025
 */
public class RoundResult {
    private int id;
    private Round round;
    private User user;
    private List<String> selectedColors;
    private float score;
    private long timeMs;
    private LocalDateTime sentAt;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Round getRound() {
        return round;
    }

    public void setRound(Round round) {
        this.round = round;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<String> getSelectedColors() {
        return selectedColors;
    }

    public void setSelectedColors(List<String> selectedColors) {
        this.selectedColors = selectedColors;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public long getTimeMs() {
        return timeMs;
    }

    public void setTimeMs(long timeMs) {
        this.timeMs = timeMs;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }
}
