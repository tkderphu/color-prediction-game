package site.viosmash.db;

import site.viosmash.common.model.Invitation;

import java.sql.Connection;
import java.util.List;

/**
 * @author Nguyen Quang Phu
 * @since 08/09/2025
 */
public class InvitationDao {
    private final Connection connection;

    public InvitationDao() {
        connection = DBConnection.getInstance().getConnection();
    }

    public int insert(Invitation invitation) {
        return 1;
    }

    public List<Invitation> findAllByReceiverIdAndIsAccepted(int receiverId, Boolean isAccepted) {
        return null;
    }

    public int updateIsAccepted(int invitationId, Boolean isAccepted) {
        return 1;
    }

}
