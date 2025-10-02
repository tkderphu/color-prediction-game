package site.viosmash.client;

/**
 * @author Nguyen Quang Phu
 * @since 01/10/2025
 */
import site.viosmash.common.Json;
import site.viosmash.common.Message;

import java.io.*;
import java.net.Socket;
import java.util.Map;
import java.util.function.Consumer;

public class NetClient {
    private Socket socket;
    private BufferedReader in;
    private BufferedWriter out;
    private Thread readerThread;
    private Consumer<Message> onMessage;

    public void connect(String host, int port, Consumer<Message> onMessage) throws Exception {
        this.onMessage = onMessage;
        socket = new Socket(host, port);
        in  = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
        readerThread = new Thread(this::readLoop, "Client-Reader");
        readerThread.start();
    }

    private void readLoop() {
        try {
            String line;
            while ((line = in.readLine()) != null) {
                Message m = Json.from(line, Message.class);
                if (onMessage != null) onMessage.accept(m);
            }
        } catch (Exception ignored) {}
    }

    public synchronized void send(String type, Map<String,Object> payload) throws IOException {
        Message m = new Message();
        m.type = type; m.payload = payload;
        String s = Json.to(m);
        out.write(s); out.write("\n"); out.flush();
    }

    public void close() {
        try { socket.close(); } catch (Exception ignored) {}
    }
}