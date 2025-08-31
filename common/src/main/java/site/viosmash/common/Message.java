package site.viosmash.common;

import java.io.Serializable;

public class Message implements Serializable {
    private Instruction instruction;
    private Object payload;

    public Message(Instruction instruction, Object payload) {
        this.instruction = instruction;
        this.payload = payload;
    }

    public Instruction getInstruction() { return instruction; }
    public Object getPayload() { return payload; }
}
