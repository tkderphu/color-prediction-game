package site.viosmash.common.instruction;

import java.io.Serializable;

/**
 * @author Nguyen Quang Phu
 * @since 08/09/2025
 */
public class BaseResponse implements Serializable {  // Thêm Serializable nếu chưa
    private String message = "";  // Init rỗng
    private Boolean success = false;  // Init false thay vì null

    // Getter/setter giữ nguyên
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
        this.success = success != null ? success : false;  // Fallback nếu set null
    }
}