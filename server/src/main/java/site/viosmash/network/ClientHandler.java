package site.viosmash.network;

import site.viosmash.common.instruction.Message;
import site.viosmash.common.model.User;
import site.viosmash.handle.InstructionContext;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;

public class ClientHandler implements Runnable {
    private Socket socket;
    private InstructionContext instructionContext;
    private User user;
    private ObjectOutputStream out;  // Tái sử dụng stream

    public ClientHandler(Socket socket, InstructionContext instructionContext) {
        this.socket = socket;
        this.instructionContext = instructionContext;
    }

    @Override
    public void run() {
        try (ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {  // Chỉ đóng input ở đây
            // Tạo output stream một lần, trước input (nhưng input đã tạo, nên OK vì socket mới)
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();  // Đảm bảo stream sẵn sàng

            System.out.println("Client connected from: " + socket.getInetAddress());

            while (true) {
                try {
                    Message request = (Message) in.readObject();
                    instructionContext.execute(request, this);
                } catch (SocketException e) {
                    if (e.getMessage().contains("Connection reset") || e.getMessage().contains("Socket closed")) {
                        System.out.println("Client " + socket.getInetAddress() + " disconnected unexpectedly.");
                        break;  // Thoát vòng lặp an toàn
                    } else {
                        throw e;  // Re-throw các SocketException khác
                    }
                } catch (ClassNotFoundException e) {
                    System.err.println("Invalid object received from client: " + e.getMessage());
                    break;
                } catch (IOException e) {
                    System.err.println("IO error with client " + socket.getInetAddress() + ": " + e.getMessage());
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Đóng socket an toàn
            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Client handler thread ended.");
        }
    }

    public void sendResponse(Message message) {
        if (out != null) {
            try {
                out.writeObject(message);
                out.flush();
                out.reset();  // Reset stream để tránh buffer cũ (tùy chọn cho performance)
            } catch (IOException e) {
                System.err.println("Failed to send response: " + e.getMessage());
                // Có thể đóng socket nếu lỗi nghiêm trọng
            }
        } else {
            System.err.println("Output stream not initialized.");
        }
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}