package site.viosmash.handle;

import site.viosmash.common.instruction.Message;
import site.viosmash.db.UserDao;
import site.viosmash.network.ClientHandler;

public class RegisterStrategy implements InstructionStrategy{

    private final UserDao userDao;

    public RegisterStrategy(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public void execute(Message message, ClientHandler clientHandler) {

    }
}
