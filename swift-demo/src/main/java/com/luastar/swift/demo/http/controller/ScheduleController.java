package com.luastar.swift.demo.http.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.luastar.swift.base.json.JsonUtils;
import com.luastar.swift.base.utils.ObjUtils;
import com.luastar.swift.base.utils.RandomUtils;
import com.luastar.swift.demo.http.entity.ScheduleJob;
import com.luastar.swift.demo.http.service.ScheduleService;
import com.luastar.swift.demo.http.utils.ResponseResultUtils;
import com.luastar.swift.http.route.RequestMethod;
import com.luastar.swift.http.server.HttpRequest;
import com.luastar.swift.http.server.HttpResponse;
import com.luastar.swift.http.server.HttpService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 字段名                允许的值                允许的特殊字符
 * 秒                    0-59                   , - * /
 * 分                    0-59                   , - * /
 * 小时                  0-23                   , - * /
 * 日（Day-of-Month）    1-31                   , - * ? / L W C
 * 月                    1-12 或 JAN-DEC        , - * /
 * 周（Day-of-Week）     1-7 或 SUN-SAT         , - * ? / L C #
 * 年(可选)                                     1970-2099 , - * /
 * ：代表所有可能的值；例如：在月中表示每个月，在日中表示每天，在小时中表示每小时
 * - ：表示指定范围；
 * , ：表示列出枚举值；例如：在分钟中，“5,20”表示在5分钟和20分钟触发。
 * / ：被用于指定增量；例如：在分钟中，“0/15”表示从0分钟开始，每15分钟执行一次。"3/20"表示从第三分钟开始，每20分钟执行一次。和"3,23,43"（表示第3，23，43分钟触发）的含义一样。
 * ? ：用在Day-of-Month和Day-of-Week中，指“没有具体的值”。当两个子表达式其中一个被指定了值以后，为了避免冲突，需要将另外一个的值设为“?”。例如：想在每月20日触发调度，不管20号是星期几，只能用如下写法：0 0 0 20 * ?，其中最后以为只能用“?”，而不能用“*”。
 */
@HttpService("/job")
public class ScheduleController {

    private final Logger logger = LoggerFactory.getLogger(ScheduleController.class);

    @Autowired
    private ScheduleService scheduleService;

    /**
     * 首页面
     *
     * @param request
     * @param response
     */
    @HttpService(value = "")
    public void index(HttpRequest request, HttpResponse response) throws Exception {
        response.sendRedirect("/static/pages/index.html");
    }

    /**
     * 添加任务
     *
     * @param request
     * @param response
     */
    @HttpService(value = "/add", method = RequestMethod.POST)
    public void add(HttpRequest request, HttpResponse response) throws Exception {
        String body = request.getBody();
        ScheduleJob scheduleJob = JsonUtils.toObj(body, ScheduleJob.class);
        scheduleJob.setJobCode(RandomUtils.bsonId());
        scheduleService.saveJob(scheduleJob);
        ResponseResultUtils.success(response, scheduleJob);
    }

    /**
     * 修改任务
     *
     * @param request
     * @param response
     */
    @HttpService(value = "/edit", method = RequestMethod.POST)
    public void edit(HttpRequest request, HttpResponse response) throws Exception {
        String body = request.getBody();
        ScheduleJob scheduleJob = JsonUtils.toObj(body, ScheduleJob.class);
        scheduleService.saveJob(scheduleJob);
        ResponseResultUtils.success(response, null);
    }

    /**
     * 删除任务
     *
     * @param request
     * @param response
     */
    @HttpService(value = "/delete")
    public void delete(HttpRequest request, HttpResponse response) throws Exception {
        ScheduleJob scheduleJob = request.bindObj(new ScheduleJob());
        scheduleService.deleteJob(scheduleJob);
        ResponseResultUtils.success(response, null);
    }

