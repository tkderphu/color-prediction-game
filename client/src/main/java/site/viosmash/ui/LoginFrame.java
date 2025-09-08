package site.viosmash.ui;

import site.viosmash.common.instruction.Instruction;
import site.viosmash.common.instruction.LoginRequest;
import site.viosmash.common.instruction.LoginResponse;
import site.viosmash.common.instruction.Message;
import site.viosmash.network.ClientConnection;

import javax.swing.*;

public class LoginFrame extends JFrame {
    private JTextField usernameTf;
    private JTextField passwordTf;
    private JButton loginBtn;

    public LoginFrame(ClientConnection clientConnection) {
        loginBtn.addActionListener((event) -> {
            LoginRequest request = new LoginRequest();
            request.setPassword(passwordTf.getText());
            request.setUsername(usernameTf.getText());

            clientConnection.sendMessage(new Message(
                    Instruction.LOGIN,
                    request
            ));

            LoginResponse loginResponse = (LoginResponse) clientConnection.readMessage();

            if(loginResponse.getSuccess()) {
                /**
                 * hide login page
                 */
                setVisible(false);
                /**
                 * go to another page
                 */
            } else {
                /**
                 * show dialog login failed
                 */
            }
        });
    }
}
