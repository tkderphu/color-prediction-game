package site.viosmash.common.model;

import java.time.LocalDateTime;

/**
 * @author Nguyen Quang Phu
 * @since 08/09/2025
 */
public class Invitation {
    private int id;
    private User inviter;
    private User receiver;
    private LocalDateTime invitedAt;
    private Boolean isAccepted;

    public Invitation() {

    }

    public Boolean getAccepted() {
        return isAccepted;
    }

    public void setAccepted(Boolean accepted) {
        isAccepted = accepted;
    }

    public Invitation(int id, User inviter, User receiver, LocalDateTime invitedAt) {
        this.id = id;
        this.inviter = inviter;
        this.receiver = receiver;
        this.invitedAt = invitedAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public User getInviter() {
        return inviter;
    }

    public void setInviter(User inviter) {
        this.inviter = inviter;
    }

    public User getReceiver() {
        return receiver;
    }

    public void setReceiver(User receiver) {
        this.receiver = receiver;
    }

    public LocalDateTime getInvitedAt() {
        return invitedAt;
    }

    public void setInvitedAt(LocalDateTime invitedAt) {
        this.invitedAt = invitedAt;
    }
}
