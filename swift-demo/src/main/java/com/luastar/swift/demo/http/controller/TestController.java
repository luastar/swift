package com.luastar.swift.demo.http.controller;

import com.luastar.swift.base.json.JsonUtils;
import com.luastar.swift.demo.http.entity.User;
import com.luastar.swift.http.route.RequestMethod;
import com.luastar.swift.http.server.HttpFileUpload;
import com.luastar.swift.http.server.HttpRequest;
import com.luastar.swift.http.server.HttpResponse;
import com.luastar.swift.http.server.HttpService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

@HttpService("/test")
public class TestController {

    private static final Logger logger = LoggerFactory.getLogger(TestController.class);

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
        for (Map.Entry<String, HttpFileUpload> file : request.getFileMap().entrySet()) {
            logger.info("request parameter : {}={}", file.getKey(), file.getValue().getFilename());
        }
        logger.info("request to user : {}", request.bindObj(new User()));
        logger.info("request body is : {}", request.getBody());
        if (StringUtils.isNoneBlank(request.getParameter("exp"))) {
            throw new RuntimeException("业务异常");
        }
        // response
        response.setResponseContentTypePlain();
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
        response.setResponseContentTypePlain();
        response.setResult(result);
    }

    @HttpService("/hello/{p1}")
    public void hello(HttpRequest request, HttpResponse response) {
        logger.info("----------come into TestCtrl[hello][{}]", request.getPathValue("p1"));
        // response
        response.setResponseContentTypePlain();
        response.setResult("TestCtrl[hello] OK !");
    }

    @HttpService(value = "/getpost", method = RequestMethod.GET)
    public void get(HttpRequest request, HttpResponse response) {
        logger.info("----------come into TestCtrl[get]");
        // response
        response.setResponseContentTypePlain();
        response.setResult("TestCtrl[get] OK !");
    }

    @HttpService(value = "/getpost", method = RequestMethod.POST)
    public void post(HttpRequest request, HttpResponse response) {
        logger.info("----------come into TestCtrl[post]");
        // response
        response.setResponseContentTypePlain();
        response.setResult("TestCtrl[post] OK !");
    }

    @HttpService(value = "/getpost")
    public void getpost(HttpRequest request, HttpResponse response) {
        logger.info("----------come into TestCtrl[getpost]");
        // response
        response.setResponseContentTypePlain();
        response.setResult("TestCtrl[getpost] OK !");
    }

    @HttpService(value = "/get_or_post", method = {RequestMethod.GET, RequestMethod.POST})
    public void getOrPost(HttpRequest request, HttpResponse response) {
        logger.info("----------come into TestCtrl[get_or_post]");
        // response
        response.setResponseContentTypePlain();
        response.setResult("TestCtrl[get_or_post] OK !");
    }

    @HttpService("/upload")
    public void upload(HttpRequest request, HttpResponse response) {
        logger.info("----------come into TestCtrl[upload]");
        Collection<HttpFileUpload> fileUploadSet = request.getFileMap().values();
        for (HttpFileUpload file : fileUploadSet) {
            logger.info("request parameter : {}={}", file.getName(), file.getFilename());
            try {
                File saveFile = new File("/Users/zhuminghua/Downloads/" + file.getFilename());
                FileUtils.copyInputStreamToFile(file.getInputStream(), saveFile);
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
        // response
        response.setResponseContentTypePlain();
        response.setResult("TestCtrl[upload] OK !");
    }

    @HttpService("/download")
    public void download(HttpRequest request, HttpResponse response) {
        try {
            Workbook wb = new XSSFWorkbook();
            CreationHelper createHelper = wb.getCreationHelper();
            Sheet sheet = wb.createSheet("sheet1");
            Row row = sheet.createRow((short) 0);
            row.createCell(0).setCellValue(createHelper.createRichTextString("aaa"));
            row.createCell(1).setCellValue(createHelper.createRichTextString("bbb"));
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            wb.write(outputStream);
            response.setResponseContentTypeStream("aaa.xlsx");
            response.setOutputStream(outputStream);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

}
