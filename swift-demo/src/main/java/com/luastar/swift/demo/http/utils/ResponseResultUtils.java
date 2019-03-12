package com.luastar.swift.demo.http.utils;

import com.luastar.swift.base.config.PropertyUtils;
import com.luastar.swift.base.json.JsonUtils;
import com.luastar.swift.base.utils.ExceptionUtils;
import com.luastar.swift.demo.http.entity.ResponseResult;
import com.luastar.swift.http.server.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class ResponseResultUtils {

    private static Logger logger = LoggerFactory.getLogger(ResponseResultUtils.class);

    public static void success(HttpResponse httpResponse, Object data) {
        ResponseResult result = new ResponseResult();
        result.setRequest_id(httpResponse.getRequestId());
        result.setCode(0);
        result.setMsg(PropertyUtils.getMessage(result.getCode()));
        result.setData(data);
        httpResponse.setResult(JsonUtils.toJson(result));
    }

    public static void exception(HttpResponse httpResponse, Throwable exception) {
        ResponseResult result = new ResponseResult();
        result.setRequest_id(httpResponse.getRequestId());
        result.setCode(500);
        result.setMsg(ExceptionUtils.getErrorMessageWithNestedException(exception));
        httpResponse.setResult(JsonUtils.toJson(result));
    }

}
