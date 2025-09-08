package site.viosmash.common.instruction;

public enum Instruction {
    LOGIN("đăng nhập"), //phuc
    REGISTER("đăng ký"), //
    DISPLAY_INVITATION("hiển thị danh sách lời mời chơi game"),
    DISPLAY_HISTORY("hiển thị danh sách lịch sử chơi game của người chơi hiện tại"),
    DISPLAY_PLAYER("hiển thị toàn bộ danh sách người chơi của hệ thống"),
    DISPLAY_SCORE("hiển thị bảng điểm khi chơi game"),
    ACCEPT_INVITATION("Chấp nhận tham gia chơi game"),
    INVITE_PLAY_GAME("Mời player khác thi tham gia thi đấu"),
    CLOSE_SESSION_GAME("Đóng session chơi game"),
    PLAY_GAME("chơi game"),
    LOGOUT("đăng xuất"),
    ERROR("lỗi khi không tồn tại instruction như trên");


    private final String description;

    Instruction(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
