package site.viosmash.context;

import site.viosmash.common.model.User;
import site.viosmash.network.ClientHandler;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Nguyen Quang Phu
 * @since 10/09/2025
 */
public class ContextHolder {
    public static final Map<Integer, ClientHandler> CLIENT_CONTEXT = new ConcurrentHashMap<>();
    public static final Map<Integer, List<ClientHandler>> SESSION_CONTEXT = new ConcurrentHashMap<>();

}
