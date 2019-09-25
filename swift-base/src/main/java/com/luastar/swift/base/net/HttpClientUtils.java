package com.luastar.swift.base.net;

import com.luastar.swift.base.utils.EncodeUtils;
import com.luastar.swift.base.utils.ObjUtils;
import com.luastar.swift.base.utils.RandomUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.NameValuePair;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.net.ssl.SSLContext;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

public class HttpClientUtils {

    private static Logger logger = LoggerFactory.getLogger(HttpClientUtils.class);

    public static final ContentType TEXT_PLAIN_UTF8 = ContentType.create("text/plain", UTF_8);

    /**
     * requestId
     */
    private static final String MDC_KEY_REQUESTID = "requestId";
    /**
     * request_id
     */
    private static final String MDC_KEY_REQUEST_ID = "request_id";

    /**
     * 创建支持https的调用
     *
     * @param url
     * @return
     */
    private static CloseableHttpClient createHttpClient(String url) {
        return createHttpClient(url, null);
    }

    /**
     * 创建自定义重定向策略，支持https的调用
     *
     * @param url
     * @return
     */
    private static CloseableHttpClient createHttpClient(String url, RedirectStrategy redirectStrategy) {
        try {
            HttpClientBuilder httpClientBuilder = HttpClients.custom();
            // 重定向策略
            if (redirectStrategy != null) {
                httpClientBuilder.setRedirectStrategy(redirectStrategy);
            }
            // https支持
            if (StringUtils.isNotEmpty(url) && url.startsWith("https://")) {
                SSLContext sslcontext = SSLContexts.custom().loadTrustMaterial(null, new TrustStrategy() {
                    @Override
                    public boolean isTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                        return true;
                    }
                }).build();
                SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext);
                httpClientBuilder.setSSLSocketFactory(sslsf);
            }
            return httpClientBuilder.build();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return HttpClients.createDefault();
    }

    public static String get(String url) {
        HttpParam param = HttpParam.builder()
                .setUrl(url)
                .build();
        return get(param);
    }

    public static String get(String url, int timeout) {
        HttpParam param = HttpParam.builder()
                .setUrl(url)
                .setTimeout(timeout)
                .build();
        return get(param);
    }

    public static String get(String url, Map<String, String> headMap) {
        HttpParam param = HttpParam.builder()
                .setUrl(url)
                .setHeaderMap(headMap)
                .build();
        return get(param);
    }

    public static String get(String url,
                             int timeout,
                             String charset,
                             Map<String, String> headMap) {
        HttpParam param = HttpParam.builder()
                .setUrl(url)
                .setTimeout(timeout)
                .setCharset(charset)
                .setHeaderMap(headMap)
                .build();
        return get(param);
    }

    public static String get(HttpParam param) {
        CloseableHttpClient httpclient = null;
        CloseableHttpResponse response = null;
        try {
            HttpGet httpGet = new HttpGet(param.getUrl());
            // 超时设置
            RequestConfig requestConfig = RequestConfig.custom()
                    .setSocketTimeout(param.getTimeout())
                    .setConnectTimeout(param.getTimeout())
                    .setConnectionRequestTimeout(param.getTimeout()).build();
            httpGet.setConfig(requestConfig);
            // head设置
            if (ObjUtils.isNotEmpty(param.getHeaderMap())) {
                for (Map.Entry<String, String> entry : param.getHeaderMap().entrySet()) {
                    httpGet.setHeader(entry.getKey(), entry.getValue());
                }
            }
            // requestId
            String requestId = ObjUtils.ifEmpty(MDC.get(MDC_KEY_REQUESTID), MDC.get(MDC_KEY_REQUEST_ID));
            if (ObjUtils.isNotEmpty(requestId)) {
                httpGet.setHeader(MDC_KEY_REQUESTID, requestId);
            }
            httpclient = createHttpClient(param.getUrl());
            long start = System.currentTimeMillis();
            response = httpclient.execute(httpGet);
            long end = System.currentTimeMillis();
            int status = response.getStatusLine().getStatusCode();
            logger.info("请求url：{}，结果状态：{}，耗时：{}毫秒。", param.getUrl(), status, ((end - start)));
            if (status >= 200 && status < 300) {
                HttpEntity entity = response.getEntity();
                return entity != null ? EntityUtils.toString(entity, param.getCharset()) : null;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            org.apache.http.client.utils.HttpClientUtils.closeQuietly(response);
            org.apache.http.client.utils.HttpClientUtils.closeQuietly(httpclient);
        }
        return null;
    }

    public static HttpResult getHttpResult(String url) {
        HttpParam param = HttpParam.builder()
                .setUrl(url)
                .build();
        return getHttpResult(param);
    }

    public static HttpResult getHttpResult(String url, int timeout) {
        HttpParam param = HttpParam.builder()
                .setUrl(url)
                .setTimeout(timeout)
                .build();
        return getHttpResult(param);
    }

    public static HttpResult getHttpResult(String url, Map<String, String> headMap) {
        HttpParam param = HttpParam.builder()
                .setUrl(url)
                .setHeaderMap(headMap)
                .build();
        return getHttpResult(param);
    }

    public static HttpResult getHttpResult(String url,
                                           int timeout,
                                           Map<String, String> headMap) {
        HttpParam param = HttpParam.builder()
                .setUrl(url)
                .setTimeout(timeout)
                .setHeaderMap(headMap)
                .build();
        return getHttpResult(param);
    }

    public static HttpResult getHttpResult(HttpParam param) {
        CloseableHttpClient httpclient = null;
        CloseableHttpResponse response = null;
        HttpResult result = new HttpResult();
        try {
            HttpGet httpGet = new HttpGet(param.getUrl());
            // 超时设置
            RequestConfig requestConfig = RequestConfig.custom()
                    .setSocketTimeout(param.getTimeout())
                    .setConnectTimeout(param.getTimeout())
                    .setConnectionRequestTimeout(param.getTimeout()).build();
            httpGet.setConfig(requestConfig);
            // head设置
            if (ObjUtils.isNotEmpty(param.getHeaderMap())) {
                for (Map.Entry<String, String> entry : param.getHeaderMap().entrySet()) {
                    httpGet.setHeader(entry.getKey(), entry.getValue());
                }
            }
            // requestId
            String requestId = ObjUtils.ifEmpty(MDC.get(MDC_KEY_REQUESTID), MDC.get(MDC_KEY_REQUEST_ID));
            if (ObjUtils.isNotEmpty(requestId)) {
                httpGet.setHeader(MDC_KEY_REQUESTID, requestId);
            }
            httpclient = createHttpClient(param.getUrl());
            long start = System.currentTimeMillis();
            response = httpclient.execute(httpGet);
            long end = System.currentTimeMillis();
            int status = response.getStatusLine().getStatusCode();
            logger.info("请求url：{}，结果状态：{}，耗时：{}毫秒。", param.getUrl(), status, ((end - start)));
            result.setStatus(status);
            result.setCost(end - start);
            result.setHeaders(response.getAllHeaders());
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                byte[] content = EntityUtils.toByteArray(entity);
                if (content != null) {
                    result.setInputStream(new ByteArrayInputStream(content));
                }
            }
            return result;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            result.setStatus(HttpResult.STATUS_EXP);
            result.setException(e);
        } finally {
            org.apache.http.client.utils.HttpClientUtils.closeQuietly(response);
            org.apache.http.client.utils.HttpClientUtils.closeQuietly(httpclient);
        }
        return result;
    }

    public static byte[] getByte(String url) {
        HttpParam param = HttpParam.builder()
                .setUrl(url)
                .build();
        return getByte(param);
    }

    public static byte[] getByte(HttpParam param) {
        CloseableHttpClient httpclient = null;
        CloseableHttpResponse response = null;
        try {
            HttpGet httpGet = new HttpGet(param.getUrl());
            // 超时设置
            RequestConfig requestConfig = RequestConfig.custom()
                    .setSocketTimeout(param.getTimeout())
                    .setConnectTimeout(param.getTimeout())
                    .setConnectionRequestTimeout(param.getTimeout()).build();
            httpGet.setConfig(requestConfig);
            // head设置
            if (ObjUtils.isNotEmpty(param.getHeaderMap())) {
                for (Map.Entry<String, String> entry : param.getHeaderMap().entrySet()) {
                    httpGet.setHeader(entry.getKey(), entry.getValue());
                }
            }
            // requestId
            String requestId = ObjUtils.ifEmpty(MDC.get(MDC_KEY_REQUESTID), MDC.get(MDC_KEY_REQUEST_ID));
            if (ObjUtils.isNotEmpty(requestId)) {
                httpGet.setHeader(MDC_KEY_REQUESTID, requestId);
            }
            httpclient = createHttpClient(param.getUrl());
            long start = System.currentTimeMillis();
            response = httpclient.execute(httpGet);
            long end = System.currentTimeMillis();
            int status = response.getStatusLine().getStatusCode();
            logger.info("请求url：{}，结果状态：{}，耗时：{}毫秒。", param.getUrl(), status, ((end - start)));
            HttpEntity entity = response.getEntity();
            return EntityUtils.toByteArray(entity);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            org.apache.http.client.utils.HttpClientUtils.closeQuietly(response);
            org.apache.http.client.utils.HttpClientUtils.closeQuietly(httpclient);
        }
        return null;
    }

    public static String buildQueryString(String uri, LinkedHashMap<String, String> paramMap) {
        if (MapUtils.isEmpty(paramMap)) {
            return uri;
        }
        StringBuilder sb = new StringBuilder(uri);
        int index = 1;
        for (Map.Entry<String, String> param : paramMap.entrySet()) {
            if (index == 1 && !uri.contains("?")) {
                sb.append("?");
            } else {
                sb.append("&");
            }
            sb.append(EncodeUtils.urlEncodeComponent(param.getKey()));
            sb.append("=");
            if (param.getValue() != null) {
                sb.append(EncodeUtils.urlEncodeComponent(param.getValue()));
            }
            index++;
        }
        return sb.toString();
    }

    public static String post(String url,
                              Map<String, String> paramMap) {
        HttpParam param = HttpParam.builder()
                .setUrl(url)
                .setParamMap(paramMap)
                .build();
        return post(param);
    }

    public static String post(String url,
                              Map<String, String> paramMap,
                              int timeout) {
        HttpParam param = HttpParam.builder()
                .setUrl(url)
                .setParamMap(paramMap)
                .setTimeout(timeout)
                .build();
        return post(param);
    }

    public static String post(String url,
                              Map<String, String> paramMap,
                              Map<String, String> headMap) {
        HttpParam param = HttpParam.builder()
                .setUrl(url)
                .setParamMap(paramMap)
                .setHeaderMap(headMap)
                .build();
        return post(param);
    }

    public static String post(String url,
                              Map<String, String> paramMap,
                              Map<String, String> headMap,
                              int timeout) {
        HttpParam param = HttpParam.builder()
                .setUrl(url)
                .setParamMap(paramMap)
                .setTimeout(timeout)
                .setHeaderMap(headMap)
                .build();
        return post(param);
    }

    public static String post(String url,
                              Map<String, String> paramMap,
                              int timeout,
                              String charset,
                              Map<String, String> headMap) {
        HttpParam param = HttpParam.builder()
                .setUrl(url)
                .setParamMap(paramMap)
                .setTimeout(timeout)
                .setCharset(charset)
                .setHeaderMap(headMap)
                .build();
        return post(param);
    }

    public static String post(HttpParam param) {
        CloseableHttpClient httpclient = null;
        CloseableHttpResponse response = null;
        try {
            HttpPost httpPost = new HttpPost(param.getUrl());
            // 超时设置
            RequestConfig requestConfig = RequestConfig.custom()
                    .setSocketTimeout(param.getTimeout())
                    .setConnectTimeout(param.getTimeout())
                    .setConnectionRequestTimeout(param.getTimeout()).build();
            httpPost.setConfig(requestConfig);
            // head设置
            if (ObjUtils.isNotEmpty(param.getHeaderMap())) {
                for (Map.Entry<String, String> entry : param.getHeaderMap().entrySet()) {
                    httpPost.setHeader(entry.getKey(), entry.getValue());
                }
            }
            // requestId
            String requestId = ObjUtils.ifEmpty(MDC.get(MDC_KEY_REQUESTID), MDC.get(MDC_KEY_REQUEST_ID));
            if (ObjUtils.isNotEmpty(requestId)) {
                httpPost.setHeader(MDC_KEY_REQUESTID, requestId);
            }
            // 参数设置
            if (ObjUtils.isNotEmpty(param.getParamMap())) {
                List<NameValuePair> nvps = new ArrayList<>();
                for (Map.Entry<String, String> entry : param.getParamMap().entrySet()) {
                    nvps.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
                }
                httpPost.setEntity(new UrlEncodedFormEntity(nvps, param.getCharset()));
            }
            httpclient = createHttpClient(param.getUrl());
            long start = System.currentTimeMillis();
            response = httpclient.execute(httpPost);
            long end = System.currentTimeMillis();
            int status = response.getStatusLine().getStatusCode();
            logger.info("请求url：{}，结果状态：{}，耗时：{}毫秒。", param.getUrl(), status, (end - start));
            if (status >= 200 && status < 300) {
                HttpEntity entity = response.getEntity();
                return entity != null ? EntityUtils.toString(entity, param.getCharset()) : null;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            org.apache.http.client.utils.HttpClientUtils.closeQuietly(response);
            org.apache.http.client.utils.HttpClientUtils.closeQuietly(httpclient);
        }
        return null;
    }

    public static HttpResult postHttpResult(String url, Map<String, String> paramMap) {
        HttpParam param = HttpParam.builder()
                .setUrl(url)
                .setParamMap(paramMap)
                .build();
        return postHttpResult(param);
    }

    public static HttpResult postHttpResult(String url,
                                            Map<String, String> paramMap,
                                            Map<String, String> headMap) {
        HttpParam param = HttpParam.builder()
                .setUrl(url)
                .setParamMap(paramMap)
                .setHeaderMap(headMap)
                .build();
        return postHttpResult(param);
    }

    public static HttpResult postHttpResult(String url,
                                            Map<String, String> paramMap,
                                            int timeout) {
        HttpParam param = HttpParam.builder()
                .setUrl(url)
                .setParamMap(paramMap)
                .setTimeout(timeout)
                .build();
        return postHttpResult(param);
    }

    public static HttpResult postHttpResult(String url,
                                            Map<String, String> paramMap,
                                            Map<String, String> headMap,
                                            int timeout) {
        HttpParam param = HttpParam.builder()
                .setUrl(url)
                .setParamMap(paramMap)
                .setTimeout(timeout)
                .setHeaderMap(headMap)
                .build();
        return postHttpResult(param);
    }

    public static HttpResult postHttpResult(String url,
                                            Map<String, String> paramMap,
                                            int timeout,
                                            String charset,
                                            Map<String, String> headMap) {
        HttpParam param = HttpParam.builder()
                .setUrl(url)
                .setParamMap(paramMap)
                .setTimeout(timeout)
                .setCharset(charset)
                .setHeaderMap(headMap)
                .build();
        return postHttpResult(param);
    }

    public static HttpResult postHttpResult(HttpParam param) {
        CloseableHttpClient httpclient = null;
        CloseableHttpResponse response = null;
        HttpResult result = new HttpResult();
        try {
            HttpPost httpPost = new HttpPost(param.getUrl());
            // 超时设置
            RequestConfig requestConfig = RequestConfig.custom()
                    .setSocketTimeout(param.getTimeout())
                    .setConnectTimeout(param.getTimeout())
                    .setConnectionRequestTimeout(param.getTimeout()).build();
            httpPost.setConfig(requestConfig);
            // head设置
            if (ObjUtils.isNotEmpty(param.getHeaderMap())) {
                for (Map.Entry<String, String> entry : param.getHeaderMap().entrySet()) {
                    httpPost.setHeader(entry.getKey(), entry.getValue());
                }
            }
            // requestId
            String requestId = ObjUtils.ifEmpty(MDC.get(MDC_KEY_REQUESTID), MDC.get(MDC_KEY_REQUEST_ID));
            if (ObjUtils.isNotEmpty(requestId)) {
                httpPost.setHeader(MDC_KEY_REQUESTID, requestId);
            }
            // 参数设置
            if (ObjUtils.isNotEmpty(param.getParamMap())) {
                List<NameValuePair> nvps = new ArrayList<>();
                for (Map.Entry<String, String> entry : param.getParamMap().entrySet()) {
                    nvps.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
                }
                httpPost.setEntity(new UrlEncodedFormEntity(nvps, param.getCharset()));
            }
            httpclient = createHttpClient(param.getUrl());
            long start = System.currentTimeMillis();
            response = httpclient.execute(httpPost);
            long end = System.currentTimeMillis();
            int status = response.getStatusLine().getStatusCode();
            logger.info("请求url：{}，结果状态：{}，耗时：{}毫秒。", param.getUrl(), status, (end - start));
            result.setStatus(status);
            result.setCost(end - start);
            result.setHeaders(response.getAllHeaders());
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                byte[] content = EntityUtils.toByteArray(entity);
                if (content != null) {
                    result.setInputStream(new ByteArrayInputStream(content));
                }
            }
            return result;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            result.setStatus(HttpResult.STATUS_EXP);
            result.setException(e);
        } finally {
            org.apache.http.client.utils.HttpClientUtils.closeQuietly(response);
            org.apache.http.client.utils.HttpClientUtils.closeQuietly(httpclient);
        }
        return result;
    }

    /**
     * 包含文件类型的POST提交,参数支持File,InputStream,byte[]和String
     * 第三个参数支持远程文件，先将远程文件下载
     *
     * @param url
     * @param paramMap
     * @param urlParamMap
     * @return
     */
    public static String postMultipartForm(String url,
                                           Map<String, Object> paramMap,
                                           Map<String, String> urlParamMap,
                                           Map<String, String> fileNameMap) {
        HttpParam param = HttpParam.builder()
                .setUrl(url)
                .setFileMap(paramMap)
                .setUrlParamMap(urlParamMap)
                .setFileNameMap(fileNameMap)
                .build();
        return postMultipartForm(param);
    }

    /**
     * 包含文件类型的POST提交,参数支持File,InputStream,byte[]和String
     * 第三个参数支持远程文件，先将远程文件下载
     *
     * @param param
     * @return
     */
    public static String postMultipartForm(HttpParam param) {
        CloseableHttpClient httpclient = null;
        CloseableHttpResponse response = null;
        try {
            HttpPost httpPost = new HttpPost(param.getUrl());
            // 超时设置
            RequestConfig requestConfig = RequestConfig.custom()
                    .setSocketTimeout(param.getTimeout())
                    .setConnectTimeout(param.getTimeout())
                    .setConnectionRequestTimeout(param.getTimeout()).build();
            httpPost.setConfig(requestConfig);
            // head设置
            if (ObjUtils.isNotEmpty(param.getHeaderMap())) {
                for (Map.Entry<String, String> entry : param.getHeaderMap().entrySet()) {
                    httpPost.setHeader(entry.getKey(), entry.getValue());
                }
            }
            // requestId
            String requestId = ObjUtils.ifEmpty(MDC.get(MDC_KEY_REQUESTID), MDC.get(MDC_KEY_REQUEST_ID));
            if (ObjUtils.isNotEmpty(requestId)) {
                httpPost.setHeader(MDC_KEY_REQUESTID, requestId);
            }
            // 参数设置
            MultipartEntityBuilder builder = MultipartEntityBuilder
                    .create()
                    .setCharset(UTF_8)
                    .setMode(HttpMultipartMode.RFC6532);
            if (ObjUtils.isNotEmpty(param.getFileMap())) {
                for (Map.Entry<String, Object> entry : param.getFileMap().entrySet()) {
                    Object value = entry.getValue();
                    if (value instanceof File) {
                        builder.addBinaryBody(entry.getKey(), (File) value);
                    } else if (value instanceof InputStream) {
                        String fileName = null;
                        if (param.getFileNameMap() != null) {
                            fileName = param.getFileNameMap().get(entry.getKey());
                        }
                        builder.addBinaryBody(entry.getKey(), (InputStream) value, ContentType.DEFAULT_BINARY, fileName);
                    } else if (value instanceof byte[]) {
                        String fileName = null;
                        if (param.getFileNameMap() != null) {
                            fileName = param.getFileNameMap().get(entry.getKey());
                        }
                        builder.addBinaryBody(entry.getKey(), (byte[]) value, ContentType.DEFAULT_BINARY, fileName);
                    } else {
                        builder.addTextBody(entry.getKey(), ObjUtils.toString(entry.getValue()), TEXT_PLAIN_UTF8);
                    }
                }
            }
            if (ObjUtils.isNotEmpty(param.getUrlParamMap())) {
                for (Map.Entry<String, String> entry : param.getUrlParamMap().entrySet()) {
                    String fileName = null;
                    if (param.getFileNameMap() != null) {
                        fileName = param.getFileNameMap().get(entry.getKey());
                    }
                    builder.addBinaryBody(entry.getKey(), getByte(entry.getValue()), ContentType.DEFAULT_BINARY, fileName);
                }
            }
            httpPost.setEntity(builder.build());
            httpclient = createHttpClient(param.getUrl());
            long start = System.currentTimeMillis();
            response = httpclient.execute(httpPost);
            long end = System.currentTimeMillis();
            int status = response.getStatusLine().getStatusCode();
            logger.info("请求url：{}，结果状态：{}，耗时：{}毫秒。", param.getUrl(), status, (end - start));
            if (status >= 200 && status < 300) {
                HttpEntity entity = response.getEntity();
                return entity != null ? EntityUtils.toString(entity, param.getCharset()) : null;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            org.apache.http.client.utils.HttpClientUtils.closeQuietly(response);
            org.apache.http.client.utils.HttpClientUtils.closeQuietly(httpclient);
        }
        return null;
    }

    /**
     * 包含文件类型的POST提交,参数支持File,InputStream,byte[]和String
     * 第三个参数支持远程文件，先将远程文件下载
     *
     * @param url
     * @param paramMap
     * @param urlParamMap
     * @return
     */
    public static HttpResult postMultipartFormHttpResult(String url,
                                                         Map<String, Object> paramMap,
                                                         Map<String, String> urlParamMap,
                                                         Map<String, String> fileNameMap) {
        HttpParam param = HttpParam.builder()
                .setUrl(url)
                .setFileMap(paramMap)
                .setUrlParamMap(urlParamMap)
                .setFileNameMap(fileNameMap)
                .build();
        return postMultipartFormHttpResult(param);
    }

    /**
     * 包含文件类型的POST提交,参数支持File,InputStream,byte[]和String
     * 第三个参数支持远程文件，先将远程文件下载
     *
     * @param param
     * @return
     */
    public static HttpResult postMultipartFormHttpResult(HttpParam param) {
        CloseableHttpClient httpclient = null;
        CloseableHttpResponse response = null;
        HttpResult result = new HttpResult();
        try {
            HttpPost httpPost = new HttpPost(param.getUrl());
            // 超时设置
            RequestConfig requestConfig = RequestConfig.custom()
                    .setSocketTimeout(param.getTimeout())
                    .setConnectTimeout(param.getTimeout())
                    .setConnectionRequestTimeout(param.getTimeout()).build();
            httpPost.setConfig(requestConfig);
            // head设置
            if (ObjUtils.isNotEmpty(param.getHeaderMap())) {
                for (Map.Entry<String, String> entry : param.getHeaderMap().entrySet()) {
                    httpPost.setHeader(entry.getKey(), entry.getValue());
                }
            }
            // requestId
            String requestId = ObjUtils.ifEmpty(MDC.get(MDC_KEY_REQUESTID), MDC.get(MDC_KEY_REQUEST_ID));
            if (ObjUtils.isNotEmpty(requestId)) {
                httpPost.setHeader(MDC_KEY_REQUESTID, requestId);
            }
            // 参数设置
            MultipartEntityBuilder builder = MultipartEntityBuilder
                    .create()
                    .setCharset(UTF_8)
                    .setMode(HttpMultipartMode.RFC6532);
            if (ObjUtils.isNotEmpty(param.getFileMap())) {
                for (Map.Entry<String, Object> entry : param.getFileMap().entrySet()) {
                    Object value = entry.getValue();
                    if (value instanceof File) {
                        builder.addBinaryBody(entry.getKey(), (File) value);
                    } else if (value instanceof InputStream) {
                        String fileName = null;
                        if (param.getFileNameMap() != null) {
                            fileName = param.getFileNameMap().get(entry.getKey());
                        }
                        builder.addBinaryBody(entry.getKey(), (InputStream) value, ContentType.DEFAULT_BINARY, fileName);
                    } else if (value instanceof byte[]) {
                        String fileName = null;
                        if (param.getFileNameMap() != null) {
                            fileName = param.getFileNameMap().get(entry.getKey());
                        }
                        builder.addBinaryBody(entry.getKey(), (byte[]) value, ContentType.DEFAULT_BINARY, fileName);
                    } else {
                        builder.addTextBody(entry.getKey(), ObjUtils.toString(entry.getValue()), TEXT_PLAIN_UTF8);
                    }
                }
            }
            if (ObjUtils.isNotEmpty(param.getUrlParamMap())) {
                for (Map.Entry<String, String> entry : param.getUrlParamMap().entrySet()) {
                    String fileName = null;
                    if (param.getFileNameMap() != null) {
                        fileName = param.getFileNameMap().get(entry.getKey());
                    }
                    builder.addBinaryBody(entry.getKey(), getByte(entry.getValue()), ContentType.DEFAULT_BINARY, fileName);
                }
            }
            httpPost.setEntity(builder.build());
            httpclient = createHttpClient(param.getUrl());
            long start = System.currentTimeMillis();
            response = httpclient.execute(httpPost);
            long end = System.currentTimeMillis();
            int status = response.getStatusLine().getStatusCode();
            logger.info("请求url：{}，结果状态：{}，耗时：{}毫秒。", param.getUrl(), status, (end - start));
            result.setStatus(status);
            result.setCost(end - start);
            result.setHeaders(response.getAllHeaders());
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                byte[] content = EntityUtils.toByteArray(entity);
                if (content != null) {
                    result.setInputStream(new ByteArrayInputStream(content));
                }
            }
            return result;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            result.setStatus(HttpResult.STATUS_EXP);
            result.setException(e);
        } finally {
            org.apache.http.client.utils.HttpClientUtils.closeQuietly(response);
            org.apache.http.client.utils.HttpClientUtils.closeQuietly(httpclient);
        }
        return result;
    }

    public static String postBody(String url, String requestBody) {
        HttpParam param = HttpParam.builder()
                .setUrl(url)
                .setRequestBody(requestBody)
                .build();
        return postBody(param);
    }

    public static String postBody(String url,
                                  String requestBody,
                                  Map<String, String> headMap) {
        HttpParam param = HttpParam.builder()
                .setUrl(url)
                .setRequestBody(requestBody)
                .setHeaderMap(headMap)
                .build();
        return postBody(param);
    }

    public static String postBody(String url,
                                  String requestBody,
                                  int timeout,
                                  String charset,
                                  Map<String, String> headMap) {
        HttpParam param = HttpParam.builder()
                .setUrl(url)
                .setRequestBody(requestBody)
                .setTimeout(timeout)
                .setCharset(charset)
                .setHeaderMap(headMap)
                .build();
        return postBody(param);
    }

    public static String postBody(HttpParam param) {
        CloseableHttpClient httpclient = null;
        CloseableHttpResponse response = null;
        try {
            HttpPost httpPost = new HttpPost(param.getUrl());
            // 超时设置
            RequestConfig requestConfig = RequestConfig.custom()
                    .setSocketTimeout(param.getTimeout())
                    .setConnectTimeout(param.getTimeout())
                    .setConnectionRequestTimeout(param.getTimeout()).build();
            httpPost.setConfig(requestConfig);
            // head设置
            httpPost.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
            if (ObjUtils.isNotEmpty(param.getHeaderMap())) {
                for (Map.Entry<String, String> entry : param.getHeaderMap().entrySet()) {
                    httpPost.setHeader(entry.getKey(), entry.getValue());
                }
            }
            // requestId
            String requestId = ObjUtils.ifEmpty(MDC.get(MDC_KEY_REQUESTID), MDC.get(MDC_KEY_REQUEST_ID));
            if (ObjUtils.isNotEmpty(requestId)) {
                httpPost.setHeader(MDC_KEY_REQUESTID, requestId);
            }
            // 请求体设置
            if (StringUtils.isNotEmpty(param.getRequestBody())) {
                httpPost.setEntity(new StringEntity(param.getRequestBody(), param.getCharset()));
            }
            httpclient = createHttpClient(param.getUrl());
            long start = System.currentTimeMillis();
            response = httpclient.execute(httpPost);
            long end = System.currentTimeMillis();
            int status = response.getStatusLine().getStatusCode();
            logger.info("请求url：{}，结果状态：{}，耗时：{}毫秒。", param.getUrl(), status, ((end - start)));
            if (status >= 200 && status < 300) {
                HttpEntity entity = response.getEntity();
                return entity != null ? EntityUtils.toString(entity, param.getCharset()) : null;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            org.apache.http.client.utils.HttpClientUtils.closeQuietly(response);
            org.apache.http.client.utils.HttpClientUtils.closeQuietly(httpclient);
        }
        return null;
    }

    public static HttpResult postBodyHttpResult(String url, String requestBody) {
        HttpParam param = HttpParam.builder()
                .setUrl(url)
                .setRequestBody(requestBody)
                .build();
        return postBodyHttpResult(param);
    }

    public static HttpResult postBodyHttpResult(String url,
                                                String requestBody,
                                                Map<String, String> headMap) {
        HttpParam param = HttpParam.builder()
                .setUrl(url)
                .setRequestBody(requestBody)
                .setHeaderMap(headMap)
                .build();
        return postBodyHttpResult(param);
    }

    public static HttpResult postBodyHttpResult(String url,
                                                String requestBody,
                                                int timeout,
                                                String charset,
                                                Map<String, String> headMap) {
        HttpParam param = HttpParam.builder()
                .setUrl(url)
                .setRequestBody(requestBody)
                .setTimeout(timeout)
                .setCharset(charset)
                .setHeaderMap(headMap)
                .build();
        return postBodyHttpResult(param);
    }

    public static HttpResult postBodyHttpResult(HttpParam param) {
        CloseableHttpClient httpclient = null;
        CloseableHttpResponse response = null;
        HttpResult result = new HttpResult();
        try {
            HttpPost httpPost = new HttpPost(param.getUrl());
            // 超时设置
            RequestConfig requestConfig = RequestConfig.custom()
                    .setSocketTimeout(param.getTimeout())
                    .setConnectTimeout(param.getTimeout())
                    .setConnectionRequestTimeout(param.getTimeout()).build();
            httpPost.setConfig(requestConfig);
            // head设置
            httpPost.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
            if (ObjUtils.isNotEmpty(param.getHeaderMap())) {
                for (Map.Entry<String, String> entry : param.getHeaderMap().entrySet()) {
                    httpPost.setHeader(entry.getKey(), entry.getValue());
                }
            }
            // requestId
            String requestId = ObjUtils.ifEmpty(MDC.get(MDC_KEY_REQUESTID), MDC.get(MDC_KEY_REQUEST_ID));
            if (ObjUtils.isNotEmpty(requestId)) {
                httpPost.setHeader(MDC_KEY_REQUESTID, requestId);
            }
            // 请求体设置
            if (ObjUtils.isNotEmpty(param.getRequestBody())) {
                httpPost.setEntity(new StringEntity(param.getRequestBody(), param.getCharset()));
            }
            httpclient = createHttpClient(param.getUrl());
            long start = System.currentTimeMillis();
            response = httpclient.execute(httpPost);
            long end = System.currentTimeMillis();
            int status = response.getStatusLine().getStatusCode();
            logger.info("请求url：{}，结果状态：{}，耗时：{}毫秒。", param.getUrl(), status, ((end - start)));
            result.setStatus(status);
            result.setCost(end - start);
            result.setHeaders(response.getAllHeaders());
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                byte[] content = EntityUtils.toByteArray(entity);
                if (content != null) {
                    result.setInputStream(new ByteArrayInputStream(content));
                }
            }
            return result;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            result.setStatus(HttpResult.STATUS_EXP);
            result.setException(e);
        } finally {
            org.apache.http.client.utils.HttpClientUtils.closeQuietly(response);
            org.apache.http.client.utils.HttpClientUtils.closeQuietly(httpclient);
        }
        return result;
    }

    public static String delete(String url) {
        HttpParam param = HttpParam.builder()
                .setUrl(url)
                .build();
        return delete(param);
    }

    public static String delete(String url, int timeout) {
        HttpParam param = HttpParam.builder()
                .setUrl(url)
                .setTimeout(timeout)
                .build();
        return delete(param);
    }

    public static String delete(String url, Map<String, String> headMap) {
        HttpParam param = HttpParam.builder()
                .setUrl(url)
                .setHeaderMap(headMap)
                .build();
        return delete(param);
    }

    public static String delete(String url,
                                int timeout,
                                String charset,
                                Map<String, String> headMap) {
        HttpParam param = HttpParam.builder()
                .setUrl(url)
                .setTimeout(timeout)
                .setCharset(charset)
                .setHeaderMap(headMap)
                .build();
        return delete(param);
    }

    public static String delete(HttpParam param) {
        CloseableHttpClient httpclient = null;
        CloseableHttpResponse response = null;
        try {
            HttpDelete httpDelete = new HttpDelete(param.getUrl());
            // 超时设置
            RequestConfig requestConfig = RequestConfig.custom()
                    .setSocketTimeout(param.getTimeout())
                    .setConnectTimeout(param.getTimeout())
                    .setConnectionRequestTimeout(param.getTimeout())
                    .build();
            httpDelete.setConfig(requestConfig);
            if (ObjUtils.isNotEmpty(param.getHeaderMap())) {
                for (Map.Entry<String, String> entry : param.getHeaderMap().entrySet()) {
                    httpDelete.setHeader(entry.getKey(), entry.getValue());
                }
            }
            // requestId
            String requestId = ObjUtils.ifEmpty(MDC.get(MDC_KEY_REQUESTID), MDC.get(MDC_KEY_REQUEST_ID));
            if (ObjUtils.isNotEmpty(requestId)) {
                httpDelete.setHeader(MDC_KEY_REQUESTID, requestId);
            }
            httpclient = createHttpClient(param.getUrl());
            long start = System.currentTimeMillis();
            response = httpclient.execute(httpDelete);
            long end = System.currentTimeMillis();
            int status = response.getStatusLine().getStatusCode();
            logger.info("请求url：{}，结果状态：{}，耗时：{}毫秒。", param.getUrl(), status, ((end - start)));
            if (status >= 200 && status < 300) {
                HttpEntity entity = response.getEntity();
                return entity != null ? EntityUtils.toString(entity, param.getCharset()) : null;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            org.apache.http.client.utils.HttpClientUtils.closeQuietly(response);
            org.apache.http.client.utils.HttpClientUtils.closeQuietly(httpclient);
        }
        return null;
    }

    public static HttpResult deleteHttpResult(String url) {
        HttpParam param = HttpParam.builder()
                .setUrl(url)
                .build();
        return deleteHttpResult(param);
    }

    public static HttpResult deleteHttpResult(String url, int timeout) {
        return getHttpResult(url, timeout, null);
    }

    public static HttpResult deleteHttpResult(String url, Map<String, String> headMap) {
        HttpParam param = HttpParam.builder()
                .setUrl(url)
                .setHeaderMap(headMap)
                .build();
        return deleteHttpResult(param);
    }

    public static HttpResult deleteHttpResult(String url,
                                              int timeout,
                                              Map<String, String> headMap) {
        HttpParam param = HttpParam.builder()
                .setUrl(url)
                .setTimeout(timeout)
                .setHeaderMap(headMap)
                .build();
        return deleteHttpResult(param);
    }

    public static HttpResult deleteHttpResult(String url,
                                              int timeout,
                                              String charset,
                                              Map<String, String> headMap) {
        HttpParam param = HttpParam.builder()
                .setUrl(url)
                .setTimeout(timeout)
                .setCharset(charset)
                .setHeaderMap(headMap)
                .build();
        return deleteHttpResult(param);
    }

    public static HttpResult deleteHttpResult(HttpParam param) {
        CloseableHttpClient httpclient = null;
        CloseableHttpResponse response = null;
        HttpResult result = new HttpResult();
        try {
            HttpDelete httpDelete = new HttpDelete(param.getUrl());
            // 超时设置
            RequestConfig requestConfig = RequestConfig.custom()
                    .setSocketTimeout(param.getTimeout())
                    .setConnectTimeout(param.getTimeout())
                    .setConnectionRequestTimeout(param.getTimeout()).build();
            httpDelete.setConfig(requestConfig);
            // head设置
            if (ObjUtils.isNotEmpty(param.getHeaderMap())) {
                for (Map.Entry<String, String> entry : param.getHeaderMap().entrySet()) {
                    httpDelete.setHeader(entry.getKey(), entry.getValue());
                }
            }
            // requestId
            String requestId = ObjUtils.ifEmpty(MDC.get(MDC_KEY_REQUESTID), MDC.get(MDC_KEY_REQUEST_ID));
            if (ObjUtils.isNotEmpty(requestId)) {
                httpDelete.setHeader(MDC_KEY_REQUESTID, requestId);
            }
            httpclient = createHttpClient(param.getUrl());
            long start = System.currentTimeMillis();
            response = httpclient.execute(httpDelete);
            long end = System.currentTimeMillis();
            int status = response.getStatusLine().getStatusCode();
            logger.info("请求url：{}，结果状态：{}，耗时：{}毫秒。", param.getUrl(), status, ((end - start)));
            result.setStatus(status);
            result.setCost(end - start);
            result.setHeaders(response.getAllHeaders());
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                byte[] content = EntityUtils.toByteArray(entity);
                if (content != null) {
                    result.setInputStream(new ByteArrayInputStream(content));
                }
            }
            return result;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            result.setStatus(HttpResult.STATUS_EXP);
            result.setException(e);
        } finally {
            org.apache.http.client.utils.HttpClientUtils.closeQuietly(response);
            org.apache.http.client.utils.HttpClientUtils.closeQuietly(httpclient);
        }
        return result;
    }

    public static void download(String url, String filePath) {
        HttpParam param = HttpParam.builder()
                .setUrl(url)
                .setFilePath(filePath)
                .build();
        download(param);
    }

    public static void download(String url,
                                String filePath,
                                Map<String, String> headMap) {
        HttpParam param = HttpParam.builder()
                .setUrl(url)
                .setFilePath(filePath)
                .setHeaderMap(headMap)
                .build();
        download(param);
    }


    public static void download(HttpParam param) {
        CloseableHttpClient httpclient = null;
        CloseableHttpResponse response = null;
        try {
            HttpGet httpGet = new HttpGet(param.getUrl());
            // head设置
            if (ObjUtils.isNotEmpty(param.getHeaderMap())) {
                for (Map.Entry<String, String> entry : param.getHeaderMap().entrySet()) {
                    httpGet.setHeader(entry.getKey(), entry.getValue());
                }
            }
            // requestId
            String requestId = ObjUtils.ifEmpty(MDC.get(MDC_KEY_REQUESTID), MDC.get(MDC_KEY_REQUEST_ID));
            if (ObjUtils.isNotEmpty(requestId)) {
                httpGet.setHeader(MDC_KEY_REQUESTID, requestId);
            }
            httpclient = createHttpClient(param.getUrl());
            long start = System.currentTimeMillis();
            response = httpclient.execute(httpGet);
            long end = System.currentTimeMillis();
            int status = response.getStatusLine().getStatusCode();
            logger.info("请求url：{}，结果状态：{}，耗时：{}毫秒。", param.getUrl(), status, ((end - start)));
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                File targetFile = new File(param.getFilePath());
                File tmpFile = new File(targetFile.getAbsolutePath() + "." + RandomUtils.bsonId());
                FileUtils.copyInputStreamToFile(entity.getContent(), tmpFile);
                tmpFile.renameTo(targetFile);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            org.apache.http.client.utils.HttpClientUtils.closeQuietly(response);
            org.apache.http.client.utils.HttpClientUtils.closeQuietly(httpclient);
        }
    }

    public static String getRedirectUrl(String url, Map<String, String> headMap) {
        HttpParam param = HttpParam.builder()
                .setUrl(url)
                .setHeaderMap(headMap)
                .build();
        return getRedirectUrl(param);
    }

    public static String getRedirectUrl(HttpParam param) {
        CloseableHttpClient httpclient = null;
        CloseableHttpResponse response = null;
        try {
            HttpGet httpGet = new HttpGet(param.getUrl());
            // 超时设置
            RequestConfig requestConfig = RequestConfig.custom()
                    .setSocketTimeout(param.getTimeout())
                    .setConnectTimeout(param.getTimeout())
                    .setConnectionRequestTimeout(param.getTimeout()).build();
            httpGet.setConfig(requestConfig);
            // head设置
            if (ObjUtils.isNotEmpty(param.getHeaderMap())) {
                for (Map.Entry<String, String> entry : param.getHeaderMap().entrySet()) {
                    httpGet.setHeader(entry.getKey(), entry.getValue());
                }
            }
            // requestId
            String requestId = ObjUtils.ifEmpty(MDC.get(MDC_KEY_REQUESTID), MDC.get(MDC_KEY_REQUEST_ID));
            if (ObjUtils.isNotEmpty(requestId)) {
                httpGet.setHeader(MDC_KEY_REQUESTID, requestId);
            }
            // 自定义重定向，不自动处理
            CustomRedirectStrategy redirectStrategy = new CustomRedirectStrategy();
            httpclient = createHttpClient(param.getUrl(), redirectStrategy);
            HttpClientContext context = HttpClientContext.create();
            long start = System.currentTimeMillis();
            response = httpclient.execute(httpGet, context);
            long end = System.currentTimeMillis();
            int status = response.getStatusLine().getStatusCode();
            logger.info("请求url：{}，结果状态：{}，耗时：{}毫秒。", param.getUrl(), status, ((end - start)));
            HttpEntity entity = response.getEntity();
            logger.info(EntityUtils.toString(entity));
            // 判断是否重定向
            boolean isRedirected = redirectStrategy.isRedirectedDefault(httpGet, response, context);
            if (!isRedirected) {
                return null;
            }
            return redirectStrategy.getRedirectLocation(httpGet, response, context);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            org.apache.http.client.utils.HttpClientUtils.closeQuietly(response);
            org.apache.http.client.utils.HttpClientUtils.closeQuietly(httpclient);
        }
        return null;
    }

    public static void main(String[] args) throws Exception {

    }

}
