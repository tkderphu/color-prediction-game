package site.viosmash;

import site.viosmash.network.ClientConnection;
import site.viosmash.ui.LoginFrame;

import java.io.IOException;
import java.net.Socket;

public class MainClient {
    public static void main(String[] args) throws IOException {
//        ClientConnection connection = new ClientConnection("localhost", 5000);

        LoginFrame loginFrame = new LoginFrame(null);
        loginFrame.setVisible(true);
    }
}
