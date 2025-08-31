package site.viosmash.handle;

import site.viosmash.common.ErrorResponse;
import site.viosmash.common.Instruction;
import site.viosmash.common.Message;
import site.viosmash.network.ClientHandler;

import java.util.HashMap;
import java.util.Map;

public class InstructionContext {
    private final Map<Instruction, InstructionStrategy> strategies = new HashMap<>();

    public void register(Instruction instruction, InstructionStrategy strategy) {
        strategies.put(instruction, strategy);
    }

    public void execute(Message msg, ClientHandler handler) {
        InstructionStrategy strategy = strategies.get(msg.getInstruction());
        if (strategy != null) {
            strategy.execute(msg, handler);
        } else {
            handler.sendResponse(new Message(
                    Instruction.ERROR,
                    new ErrorResponse("Unknown instruction: " + msg.getInstruction())
            ));
        }
    }
}
