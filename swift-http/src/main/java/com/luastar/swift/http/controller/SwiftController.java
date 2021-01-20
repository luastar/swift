package com.luastar.swift.http.controller;

import com.luastar.swift.http.server.HttpRequest;
import com.luastar.swift.http.server.HttpResponse;
import com.luastar.swift.http.server.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@HttpService("/swift")
public class SwiftController {

    private static final Logger logger = LoggerFactory.getLogger(SwiftController.class);

    @HttpService("/alive")
    public void index(HttpRequest request, HttpResponse response) {
        response.setResponseContentTypePlain();
        response.setResult("OK");
    }

}
