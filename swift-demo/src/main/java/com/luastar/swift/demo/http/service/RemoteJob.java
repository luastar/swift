package com.luastar.swift.demo.http.service;

import com.google.common.collect.Maps;
import com.luastar.swift.base.net.HttpClientUtils;
import com.luastar.swift.base.net.HttpResult;
import com.luastar.swift.demo.http.entity.ScheduleJob;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * 任务执行器
 */
//@DisallowConcurrentExecution // 上次任务执行完才继续执行
public class RemoteJob implements Job {

    private static final Logger logger = LoggerFactory.getLogger(RemoteJob.class);

    @Override
    public void execute(JobExecutionContext context) {
        ScheduleJob scheduleJob = (ScheduleJob) context.getMergedJobDataMap().get("scheduleJob");
        if (scheduleJob == null) {
            logger.warn("从JobExecutionContext获取任务信息为空！");
            return;
        }
        logger.info("开始执行任务：{}", scheduleJob);
        // 非同步任务
        if (scheduleJob.isConcurrent()) {
            executeConcurrent(scheduleJob);
        } else {
            executeRemote(scheduleJob);
        }
        logger.info("结束执行任务：{}", scheduleJob);
    }

    private void executeConcurrent(ScheduleJob scheduleJob) {
        executeRemote(scheduleJob);
    }

    /**
     * 执行远程任务
     *
     * @param scheduleJob
     */
    private void executeRemote(ScheduleJob scheduleJob) {
        Map<String, String> paramMap = Maps.newLinkedHashMap();
        paramMap.put("jobCode", scheduleJob.getJobCode());
        paramMap.put("jobConfig", scheduleJob.getRemoteConfig());
        HttpResult result = HttpClientUtils.postHttpResult(scheduleJob.getRemoteUrl(), paramMap, null, 120000);
        if (result.getStatus() != HttpResult.STATUS_EXP) {
            logger.info("执行任务成功，结果为：{}", result.getResult());
        } else {
            logger.error("执行任务异常，异常为：", result.getException());
        }
    }

}
