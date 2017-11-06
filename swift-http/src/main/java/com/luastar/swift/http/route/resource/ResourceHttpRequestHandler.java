package com.luastar.swift.http.route.resource;

import com.luastar.swift.base.utils.EncodeUtils;
import com.luastar.swift.http.constant.HttpConstant;
import com.luastar.swift.http.route.HttpRequestHandler;
import com.luastar.swift.http.server.HttpRequest;
import com.luastar.swift.http.server.HttpResponse;
import io.netty.handler.codec.DateFormatter;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.apache.poi.util.IOUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

import javax.activation.FileTypeMap;
import javax.activation.MimetypesFileTypeMap;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 */
public class ResourceHttpRequestHandler extends WebContentGenerator implements HttpRequestHandler {

    private final List<Resource> locations = new ArrayList<Resource>(4);

    public ResourceHttpRequestHandler() {
        super(METHOD_GET, METHOD_HEAD);
    }

    /**
     * Set a {@code List} of {@code Resource} paths to use as sources
     * for serving static resources.
     */
    public void setLocations(List<Resource> locations) {
        Assert.notNull(locations, "Locations list must not be null");
        this.locations.clear();
        this.locations.addAll(locations);
    }

    public List<Resource> getLocations() {
        return this.locations;
    }

    /**
     * Processes a resource request.
     * <p>Checks for the existence of the requested resource in the configured list of locations.
     * If the resource does not exist, a {@code 404} response will be returned to the client.
     * If the resource exists, the request will be checked for the presence of the
     * {@code Last-Modified} header, and its value will be compared against the last-modified
     * timestamp of the given resource, returning a {@code 304} status code if the
     * {@code Last-Modified} value  is greater. If the resource is newer than the
     * {@code Last-Modified} value, or the header is not present, the content resource
     * of the resource will be written to the response with caching headers
     * set to expire one year in the future.
     */
    @Override
    public void handleRequest(HttpRequest request, HttpResponse response) throws Exception {

        // Supported methods and required session
        checkRequest(request);

        // check whether a matching resource exists
        Resource resource = getResource(request);
        if (resource == null) {
            logger.info("No matching resource found - returning 404");
            response.setStatus(HttpResponseStatus.NOT_FOUND);
            return;
        }
        // header phase
        if (checkNotModified(request, response, resource.lastModified())) {
            logger.info("Resource not modified - returning 304");
            return;
        }
        // Apply cache settings, if any
        prepareResponse(response);

        // Check the media type for the resource
        MediaType mediaType = getMediaType(resource);
        if (mediaType != null) {
            logger.debug("Determined media type '" + mediaType + "' for " + resource);
            response.setHeader(HttpHeaderNames.CONTENT_TYPE.toString(), mediaType.toString());
        } else {
            logger.debug("No media type found for " + resource + " - not sending a content-type header");
        }
        // Content phase
        if (METHOD_HEAD.equals(request.getMethod())) {
            logger.debug("HEAD request - skipping content");
            return;
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        IOUtils.copy(resource.getInputStream(), outputStream);
        response.setOutputStream(outputStream);
    }

    protected Resource getResource(HttpRequest request) {
        String path = (String) request.getAttribute(HttpConstant.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        if (path == null) {
            throw new IllegalStateException("Required request attribute '" + HttpConstant.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE + "' is not set");
        }
        path = processPath(path);
        if (!StringUtils.hasText(path) || isInvalidPath(path)) {
            logger.info("Ignoring invalid resource path [" + path + "]");
            return null;
        }
        if (path.contains("%")) {
            try {
                // Use URLDecoder (vs UriUtils) to preserve potentially decoded UTF-8 chars
                if (isInvalidPath(EncodeUtils.urlDecode(path, "UTF-8"))) {
                    logger.info("Ignoring invalid resource path with escape sequences [" + path + "].");
                    return null;
                }
            } catch (IllegalArgumentException ex) {
                // ignore
            }
        }
        for (Resource location : this.locations) {
            try {
                logger.debug("Checking location: " + location);
                Resource resource = location.createRelative(path);
                if (resource.exists() && resource.isReadable()) {
                    logger.info("Found match: " + resource);
                    return resource;
                } else {
                    logger.info("No match for location: " + location);
                }
            } catch (IOException ex) {
                logger.error("Failure checking for relative resource - trying next location", ex);
            }
        }
        return null;
    }

    /**
     * Process the given resource path to be used.
     * <p>The default implementation replaces any combination of leading '/' and
     * control characters (00-1F and 7F) with a single "/" or "". For example
     * {@code "  // /// ////  foo/bar"} becomes {@code "/foo/bar"}.
     *
     * @since 3.2.12
     */
    protected String processPath(String path) {
        boolean slash = false;
        for (int i = 0; i < path.length(); i++) {
            if (path.charAt(i) == '/') {
                slash = true;
            } else if (path.charAt(i) > ' ' && path.charAt(i) != 127) {
                if (i == 0 || (i == 1 && slash)) {
                    return path;
                }
                path = slash ? "/" + path.substring(i) : path.substring(i);
                if (logger.isTraceEnabled()) {
                    logger.trace("Path after trimming leading '/' and control characters: " + path);
                }
                return path;
            }
        }
        return (slash ? "/" : "");
    }

    /**
     * Identifies invalid resource paths. By default rejects:
     * <ul>
     * <li>Paths that contain "WEB-INF" or "META-INF"
     * <li>Paths that contain "../" after a call to
     * {@link org.springframework.util.StringUtils#cleanPath}.
     * <li>Paths that represent a {@link org.springframework.util.ResourceUtils#isUrl
     * valid URL} or would represent one after the leading slash is removed.
     * </ul>
     * <p><strong>Note:</strong> this method assumes that leading, duplicate '/'
     * or control characters (e.g. white space) have been trimmed so that the
     * path starts predictably with a single '/' or does not have one.
     *
     * @param path the path to validate
     * @return {@code true} if the path is invalid, {@code false} otherwise
     */
    protected boolean isInvalidPath(String path) {
        logger.debug("Applying \"invalid path\" checks to path: " + path);
        if (path.contains(":/")) {
            String relativePath = (path.charAt(0) == '/' ? path.substring(1) : path);
            if (ResourceUtils.isUrl(relativePath) || relativePath.startsWith("url:")) {
                logger.info("Path represents URL or has \"url:\" prefix.");
                return true;
            }
        }
        if (path.contains("..")) {
            path = StringUtils.cleanPath(path);
            if (path.contains("../")) {
                logger.info("Path contains \"../\" after call to StringUtils#cleanPath.");
                return true;
            }
        }
        return false;
    }

    /**
     * Determine an appropriate media type for the given resource.
     *
     * @param resource the resource to check
     * @return the corresponding media type, or {@code null} if none found
     */
    protected MediaType getMediaType(Resource resource) {
        return ActivationMediaTypeFactory.getMediaType(resource.getFilename());
    }

    protected boolean checkNotModified(HttpRequest request, HttpResponse response, long lastModifiedTimestamp) {
        if (lastModifiedTimestamp < 0 || response.getHeaders().contains(HEADER_LAST_MODIFIED)) {
            return false;
        }
        Long ifModifiedSince = request.getHeaders().getTimeMillis(HEADER_IF_MODIFIED_SINCE);
        boolean notModified = (ifModifiedSince != null && ifModifiedSince >= (lastModifiedTimestamp / 1000 * 1000));
        if (notModified) {
            response.setStatus(HttpResponseStatus.NOT_MODIFIED);
        } else {
            response.getHeaders().set(HEADER_LAST_MODIFIED, DateFormatter.format(new Date(lastModifiedTimestamp)));
        }
        return notModified;
    }

    /**
     * Inner class to avoid a hard-coded JAF dependency.
     */
    private static class ActivationMediaTypeFactory {

        private static final FileTypeMap fileTypeMap;

        static {
            fileTypeMap = loadFileTypeMapFromContextSupportModule();
        }

        private static FileTypeMap loadFileTypeMapFromContextSupportModule() {
            // See if we can find the extended mime.types from the context-support module...
            Resource mappingLocation = new ClassPathResource("org/springframework/mail/javamail/mime.types");
            if (mappingLocation.exists()) {
                InputStream inputStream = null;
                try {
                    inputStream = mappingLocation.getInputStream();
                    return new MimetypesFileTypeMap(inputStream);
                } catch (IOException ex) {
                    // ignore
                } finally {
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException ex) {
                            // ignore
                        }
                    }
                }
            }
            return FileTypeMap.getDefaultFileTypeMap();
        }

        public static MediaType getMediaType(String filename) {
            String mediaType = fileTypeMap.getContentType(filename);
            return (StringUtils.hasText(mediaType) ? MediaType.parseMediaType(mediaType) : null);
        }
    }

}
