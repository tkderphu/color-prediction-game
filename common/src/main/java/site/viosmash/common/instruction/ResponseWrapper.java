package site.viosmash.common.instruction;


import java.io.Serializable;

public class ResponseWrapper implements Serializable {
    private String message;
    private Object object;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public static ResponseWrapper wrapper(String message) {
        ResponseWrapper responseWrapper = new ResponseWrapper();
        responseWrapper.message = message;
        return responseWrapper;
    }
}
