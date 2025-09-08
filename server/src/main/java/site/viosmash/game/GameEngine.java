package site.viosmash.game;

import site.viosmash.common.instruction.ColorResponse;
import site.viosmash.common.model.Session;
import site.viosmash.network.ClientHandler;

import java.util.List;

public class GameEngine {
    private List<ClientHandler> clientPlayGames;
    private Session session;
    private final String[] COLORS = {};

    public ColorResponse randomColor() {
        return null;
    }


    public void send() {
        clientPlayGames.forEach(client -> {
            client.sendResponse(randomColor());
        });
    }
}
