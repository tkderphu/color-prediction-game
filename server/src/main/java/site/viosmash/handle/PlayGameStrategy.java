package site.viosmash.handle;

import site.viosmash.common.instruction.Message;
import site.viosmash.common.instruction.PlayGameRequest;
import site.viosmash.common.instruction.PlayGameResponse;
import site.viosmash.common.model.Session;
import site.viosmash.common.model.User;
import site.viosmash.db.SessionDao;
import site.viosmash.db.UserDao;
import site.viosmash.network.ClientHandler;

import java.time.LocalDateTime;

/**
 * @author Nguyen Quang Phu
 * @since 08/09/2025
 */
public class PlayGameStrategy implements InstructionStrategy{

    private final SessionDao sessionDao;
    private final UserDao userDao;
    public PlayGameStrategy(SessionDao sessionDao, UserDao userDao) {
        this.sessionDao = sessionDao;
        this.userDao = userDao;
    }

    @Override
    public void execute(Message message, ClientHandler clientHandler) {
        PlayGameRequest playGameRequest = (PlayGameRequest) message.getPayload();
        User user = userDao.findById(playGameRequest.getUserId());

        Session session = new Session();
        session.setStartedAt(LocalDateTime.now());
        session.setOwner(user);

        int sessionId = this.sessionDao.insert(session);

        session.setId(sessionId);


        PlayGameResponse playGameResponse = new PlayGameResponse();
        playGameResponse.setMessage("session is created");
        playGameResponse.setSuccess(true);
        playGameResponse.setSession(session);

        clientHandler.sendResponse(session);
    }
}
