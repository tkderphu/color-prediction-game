package site.viosmash.server;

import site.viosmash.server.vo.UserRespVO;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class GameServer {
    private int port;
    private ServerSocket serverSocket;
    private List<GameClient> onlineClients;


    public GameServer(int port) throws IOException {
        this.port = port;
        this.serverSocket = new ServerSocket(port);
        this.onlineClients = new ArrayList<>();
    }

    public void listening() throws IOException {
        String msg = "";
        System.out.println("Game server is running at port: " + port);
        while(true) {
            Socket socketClient = serverSocket.accept();
            /**
             * Add new online user
             */
            addNewOnlineClient(socketClient);




        }
    }

    /**
     * Match session
     */
    private void handleMatchSession() {

    }

    private void addNewOnlineClient(Socket socketClient) throws IOException {
        GameClient gameClient = new GameClient(socketClient);
        //readUser from socket
        UserRespVO user = null;
        gameClient.setUser(user);

        this.onlineClients.add(gameClient);
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    public void setServerSocket(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public List<GameClient> getOnlineClients() {
        return onlineClients;
    }

    public void setOnlineClients(List<GameClient> onlineClients) {
        this.onlineClients = onlineClients;
    }
}
