package site.viosmash.network;

import site.viosmash.common.LoginResponse;
import site.viosmash.common.Message;
import site.viosmash.common.User;
import site.viosmash.handle.InstructionContext;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket socket;
    private InstructionContext instructionContext;
    private User user;

    public ClientHandler(Socket socket, InstructionContext instructionContext) {
        this.socket = socket;
        this.instructionContext = instructionContext;
    }

    @Override
    public void run() {
        try (ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {

            while (true) {
                Message request = (Message) in.readObject();
                instructionContext.execute(request, this);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendResponse(Object response) {
        try {
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.writeObject(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
