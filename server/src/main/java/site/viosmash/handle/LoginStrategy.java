package site.viosmash.handle;

import site.viosmash.common.instruction.LoginRequest;
import site.viosmash.common.instruction.LoginResponse;
import site.viosmash.common.instruction.Message;
import site.viosmash.common.model.User;
import site.viosmash.db.UserDao;
import site.viosmash.network.ClientHandler;

public class LoginStrategy implements InstructionStrategy{

    private final UserDao userDao;

    public LoginStrategy(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public void execute(Message message, ClientHandler clientHandler) {
        LoginRequest loginRequest = (LoginRequest) message.getPayload();
        User user = userDao.findByUsernameAndPassword(
                loginRequest.getUsername(),
                loginRequest.getPassword()
        );

        LoginResponse loginResponse = new LoginResponse();
        if(user == null) {
            loginResponse.setMessage("Username or password invalid");
            loginResponse.setSuccess(false);
        } else {
            loginResponse.setMessage("Login successfully");
            loginResponse.setSuccess(true);
            loginResponse.setUser(user);
            clientHandler.setUser(user);
        }

        clientHandler.sendResponse(loginResponse);
    }
}
