package com.luastar.swift.demo.http.server;

import com.luastar.swift.base.json.JsonUtils;
import com.luastar.swift.http.server.HttpRequest;
import com.luastar.swift.http.server.HttpResponse;
import com.luastar.swift.http.server.HttpService;
import io.netty.handler.codec.http.multipart.FileUpload;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Map;

@HttpService("/test")
public class HelloService {

    private static final Logger logger = LoggerFactory.getLogger(HelloService.class);

    @Autowired
    private RedisTemplate redisTemplate;

    @HttpService("")
    public void index(HttpRequest request, HttpResponse response) {
        logger.info("----------come into TestCtrl[index]");
        // request info
        logger.info("requestId is : {}", request.getRequestId());
        logger.info("client ip is : {}", request.getIp());
        for (Map.Entry<String, String> header : request.getHeaderMap().entrySet()) {
            logger.info("request header : {}={}", header.getKey(), header.getValue());
        }
        for (Map.Entry<String, String> parameter : request.getParameterMap().entrySet()) {
            logger.info("request parameter : {}={}", parameter.getKey(), parameter.getValue());
        }
        for (Map.Entry<String, FileUpload> file : request.getFileMap().entrySet()) {
            logger.info("request parameter : {}={}", file.getKey(), file.getValue().getFilename());
        }
        logger.info("request to user : {}", request.bindObj(new User()));
        logger.info("request body is : {}", request.getBody());
        if (StringUtils.isNoneBlank(request.getParameter("exp"))){
            throw new RuntimeException("业务异常");
        }
        // response
        response.setResponseContentTypeJson();
        response.setResult("TestCtrl[index] OK !");
    }

    @HttpService("/redis")
    public void redis(HttpRequest request, HttpResponse response) {
        logger.info("----------come into TestCtrl[redis]");
        String result = "";
        Map<String, String> userMap = redisTemplate.opsForHash().entries("user:info:10001");
        if (userMap != null) {
            result = JsonUtils.toJson(userMap);
        }
        logger.info("TestCtrl[redis] result:{}", result);
        // response
        response.setResponseContentTypeJson();
        response.setResult(result);
    }

    @HttpService("/hello/{p1}")
    public void hello(HttpRequest request, HttpResponse response) {
        logger.info("----------come into TestCtrl[hello][{}]", request.getPathValue("p1"));
        // request info
        logger.info("client ip is : {}", request.getIp());
        for (Map.Entry<String, String> header : request.getHeaderMap().entrySet()) {
            logger.info("request header : {}={}", header.getKey(), header.getValue());
        }
        for (Map.Entry<String, String> parameter : request.getParameterMap().entrySet()) {
            logger.info("request parameter : {}={}", parameter.getKey(), parameter.getValue());
        }
        for (Map.Entry<String, FileUpload> file : request.getFileMap().entrySet()) {
            logger.info("request parameter : {}={}", file.getKey(), file.getValue().getFilename());
        }
        logger.info("request body is : {}", request.getBody());
        // response
        response.setResult("TestCtrl[hello] OK !");
    }

}