    /**
     * 任务列表
     *
     * @param request
     * @param response
     */
    @HttpService("/list")
    public void list(HttpRequest request, HttpResponse response) throws Exception {
        String jobDesc = request.getParameter("jobDesc");
        List<ScheduleJob> jobList = scheduleService.listJob();
        if (StringUtils.isNotEmpty(jobDesc)) {
            jobList = jobList.stream()
                    .filter(job -> job.getJobDesc().contains(jobDesc))
                    .collect(Collectors.toList());
        }
        Map<String, Object> resultMap = Maps.newLinkedHashMap();
        resultMap.put("code", 0);
        resultMap.put("msg", "ok");
        resultMap.put("count", jobList.size());
        resultMap.put("data", jobList);
        response.setResult(JsonUtils.toJson(resultMap));
    }

    /**
     * 导出列表配置
     *
     * @param request
     * @param response
     */
    @HttpService("/export")
    public void exportJob(HttpRequest request, HttpResponse response) throws Exception {
        List<ScheduleJob> jobList = scheduleService.listJob();
        String json = JSON.toJSONString(jobList);
        response.setResult(json);
    }

    /**
     * 导入列表配置
     *
     * @param request
     * @param response
     */
    @HttpService("/import")
    public void importJob(HttpRequest request, HttpResponse response) throws Exception {
        String body = request.getBody();
        if (StringUtils.isEmpty(body)) {
            throw new RuntimeException("导入内容不能为空！");
        }
        JSONArray jsonArray = JSON.parseArray(body);
        List<ScheduleJob> jobList = jsonArray.toJavaList(ScheduleJob.class);
        scheduleService.importJob(jobList);
        ResponseResultUtils.success(response, null);
    }

    /**
     * 执行任务
     *
     * @param request
     * @param response
     */
    @HttpService("/trigger")
    public void trigger(HttpRequest request, HttpResponse response) throws Exception {
        ScheduleJob scheduleJob = request.bindObj(new ScheduleJob());
        scheduleService.triggerJob(scheduleJob);
        ResponseResultUtils.success(response, null);
    }

    /**
     * 暂停任务
     *
     * @param request
     * @param response
     */
    @HttpService("/pause")
    public void pause(HttpRequest request, HttpResponse response) throws Exception {
        ScheduleJob scheduleJob = request.bindObj(new ScheduleJob());
        scheduleService.pauseJob(scheduleJob);
        ResponseResultUtils.success(response, null);
    }

    /**
     * 恢复任务
     *
     * @param request
     * @param response
     */
    @HttpService("/resume")
    public void resume(HttpRequest request, HttpResponse response) throws Exception {
        ScheduleJob scheduleJob = request.bindObj(new ScheduleJob());
        scheduleService.resumeJob(scheduleJob);
        ResponseResultUtils.success(response, null);
    }

    /**
     * 解锁任务
     *
     * @param request
     * @param response
     */
    @HttpService("/unlock")
    public void unlock(HttpRequest request, HttpResponse response) throws Exception {
        ScheduleJob scheduleJob = request.bindObj(new ScheduleJob());
        ResponseResultUtils.success(response, null);
    }

    /**
     * 查看任务日志
     *
     * @param request
     * @param response
     */
    @HttpService("/view_log")
    public void viewLog(HttpRequest request, HttpResponse response) throws Exception {
        ScheduleJob scheduleJob = request.bindObj(new ScheduleJob());
        ResponseResultUtils.success(response, Lists.newArrayList());
    }

    /**
     * 删除日志
     *
     * @param request
     * @param response
     */
    @HttpService("/delete_log")
    public void deleteLog(HttpRequest request, HttpResponse response) throws Exception {
        // 任务列表
        List<ScheduleJob> jobList = scheduleService.listJob();
        if (CollectionUtils.isEmpty(jobList)) {
            logger.info("任务列表为空，不需要清理日志。");
            ResponseResultUtils.success(response, null);
            return;
        }
        // 删除N天前的日志
        Integer days = 10;
        String jobConfig = request.getParameter("jobConfig");
        JSONObject configJson = JSON.parseObject(jobConfig);
        if (configJson != null) {
            days = ObjUtils.ifNull(configJson.getInteger("days"), 10);
        }
        logger.info("删除{}天前的日志", days);
        ResponseResultUtils.success(response, null);
    }

}
