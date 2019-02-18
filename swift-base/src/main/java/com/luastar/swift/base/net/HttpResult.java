package com.luastar.swift.base.net;

import com.luastar.swift.base.utils.ExceptionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;

import java.io.IOException;
import java.io.InputStream;

public class HttpResult {


    public static final int STATUS_EXP = 9999;

    private int status;
    private long cost;
    private Header[] headers;
    private InputStream inputStream;
    private String result;
    private Exception exception;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getCost() {
        return cost;
    }

    public void setCost(long cost) {
        this.cost = cost;
    }

    public Header[] getHeaders() {
        return headers;
    }

    public void setHeaders(Header[] headers) {
        this.headers = headers;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public String getResult() {
        if (result == null && inputStream != null) {
            try {
                result = IOUtils.toString(inputStream);
            } catch (IOException e) {
                throw ExceptionUtils.unchecked(e);
            } finally {
                IOUtils.closeQuietly(inputStream);
            }
        }
        return result;
    }

    public String getResult(String charset) {
        if (result == null && inputStream != null) {
            try {
                result = IOUtils.toString(inputStream, charset);
            } catch (IOException e) {
                throw ExceptionUtils.unchecked(e);
            } finally {
                IOUtils.closeQuietly(inputStream);
            }
        }
        return result;
    }

}
