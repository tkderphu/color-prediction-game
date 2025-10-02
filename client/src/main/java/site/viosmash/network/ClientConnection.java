package site.viosmash.network;

import site.viosmash.common.instruction.Instruction;
import site.viosmash.common.instruction.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;


public class ClientConnection {
    private Socket socket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    public ClientConnection(String host, int port) throws IOException {
        socket = new Socket(host, port);

        // Quan trọng: Output trước Input
        this.oos = new ObjectOutputStream(socket.getOutputStream());
        this.ois = new ObjectInputStream(socket.getInputStream());
    }




    public void sendMessage(Message message) {
        try {
            this.oos.writeObject(message);
            this.oos.flush();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public Object readMessage() {
        try {
            return this.ois.readObject();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }


}
