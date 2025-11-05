package site.viosmash.common;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Nguyen Quang Phu
 * @since 05/11/2025
 */
public class Round {
    private int id;
    private Match match;
    private int roundNo;
    private String level;
    private int showMs;
    private int countDownMs;
    private List<String> colors;
    private LocalDateTime sentAt;

    public List<String> getColors() {
        return colors;
    }

    public void setColors(List<String> colors) {
        this.colors = colors;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Match getMatch() {
        return match;
    }

    public void setMatch(Match match) {
        this.match = match;
    }

    public int getRoundNo() {
        return roundNo;
    }

    public void setRoundNo(int roundNo) {
        this.roundNo = roundNo;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public int getShowMs() {
        return showMs;
    }

    public void setShowMs(int showMs) {
        this.showMs = showMs;
    }

    public int getCountDownMs() {
        return countDownMs;
    }

    public void setCountDownMs(int countDownMs) {
        this.countDownMs = countDownMs;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }
}
