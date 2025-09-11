package site.viosmash.handle;

import site.viosmash.common.instruction.Message;
import site.viosmash.common.instruction.SubmitRequest;
import site.viosmash.common.model.Score;
import site.viosmash.context.ContextHolder;
import site.viosmash.db.RoundDao;
import site.viosmash.db.ScoreDao;
import site.viosmash.game.GameEngine;
import site.viosmash.network.ClientHandler;

import java.util.List;

/**
 * @author Nguyen Quang Phu
 * @since 10/09/2025
 */
public class SubmitStrategy implements InstructionStrategy{

    private final RoundDao roundDao;
    private final ScoreDao scoreDao;

    public SubmitStrategy(RoundDao roundDao, ScoreDao scoreDao) {
        this.roundDao = roundDao;
        this.scoreDao = scoreDao;
    }

    @Override
    public void execute(Message message, ClientHandler clientHandler) {
        SubmitRequest submitRequest = (SubmitRequest) message.getPayload();

        List<String> correct = submitRequest.getRound().getCurrentColors();
        int points = 0;

        for (String color : submitRequest.getGuessedColors()) {
            if (correct.contains(color)) {
                points++;
            }
        }
        Score score = new Score();
        score.setUser(clientHandler.getUser());
        score.setPoint(points);
        score.setTimestamp(System.currentTimeMillis());
        score.setRound(submitRequest.getRound());

        this.scoreDao.create(score);

        GameEngine gameEngine = new GameEngine(
                ContextHolder.SESSION_CONTEXT.get(submitRequest.getRound().getSession().getId()),
                roundDao,
                submitRequest.getRound().getSession()
        );

        gameEngine.send();
    }
}
