package site.viosmash.server.vo;

public class RankRespVO {
    private int id;
    private UserRespVO user;
    private int score;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public UserRespVO getUser() {
        return user;
    }

    public void setUser(UserRespVO user) {
        this.user = user;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
