package site.viosmash.common.model;

import java.io.Serializable;
import java.sql.Timestamp;

public class User implements Serializable {

    private int id;
    private String username;
    private String password;
    private String email;
    private String fullName;
    private Timestamp createdAt;
    private Timestamp lastLogin;
    private boolean isOnline;
    private String status;  // "free", "in_room", "playing"

    public User() {}

    public User(int id, String username, String password, boolean isOnline, String status) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.isOnline = isOnline;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getLastLogin() { return lastLogin; }
    public void setLastLogin(Timestamp lastLogin) { this.lastLogin = lastLogin; }

    public boolean isOnline() { return isOnline; }
    public void setOnline(boolean online) { isOnline = online; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

}