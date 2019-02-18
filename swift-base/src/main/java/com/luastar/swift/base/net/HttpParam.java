package com.luastar.swift.base.net;

import java.util.Map;

/**
 * http请求参数
 */
public class HttpParam {

    protected static final int DEFAULT_TIMEOUT = 120000;
    protected static final String DEFAULT_CHARSET = "UTF-8";

    /**
     * URL
     */
    private String url;
    /**
     * GET/POST(application/x-www-form-urlencoded)参数
     */
    private Map<String, String> paramMap;
    /**
     * header参数
     */
    private Map<String, String> headerMap;
    /**
     * POST(form-data)参数
     */
    private Map<String, Object> fileMap;
    /**
     * 远程文件参数
     */
    private Map<String, String> urlParamMap;
    /**
     * 文件名参数
     */
    private Map<String, String> fileNameMap;
    /**
     * POST(application/json)参数
     */
    private String requestBody;
    /**
     * 超时
     */
    private int timeout;
    /**
     * 编码
     */
    private String charset;
    /**
     * 文件路径
     */
    private String filePath;

    HttpParam() {
    }

    HttpParam(String url,
              Map<String, String> paramMap,
              Map<String, String> headerMap,
              Map<String, Object> fileMap,
              Map<String, String> urlParamMap,
              Map<String, String> fileNameMap,
              String requestBody,
              int timeout,
              String charset,
              String filePath) {
        this.url = url;
        this.paramMap = paramMap;
        this.headerMap = headerMap;
        this.fileMap = fileMap;
        this.urlParamMap = urlParamMap;
        this.fileNameMap = fileNameMap;
        this.requestBody = requestBody;
        this.timeout = timeout;
        this.charset = charset;
        this.filePath = filePath;
    }

    public String getUrl() {
        return url;
    }

    public Map<String, String> getParamMap() {
        return paramMap;
    }

    public Map<String, String> getHeaderMap() {
        return headerMap;
    }

    public Map<String, Object> getFileMap() {
        return fileMap;
    }

    public Map<String, String> getUrlParamMap() {
        return urlParamMap;
    }

    public Map<String, String> getFileNameMap() {
        return fileNameMap;
    }

    public String getRequestBody() {
        return requestBody;
    }

    public int getTimeout() {
        return timeout;
    }

    public String getCharset() {
        return charset;
    }

    public String getFilePath() {
        return filePath;
    }

    public static HttpParam.Builder builder() {
        return new HttpParam.Builder();
    }

    public static HttpParam.Builder copy(final HttpParam param) {
        return new HttpParam.Builder()
                .setUrl(param.getUrl())
                .setParamMap(param.getParamMap())
                .setHeaderMap(param.getHeaderMap())
                .setFileMap(param.getFileMap())
                .setUrlParamMap(param.getUrlParamMap())
                .setFileNameMap(param.getFileNameMap())
                .setRequestBody(param.getRequestBody())
                .setTimeout(param.getTimeout())
                .setCharset(param.charset)
                .setFilePath(param.getFilePath());
    }

    public static class Builder {
        /**
         * URL
         */
        private String url;
        /**
         * GET/POST(application/x-www-form-urlencoded)参数
         */
        private Map<String, String> paramMap;
        /**
         * header参数
         */
        private Map<String, String> headerMap;
        /**
         * POST(form-data)参数
         */
        private Map<String, Object> fileMap;
        /**
         * 远程文件参数
         */
        private Map<String, String> urlParamMap;
        /**
         * 文件名参数
         */
        private Map<String, String> fileNameMap;
        /**
         * POST(application/json)参数
         */
        private String requestBody;
        /**
         * 超时
         */
        private int timeout;
        /**
         * 编码
         */
        private String charset;
        /**
         * 文件路径
         */
        private String filePath;

        public Builder() {
            this.timeout = DEFAULT_TIMEOUT;
            this.charset = DEFAULT_CHARSET;
        }

        public Builder setUrl(String url) {
            this.url = url;
            return this;
        }

        public Builder setParamMap(Map<String, String> paramMap) {
            this.paramMap = paramMap;
            return this;
        }

        public Builder setHeaderMap(Map<String, String> headerMap) {
            this.headerMap = headerMap;
            return this;
        }

        public Builder setFileMap(Map<String, Object> fileMap) {
            this.fileMap = fileMap;
            return this;
        }

        public Builder setUrlParamMap(Map<String, String> urlParamMap) {
            this.urlParamMap = urlParamMap;
            return this;
        }

        public Builder setFileNameMap(Map<String, String> fileNameMap) {
            this.fileNameMap = fileNameMap;
            return this;
        }

        public Builder setRequestBody(String requestBody) {
            this.requestBody = requestBody;
            return this;
        }

        public Builder setTimeout(int timeout) {
            this.timeout = timeout;
            return this;
        }

        public Builder setCharset(String charset) {
            this.charset = charset;
            return this;
        }

        public Builder setFilePath(String filePath) {
            this.filePath = filePath;
            return this;
        }

        public HttpParam build() {
            return new HttpParam(url,
                    paramMap,
                    headerMap,
                    fileMap,
                    urlParamMap,
                    fileNameMap,
                    requestBody,
                    timeout,
                    charset,
                    filePath);
        }

    }

}
