package site.viosmash.common.instruction;

import java.io.Serializable;

public class ErrorResponse extends BaseResponse implements Serializable {

     public ErrorResponse() {
         setSuccess(false);
     }
     public ErrorResponse(String message) {
         this();
         setMessage(message);
     }
}
