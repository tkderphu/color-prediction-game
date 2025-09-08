package site.viosmash.common.instruction;

import site.viosmash.common.model.User;

import java.io.Serializable;

public class LoginResponse extends BaseResponse implements Serializable {
    private User user;



    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
