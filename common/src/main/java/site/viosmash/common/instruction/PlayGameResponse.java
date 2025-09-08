package site.viosmash.common.instruction;

import site.viosmash.common.model.Session;

import java.io.Serializable;

/**
 * @author Nguyen Quang Phu
 * @since 08/09/2025
 */
public class PlayGameResponse extends BaseResponse implements Serializable {
    private Session session;

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }
}
