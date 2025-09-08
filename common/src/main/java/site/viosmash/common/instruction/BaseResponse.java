package site.viosmash.common.instruction;

/**
 * @author Nguyen Quang Phu
 * @since 08/09/2025
 */
public class BaseResponse {
    private String message;
    private Boolean success;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }
}
