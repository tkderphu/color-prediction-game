package site.viosmash;

import site.viosmash.common.Instruction;
import site.viosmash.db.DBConnection;
import site.viosmash.db.UserDao;
import site.viosmash.handle.InstructionContext;
import site.viosmash.handle.LoginStrategy;
import site.viosmash.network.Server;

import java.io.IOException;

public class MainServer {
    public static void main(String[] args) throws IOException {
        DBConnection dbConnection = new DBConnection();
        UserDao userDao = new UserDao(dbConnection.getConnection());
        InstructionContext instructionContext = new InstructionContext();
        instructionContext.register(Instruction.LOGIN, new LoginStrategy(userDao));

        Server server = new Server(5000, instructionContext);

        server.start();
    }
}
