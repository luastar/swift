package com.luastar.swift.http.route;

import com.luastar.swift.base.utils.ClassLoaderUtils;
import com.luastar.swift.base.utils.EncodeUtils;
import com.luastar.swift.http.server.HttpRequest;
import com.luastar.swift.http.server.HttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import org.springframework.core.io.Resource;

import java.io.File;
import java.util.regex.Pattern;

import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;

/**
 * 默认请求处理器，请求静态资源
 */
public class DefaultHttpRequestHandler implements HttpRequestHandler {

    private static final Pattern INSECURE_URI = Pattern.compile(".*[<>&\"].*");

    @Override
    public void handleRequest(HttpRequest request, HttpResponse response) throws Exception {
        // 静态文件只支持GET方式
        if (!request.getMethod().equalsIgnoreCase(HttpMethod.GET.name())) {
            response.setStatus(NOT_FOUND);
            response.setResult("request not found.");
            return;
        }
        String path = sanitizeUri(request.getUri());
        if (path == null) {
            response.setStatus(NOT_FOUND);
            response.setResult("request not found.");
            return;
        }
        // 获取资源
        Resource resource = ClassLoaderUtils.getDefaultResourceLoader().getResource("classpath:" + path);
        File file = resource.getFile();
        if (file.isHidden() || !file.exists() || !file.isFile()) {
            response.setStatus(NOT_FOUND);
            response.setResult("request not found.");
            return;
        }
        response.setStaticFile(file);
    }

    private static String sanitizeUri(String uri) {
        // Decode the path.
        uri = EncodeUtils.urlDecode(uri);
        if (uri.isEmpty() || uri.charAt(0) != '/') {
            return null;
        }
        // Convert file separators.
        uri = uri.replace('/', File.separatorChar);
        // Simplistic dumb security check.
        if (uri.contains(File.separator + '.')
                || uri.contains('.' + File.separator)
                || uri.charAt(0) == '.'
                || uri.charAt(uri.length() - 1) == '.'
                || INSECURE_URI.matcher(uri).matches()) {
            return null;
        }
        return uri;
    }

}
