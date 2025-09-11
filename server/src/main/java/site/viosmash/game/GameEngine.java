package site.viosmash.game;

import site.viosmash.common.instruction.Instruction;
import site.viosmash.common.instruction.Message;
import site.viosmash.common.model.Round;
import site.viosmash.common.instruction.ServerResponseColor;
import site.viosmash.common.model.Session;
import site.viosmash.db.RoundDao;
import site.viosmash.network.ClientHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class GameEngine {
    private final List<ClientHandler> clientPlayGames;
    private final RoundDao roundDao;
    private final Session session;

    private static final String[] COLORS = {
            "#FF0000", // red
            "#00FF00", // green
            "#0000FF", // blue
            "#FFFF00", // yellow
            "#FF00FF", // magenta
            "#00FFFF", // cyan
            "#FFFFFF", // white
            "#000000"  // black
    };

    public GameEngine(List<ClientHandler> clientPlayGames,
                      RoundDao roundDao,
                      Session session) {
        this.clientPlayGames = clientPlayGames;
        this.roundDao = roundDao;
        this.session = session;
    }

    public List<String> randomColor() {

        int count = 3;
        List<String> palette = new ArrayList<>(Arrays.asList(COLORS));
        Collections.shuffle(palette, ThreadLocalRandom.current());
        List<String> chosen = palette.subList(0, count);

        return chosen;
    }




    public Boolean send() {
        List<String> colors = randomColor();
        Round previousRound = roundDao.findMaxRoundBySessionId(session.getId());
        Round currentRound = new Round();
        currentRound.setCurrentColors(colors);
        currentRound.setSession(session);
        if(previousRound == null) {
            currentRound.setCurrentRound(1);
        } else {
            if(currentRound.getCurrentRound() > session.getTotalRound()) {
                return false;
            }
            currentRound.setCurrentRound(previousRound.getCurrentRound());
        }
        ServerResponseColor responseColor = new ServerResponseColor(currentRound);
        Message message = new Message(Instruction.NEXT_ROUND, responseColor);
        clientPlayGames.forEach(client -> {
            client.sendResponse(message);
        });
        return true;
    }
}
