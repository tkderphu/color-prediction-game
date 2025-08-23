package site.viosmash.server.vo;

import site.viosmash.enums.MatchType;

import java.io.Serializable;
import java.time.LocalDateTime;

public class MatchRespVO implements Serializable {
    private int id;
    private String name;
    private MatchType matchType;
    private int numberOfRound;
    private LocalDateTime createdAt;
    private UserRespVO owner;
    private boolean isCompleted;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MatchType getMatchType() {
        return matchType;
    }

    public void setMatchType(MatchType matchType) {
        this.matchType = matchType;
    }

    public int getNumberOfRound() {
        return numberOfRound;
    }

    public void setNumberOfRound(int numberOfRound) {
        this.numberOfRound = numberOfRound;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public UserRespVO getOwner() {
        return owner;
    }

    public void setOwner(UserRespVO owner) {
        this.owner = owner;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }
}
