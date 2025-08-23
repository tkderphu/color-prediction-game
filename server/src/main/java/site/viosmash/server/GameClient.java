package site.viosmash.server;

import site.viosmash.server.vo.UserRespVO;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;

public class GameClient implements Runnable{
    private BufferedInputStream bis;
    private BufferedOutputStream bio;
    private Socket socket;
    private UserRespVO user;

    public GameClient() {

    }

    public GameClient(Socket socket) throws IOException {
        this.bio = new BufferedOutputStream(socket.getOutputStream());
        this.bis = new BufferedInputStream(socket.getInputStream());
        this.socket = socket;
    }

    public GameClient(Socket socket, UserRespVO user) throws IOException {
        this(socket);
        this.user = user;
    }

    @Override
    public void run() {

    }

    public BufferedInputStream getBis() {
        return bis;
    }


    public BufferedOutputStream getBio() {
        return bio;
    }



    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public UserRespVO getUser() {
        return user;
    }

    public void setUser(UserRespVO user) {
        this.user = user;
    }
}
