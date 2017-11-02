package com.luastar.swift.http.route.resource;

import com.luastar.swift.http.constant.HttpConstant;
import com.luastar.swift.http.route.HttpRequestHandler;
import com.luastar.swift.http.route.RequestMethod;
import com.luastar.swift.http.server.HttpRequest;
import com.luastar.swift.http.server.HttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.activation.MimetypesFileTypeMap;
import java.io.IOException;
import java.util.List;

/**
 *
 */
public class ResourceHttpRequestHandler extends WebContentGenerator implements InitializingBean, HttpRequestHandler {

    private List<Resource> locations;

    public ResourceHttpRequestHandler() {
        super(RequestMethod.GET.name(), RequestMethod.HEAD.name());
    }

    /**
     * Set a {@code List} of {@code Resource} paths to use as sources
     * for serving static resources.
     */
    public void setLocations(List<Resource> locations) {
        Assert.notEmpty(locations, "Locations list must not be empty");
        this.locations = locations;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (CollectionUtils.isEmpty(this.locations)) {
            logger.warn("Locations list is empty. No resources will be served");
        }
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
        checkAndPrepare(request, response, true);
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
        MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
        response.getHeaders().set(HttpHeaderNames.CONTENT_TYPE, mimeTypesMap.getContentType(resource.getFilename()));
        // content phase
        if (RequestMethod.HEAD.name().equals(request.getMethod())) {
            logger.trace("HEAD request - skipping content");
            return;
        }
        response.setResource(resource);
    }

    protected Resource getResource(HttpRequest request) {
        String path = (String) request.getAttribute(HttpConstant.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        if (path == null) {
            throw new IllegalStateException("Required request attribute '" + HttpConstant.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE + "' is not set");
        }
        if (!StringUtils.hasText(path) || isInvalidPath(path)) {
            logger.info("Ignoring invalid resource path [" + path + "]");
            return null;
        }
        for (Resource location : this.locations) {
            try {
                logger.info("Trying relative path [" + path + "] against base location: " + location);
                Resource resource = location.createRelative(path);
                if (resource.exists() && resource.isReadable()) {
                    logger.info("Found matching resource: " + resource);
                    return resource;
                } else {
                    logger.trace("Relative resource doesn't exist or isn't readable: " + resource);
                }
            } catch (IOException ex) {
                logger.debug("Failed to create relative resource - trying next resource location", ex);
            }
        }
        return null;
    }

    /**
     * Validates the given path: returns {@code true} if the given path is not a valid resource path.
     * <p>The default implementation rejects paths containing "WEB-INF" or "META-INF" as well as paths
     * with relative paths ("../") that result in access of a parent directory.
     *
     * @param path the path to validate
     * @return {@code true} if the path has been recognized as invalid, {@code false} otherwise
     */
    protected boolean isInvalidPath(String path) {
        return (path.contains("WEB-INF")
                || path.contains("META-INF")
                || StringUtils.cleanPath(path).startsWith(".."));
    }

    public boolean checkNotModified(HttpRequest request, HttpResponse response, long lastModifiedTimestamp) {
        if (lastModifiedTimestamp < 0 || response.getHeaders().contains(HEADER_LAST_MODIFIED)) {
            return false;
        }
        long ifModifiedSince = request.getHeaders().getTimeMillis(HEADER_IF_MODIFIED_SINCE);
        boolean notModified = (ifModifiedSince >= (lastModifiedTimestamp / 1000 * 1000));
        if (notModified) {
            response.setStatus(HttpResponseStatus.NOT_MODIFIED);
        } else {
            response.getHeaders().set(HEADER_LAST_MODIFIED, lastModifiedTimestamp);
        }
        return notModified;
    }

}
