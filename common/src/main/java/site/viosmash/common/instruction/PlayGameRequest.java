package site.viosmash.common.instruction;

import java.io.Serializable;

/**
 * @author Nguyen Quang Phu
 * @since 08/09/2025
 */
public class PlayGameRequest implements Serializable {
    private int userId;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
