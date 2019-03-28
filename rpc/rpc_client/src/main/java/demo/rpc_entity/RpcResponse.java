package demo.rpc_entity;

import java.io.Serializable;

public class RpcResponse implements Serializable {

    private int code;
    private String requestId;
    private String error_msg;
    private Object data;

    public RpcResponse(){
        this.code = new Integer(0);
        this.requestId = new String();
        this.error_msg = new String();
        this.data = new Object();

    };

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getError_msg() {
        return error_msg;
    }

    public void setError_msg(String error_msg) {
        this.error_msg = error_msg;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public RpcResponse(int code,String requestId, String error_msg, Object data) {
        this.code = code;
        this.requestId = requestId;
        this.error_msg = error_msg;
        this.data = data;
    }

    public void setCode(int i) {
        this.code = i;
    }
    public int getCode() {
        return code;
    }
}
