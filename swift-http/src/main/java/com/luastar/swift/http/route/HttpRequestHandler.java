package com.luastar.swift.http.route;

import com.luastar.swift.http.server.HttpRequest;
import com.luastar.swift.http.server.HttpResponse;

public interface HttpRequestHandler {

    void handleRequest(HttpRequest request, HttpResponse response) throws Exception;

}
