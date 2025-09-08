package site.viosmash.context;

import site.viosmash.common.model.User;

/**
 * Luu thong sau thi login
 */
public class ThreadLocalContext {
    private final ThreadLocal<User> THREAD_LOCAL = new ThreadLocal<>();

    public void set(User user) {
        THREAD_LOCAL.set(user);
    }

    public User get() {
        return THREAD_LOCAL.get();
    }

    public void clear() {
        THREAD_LOCAL.remove();
    }
}
