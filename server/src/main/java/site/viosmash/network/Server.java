package site.viosmash.network;

import site.viosmash.handle.InstructionContext;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private int port;
    private InstructionContext context;

    public Server(int port, InstructionContext context) {
        this.port = port;
        this.context = context;
    }

    public void start() throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Server running on port " + port);

        while (true) {
            Socket clientSocket = serverSocket.accept();
            new Thread(new ClientHandler(clientSocket, context)).start();
        }
    }
}
