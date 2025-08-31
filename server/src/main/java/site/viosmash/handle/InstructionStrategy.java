package site.viosmash.handle;

import site.viosmash.common.Message;
import site.viosmash.network.ClientHandler;

public interface InstructionStrategy {
    void execute(Message message, ClientHandler clientHandler);
}
