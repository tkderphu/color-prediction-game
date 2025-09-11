package site.viosmash.common.model;

import java.io.Serializable;
import java.util.List;

/**
 * @author Nguyen Quang Phu
 * @since 10/09/2025
 */
public class Round {
    private int id;
    private Session session;
    private int currentRound;
    private List<String> currentColors;



    public Session getSession() {
        return session;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public int getCurrentRound() {
        return currentRound;
    }

    public void setCurrentRound(int currentRound) {
        this.currentRound = currentRound;
    }

    public List<String> getCurrentColors() {
        return currentColors;
    }

    public void setCurrentColors(List<String> currentColors) {
        this.currentColors = currentColors;
    }
}
