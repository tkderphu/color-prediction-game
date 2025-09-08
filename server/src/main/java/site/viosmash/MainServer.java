package site.viosmash;

import site.viosmash.common.instruction.Instruction;
import site.viosmash.db.DBConnection;
import site.viosmash.db.UserDao;
import site.viosmash.handle.InstructionContext;
import site.viosmash.handle.LoginStrategy;
import site.viosmash.handle.RegisterStrategy;
import site.viosmash.network.Server;

import java.io.IOException;

public class MainServer {
    public static void main(String[] args) throws IOException {
        UserDao userDao = new UserDao();
        InstructionContext instructionContext = new InstructionContext();

        /**
         *
         */
        instructionContext.register(Instruction.LOGIN, new LoginStrategy(userDao));
        instructionContext.register(Instruction.REGISTER, new RegisterStrategy(userDao));

        Server server = new Server(5000, instructionContext);

        server.start();
    }
}
