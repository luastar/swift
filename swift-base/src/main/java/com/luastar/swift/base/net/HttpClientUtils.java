package com.luastar.swift.base.net;

import com.google.common.collect.Maps;
import com.luastar.swift.base.json.JsonUtils;
import com.luastar.swift.base.utils.ObjUtils;
import com.luastar.swift.base.utils.RandomUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

public class HttpClientUtils {

    private static Logger logger = LoggerFactory.getLogger(HttpClientUtils.class);

    public static final int DEFAULT_TIMEOUT = 6000;
    public static final String DEFAULT_CHARSET = "UTF-8";
    public static final ContentType TEXT_PLAIN_UTF8 = ContentType.create("text/plain", UTF_8);

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
        return get(url, DEFAULT_TIMEOUT, DEFAULT_CHARSET, null);
    }

    public static String get(String url, int timeout) {
        return get(url, timeout, DEFAULT_CHARSET, null);
    }

    public static String get(String url, Map<String, String> headMap) {
        return get(url, DEFAULT_TIMEOUT, DEFAULT_CHARSET, headMap);
    }

    public static String get(String url,
                             int timeout,
                             String charset,
                             Map<String, String> headMap) {
        CloseableHttpClient httpclient = null;
        CloseableHttpResponse response = null;
        try {
            HttpGet httpGet = new HttpGet(url);
            // 超时设置
            RequestConfig requestConfig = RequestConfig.custom()
                    .setSocketTimeout(timeout)
                    .setConnectTimeout(timeout)
                    .setConnectionRequestTimeout(timeout).build();
            httpGet.setConfig(requestConfig);
            // head设置
            if (headMap != null && !headMap.isEmpty()) {
                for (Map.Entry<String, String> entry : headMap.entrySet()) {
                    httpGet.addHeader(entry.getKey(), entry.getValue());
                }
            }
            httpclient = createHttpClient(url);
            long start = System.currentTimeMillis();
            response = httpclient.execute(httpGet);
            long end = System.currentTimeMillis();
            int status = response.getStatusLine().getStatusCode();
            logger.info("请求url：{}，结果状态：{}，耗时：{}毫秒。", url, status, ((end - start)));
            if (status >= 200 && status < 300) {
                HttpEntity entity = response.getEntity();
                return entity != null ? EntityUtils.toString(entity, charset) : null;
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
        return getHttpResult(url, DEFAULT_TIMEOUT);
    }

    public static HttpResult getHttpResult(String url, int timeout) {
        return getHttpResult(url, timeout, null);
    }

    public static HttpResult getHttpResult(String url, Map<String, String> headMap) {
        return getHttpResult(url, DEFAULT_TIMEOUT, headMap);
    }

    public static HttpResult getHttpResult(String url,
                                           int timeout,
                                           Map<String, String> headMap) {
        CloseableHttpClient httpclient = null;
        CloseableHttpResponse response = null;
        HttpResult result = new HttpResult();
        try {
            HttpGet httpGet = new HttpGet(url);
            // 超时设置
            RequestConfig requestConfig = RequestConfig.custom()
                    .setSocketTimeout(timeout)
                    .setConnectTimeout(timeout)
                    .setConnectionRequestTimeout(timeout).build();
            httpGet.setConfig(requestConfig);
            // head设置
            if (headMap != null && !headMap.isEmpty()) {
                for (Map.Entry<String, String> entry : headMap.entrySet()) {
                    httpGet.addHeader(entry.getKey(), entry.getValue());
                }
            }
            httpclient = createHttpClient(url);
            long start = System.currentTimeMillis();
            response = httpclient.execute(httpGet);
            long end = System.currentTimeMillis();
            int status = response.getStatusLine().getStatusCode();
            logger.info("请求url：{}，结果状态：{}，耗时：{}毫秒。", url, status, ((end - start)));
            result.setStatus(status);
            result.setCost(end - start);
            if (status >= 200 && status < 300) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    byte[] content = EntityUtils.toByteArray(entity);
                    if (content != null) {
                        result.setInputStream(new ByteArrayInputStream(content));
                    }
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
        CloseableHttpClient httpclient = null;
        CloseableHttpResponse response = null;
        try {
            HttpGet httpGet = new HttpGet(url);

            httpclient = createHttpClient(url);
            long start = System.currentTimeMillis();
            response = httpclient.execute(httpGet);
            long end = System.currentTimeMillis();
            int status = response.getStatusLine().getStatusCode();
            logger.info("请求url：{}，结果状态：{}，耗时：{}毫秒。", url, status, ((end - start)));
            if (status >= 200 && status < 300) {
                HttpEntity entity = response.getEntity();
                return entity != null ? EntityUtils.toByteArray(entity) : null;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            org.apache.http.client.utils.HttpClientUtils.closeQuietly(response);
            org.apache.http.client.utils.HttpClientUtils.closeQuietly(httpclient);
        }
        return null;
    }

    public static String post(String url, Map<String, String> params) {
        return post(url, params, DEFAULT_TIMEOUT, DEFAULT_CHARSET, null);
    }

    public static String post(String url, Map<String, String> params, int timeout) {
        return post(url, params, timeout, DEFAULT_CHARSET, null);
    }

    public static String post(String url,
                              Map<String, String> params,
                              Map<String, String> headMap) {
        return post(url, params, DEFAULT_TIMEOUT, DEFAULT_CHARSET, headMap);
    }

    public static String post(String url,
                              Map<String, String> params,
                              Map<String, String> headMap,
                              int timeout) {
        return post(url, params, timeout, DEFAULT_CHARSET, headMap);
    }

    public static String post(String url,
                              Map<String, String> params,
                              int timeout,
                              String charset,
                              Map<String, String> headMap) {
        CloseableHttpClient httpclient = null;
        CloseableHttpResponse response = null;
        try {
            HttpPost httpPost = new HttpPost(url);
            // 超时设置
            RequestConfig requestConfig = RequestConfig.custom()
                    .setSocketTimeout(timeout)
                    .setConnectTimeout(timeout)
                    .setConnectionRequestTimeout(timeout).build();
            httpPost.setConfig(requestConfig);
            // head设置
            if (headMap != null && !headMap.isEmpty()) {
                for (Map.Entry<String, String> entry : headMap.entrySet()) {
                    httpPost.addHeader(entry.getKey(), entry.getValue());
                }
            }
            // 参数设置
            if (params != null && !params.isEmpty()) {
                List<NameValuePair> nvps = new ArrayList<NameValuePair>();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    nvps.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
                }
                httpPost.setEntity(new UrlEncodedFormEntity(nvps, charset));
            }
            httpclient = createHttpClient(url);
            long start = System.currentTimeMillis();
            response = httpclient.execute(httpPost);
            long end = System.currentTimeMillis();
            int status = response.getStatusLine().getStatusCode();
            logger.info("请求url：{}，结果状态：{}，耗时：{}毫秒。", url, status, (end - start));
            if (status >= 200 && status < 300) {
                HttpEntity entity = response.getEntity();
                return entity != null ? EntityUtils.toString(entity, charset) : null;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            org.apache.http.client.utils.HttpClientUtils.closeQuietly(response);
            org.apache.http.client.utils.HttpClientUtils.closeQuietly(httpclient);
        }
        return null;
    }

    public static HttpResult postHttpResult(String url, Map<String, String> params) {
        return postHttpResult(url, params, DEFAULT_TIMEOUT);
    }

    public static HttpResult postHttpResult(String url,
                                            Map<String, String> params,
                                            Map<String, String> headMap) {
        return postHttpResult(url, params, DEFAULT_TIMEOUT, DEFAULT_CHARSET, headMap);
    }

    public static HttpResult postHttpResult(String url,
                                            Map<String, String> params,
                                            int timeout) {
        return postHttpResult(url, params, timeout, DEFAULT_CHARSET, null);
    }

    public static HttpResult postHttpResult(String url,
                                            Map<String, String> params,
                                            Map<String, String> headMap,
                                            int timeout) {
        return postHttpResult(url, params, timeout, DEFAULT_CHARSET, headMap);
    }

    public static HttpResult postHttpResult(String url,
                                            Map<String, String> params,
                                            int timeout,
                                            String charset,
                                            Map<String, String> headMap) {
        CloseableHttpClient httpclient = null;
        CloseableHttpResponse response = null;
        HttpResult result = new HttpResult();
        try {
            HttpPost httpPost = new HttpPost(url);
            // 超时设置
            RequestConfig requestConfig = RequestConfig.custom()
                    .setSocketTimeout(timeout)
                    .setConnectTimeout(timeout)
                    .setConnectionRequestTimeout(timeout).build();
            httpPost.setConfig(requestConfig);
            // head设置
            if (headMap != null && !headMap.isEmpty()) {
                for (Map.Entry<String, String> entry : headMap.entrySet()) {
                    httpPost.addHeader(entry.getKey(), entry.getValue());
                }
            }
            // 参数设置
            if (params != null && !params.isEmpty()) {
                List<NameValuePair> nvps = new ArrayList<NameValuePair>();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    nvps.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
                }
                httpPost.setEntity(new UrlEncodedFormEntity(nvps, charset));
            }
            httpclient = createHttpClient(url);
            long start = System.currentTimeMillis();
            response = httpclient.execute(httpPost);
            long end = System.currentTimeMillis();
            int status = response.getStatusLine().getStatusCode();
            logger.info("请求url：{}，结果状态：{}，耗时：{}毫秒。", url, status, (end - start));
            result.setStatus(status);
            result.setCost(end - start);
            if (status >= 200 && status < 300) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    byte[] content = EntityUtils.toByteArray(entity);
                    if (content != null) {
                        result.setInputStream(new ByteArrayInputStream(content));
                    }
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
        CloseableHttpClient httpclient = null;
        CloseableHttpResponse response = null;
        try {
            HttpPost httpPost = new HttpPost(url);
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setCharset(UTF_8);
            // 参数设置
            if (paramMap != null && !paramMap.isEmpty()) {
                for (Map.Entry<String, Object> entry : paramMap.entrySet()) {
                    Object value = entry.getValue();
                    if (value instanceof File) {
                        builder.addBinaryBody(entry.getKey(), (File) value);
                    } else if (value instanceof InputStream) {
                        String fileName = null;
                        if (fileNameMap != null) {
                            fileName = fileNameMap.get(entry.getKey());
                        }
                        builder.addBinaryBody(entry.getKey(), (InputStream) value, ContentType.DEFAULT_BINARY, fileName);
                    } else if (value instanceof byte[]) {
                        String fileName = null;
                        if (fileNameMap != null) {
                            fileName = fileNameMap.get(entry.getKey());
                        }
                        builder.addBinaryBody(entry.getKey(), (byte[]) value, ContentType.DEFAULT_BINARY, fileName);
                    } else {
                        builder.addTextBody(entry.getKey(), ObjUtils.toString(entry.getValue()), TEXT_PLAIN_UTF8);
                    }
                }
            }
            if (urlParamMap != null && !urlParamMap.isEmpty()) {
                for (Map.Entry<String, String> entry : urlParamMap.entrySet()) {
                    String fileName = null;
                    if (fileNameMap != null) {
                        fileName = fileNameMap.get(entry.getKey());
                    }
                    builder.addBinaryBody(entry.getKey(), getByte(entry.getValue()), ContentType.DEFAULT_BINARY, fileName);
                }
            }
            httpPost.setEntity(builder.build());
            httpclient = createHttpClient(url);
            long start = System.currentTimeMillis();
            response = httpclient.execute(httpPost);
            long end = System.currentTimeMillis();
            int status = response.getStatusLine().getStatusCode();
            logger.info("请求url：{}，结果状态：{}，耗时：{}毫秒。", url, status, (end - start));
            if (status >= 200 && status < 300) {
                HttpEntity entity = response.getEntity();
                return entity != null ? EntityUtils.toString(entity, DEFAULT_CHARSET) : null;
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
        CloseableHttpClient httpclient = null;
        CloseableHttpResponse response = null;
        HttpResult result = new HttpResult();
        try {
            HttpPost httpPost = new HttpPost(url);
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setCharset(UTF_8);
            // 参数设置
            if (paramMap != null && !paramMap.isEmpty()) {
                for (Map.Entry<String, Object> entry : paramMap.entrySet()) {
                    Object value = entry.getValue();
                    if (value instanceof File) {
                        builder.addBinaryBody(entry.getKey(), (File) value);
                    } else if (value instanceof InputStream) {
                        String fileName = null;
                        if (fileNameMap != null) {
                            fileName = fileNameMap.get(entry.getKey());
                        }
                        builder.addBinaryBody(entry.getKey(), (InputStream) value, ContentType.DEFAULT_BINARY, fileName);
                    } else if (value instanceof byte[]) {
                        String fileName = null;
                        if (fileNameMap != null) {
                            fileName = fileNameMap.get(entry.getKey());
                        }
                        builder.addBinaryBody(entry.getKey(), (byte[]) value, ContentType.DEFAULT_BINARY, fileName);
                    } else {
                        builder.addTextBody(entry.getKey(), ObjUtils.toString(entry.getValue()), TEXT_PLAIN_UTF8);
                    }
                }
            }
            if (urlParamMap != null && !urlParamMap.isEmpty()) {
                for (Map.Entry<String, String> entry : urlParamMap.entrySet()) {
                    String fileName = null;
                    if (fileNameMap != null) {
                        fileName = fileNameMap.get(entry.getKey());
                    }
                    builder.addBinaryBody(entry.getKey(), getByte(entry.getValue()), ContentType.DEFAULT_BINARY, fileName);
                }
            }
            httpPost.setEntity(builder.build());
            httpclient = createHttpClient(url);
            long start = System.currentTimeMillis();
            response = httpclient.execute(httpPost);
            long end = System.currentTimeMillis();
            int status = response.getStatusLine().getStatusCode();
            logger.info("请求url：{}，结果状态：{}，耗时：{}毫秒。", url, status, (end - start));
            result.setStatus(status);
            result.setCost(end - start);
            if (status >= 200 && status < 300) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    byte[] content = EntityUtils.toByteArray(entity);
                    if (content != null) {
                        result.setInputStream(new ByteArrayInputStream(content));
                    }
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
        return postBody(url, requestBody, null);
    }

    public static String postBody(String url, String requestBody, Map<String, String> headMap) {
        return postBody(url, requestBody, DEFAULT_TIMEOUT, DEFAULT_CHARSET, headMap);
    }

    public static String postBody(String url,
                                  String requestBody,
                                  int timeout,
                                  String charset,
                                  Map<String, String> headMap) {
        CloseableHttpClient httpclient = null;
        CloseableHttpResponse response = null;
        try {
            HttpPost httpPost = new HttpPost(url);
            // 超时设置
            RequestConfig requestConfig = RequestConfig.custom()
                    .setSocketTimeout(timeout)
                    .setConnectTimeout(timeout)
                    .setConnectionRequestTimeout(timeout).build();
            httpPost.setConfig(requestConfig);
            // head设置
            if (headMap != null && !headMap.isEmpty()) {
                for (Map.Entry<String, String> entry : headMap.entrySet()) {
                    httpPost.addHeader(entry.getKey(), entry.getValue());
                }
            }
            // 请求体设置
            if (StringUtils.isNotBlank(requestBody)) {
                httpPost.setEntity(new StringEntity(requestBody, charset));
            }
            httpclient = createHttpClient(url);
            long start = System.currentTimeMillis();
            response = httpclient.execute(httpPost);
            long end = System.currentTimeMillis();
            int status = response.getStatusLine().getStatusCode();
            logger.info("请求url：{}，结果状态：{}，耗时：{}毫秒。", url, status, ((end - start)));
            if (status >= 200 && status < 300) {
                HttpEntity entity = response.getEntity();
                return entity != null ? EntityUtils.toString(entity, charset) : null;
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
        return postBodyHttpResult(url, requestBody, null);
    }

    public static HttpResult postBodyHttpResult(String url, String requestBody, Map<String, String> headMap) {
        return postBodyHttpResult(url, requestBody, DEFAULT_TIMEOUT, DEFAULT_CHARSET, headMap);
    }

    public static HttpResult postBodyHttpResult(String url,
                                                String requestBody,
                                                int timeout,
                                                String charset,
                                                Map<String, String> headMap) {
        CloseableHttpClient httpclient = null;
        CloseableHttpResponse response = null;
        HttpResult result = new HttpResult();
        try {
            HttpPost httpPost = new HttpPost(url);
            // 超时设置
            RequestConfig requestConfig = RequestConfig.custom()
                    .setSocketTimeout(timeout)
                    .setConnectTimeout(timeout)
                    .setConnectionRequestTimeout(timeout).build();
            httpPost.setConfig(requestConfig);
            // head设置
            if (headMap != null && !headMap.isEmpty()) {
                for (Map.Entry<String, String> entry : headMap.entrySet()) {
                    httpPost.addHeader(entry.getKey(), entry.getValue());
                }
            }
            // 请求体设置
            if (StringUtils.isNotBlank(requestBody)) {
                httpPost.setEntity(new StringEntity(requestBody, charset));
            }
            httpclient = createHttpClient(url);
            long start = System.currentTimeMillis();
            response = httpclient.execute(httpPost);
            long end = System.currentTimeMillis();
            int status = response.getStatusLine().getStatusCode();
            logger.info("请求url：{}，结果状态：{}，耗时：{}毫秒。", url, status, ((end - start)));
            result.setStatus(status);
            result.setCost(end - start);
            if (status >= 200 && status < 300) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    byte[] content = EntityUtils.toByteArray(entity);
                    if (content != null) {
                        result.setInputStream(new ByteArrayInputStream(content));
                    }
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
        download(url, filePath, null);
    }

    public static void download(String url,
                                String filePath,
                                Map<String, String> headMap) {
        CloseableHttpClient httpclient = null;
        CloseableHttpResponse response = null;
        try {
            HttpGet httpGet = new HttpGet(url);
            // head设置
            if (headMap != null && !headMap.isEmpty()) {
                for (Map.Entry<String, String> entry : headMap.entrySet()) {
                    httpGet.addHeader(entry.getKey(), entry.getValue());
                }
            }
            httpclient = createHttpClient(url);
            long start = System.currentTimeMillis();
            response = httpclient.execute(httpGet);
            long end = System.currentTimeMillis();
            int status = response.getStatusLine().getStatusCode();
            logger.info("请求url：{}，结果状态：{}，耗时：{}毫秒。", url, status, ((end - start)));
            if (status >= 200 && status < 300) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    File targetFile = new File(filePath);
                    File tmpFile = new File(targetFile.getAbsolutePath() + "." + RandomUtils.uuid2());
                    FileUtils.copyInputStreamToFile(entity.getContent(), tmpFile);
                    tmpFile.renameTo(targetFile);
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            org.apache.http.client.utils.HttpClientUtils.closeQuietly(response);
            org.apache.http.client.utils.HttpClientUtils.closeQuietly(httpclient);
        }
    }

    public static String getRedirectUrl(String url, Map<String, String> headMap) {
        CloseableHttpClient httpclient = null;
        CloseableHttpResponse response = null;
        try {
            HttpGet httpGet = new HttpGet(url);
            // 超时设置
            RequestConfig requestConfig = RequestConfig.custom()
                    .setSocketTimeout(DEFAULT_TIMEOUT)
                    .setConnectTimeout(DEFAULT_TIMEOUT)
                    .setConnectionRequestTimeout(DEFAULT_TIMEOUT).build();
            httpGet.setConfig(requestConfig);
            // head设置
            if (headMap != null && !headMap.isEmpty()) {
                for (Map.Entry<String, String> entry : headMap.entrySet()) {
                    httpGet.addHeader(entry.getKey(), entry.getValue());
                }
            }
            // 自定义重定向，不自动处理
            CustomRedirectStrategy redirectStrategy = new CustomRedirectStrategy();
            httpclient = createHttpClient(url, redirectStrategy);
            HttpClientContext context = HttpClientContext.create();
            long start = System.currentTimeMillis();
            response = httpclient.execute(httpGet, context);
            long end = System.currentTimeMillis();
            int status = response.getStatusLine().getStatusCode();
            logger.info("请求url：{}，结果状态：{}，耗时：{}毫秒。", url, status, ((end - start)));
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
        String url = "http://172.16.0.2:9031/harmony/mail/attachment";
        Map<String, Object> paramMap = Maps.newHashMap();
        paramMap.put("data", "{\"subject\":\"【分贝通】【月结账单】北京分贝金服科技有限公司_20170904-20170902\",\"customerId\":\"spacex\",\"ccList\":[],\"bccList\":[],\"html\":{\"data\":{\"beginDate\":\"2017年09月04日\",\"totalAmount\":\"￥2304.71\",\"period\":\"2017/09/04-2017/09/02\",\"unpaidAmount\":\"￥0.00\",\"endDate\":\"2017年09月02日\",\"author\":\"刘维中\",\"companyName\":\"北京分贝金服科技有限公司\",\"paidAmount\":\"￥0.00\",\"deadLine\":\"2017年09月15日\",\"lastDate\":\"2017年09月15日\"},\"templateId\":\"bill_send.html\"},\"toList\":[\"zhijun.zhang@fenbeitong.com\"],\"serverId\":\"spacex.billing\"}");
        HttpResult result = postMultipartFormHttpResult(url, paramMap, null, null);
        System.out.println(result.getResult());
    }

}
