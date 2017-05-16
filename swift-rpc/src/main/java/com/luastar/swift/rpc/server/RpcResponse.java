package com.luastar.swift.rpc.server;

import java.io.Serializable;

/**
 * 封装 RPC 响应
 */
public class RpcResponse implements Serializable {

    private String requestId;
    private Object result;
    private Exception exception;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

}
