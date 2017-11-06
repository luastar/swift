package com.luastar.swift.demo.http.service;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.luastar.swift.base.json.JsonUtils;
import com.luastar.swift.base.utils.RandomUtils;
import com.luastar.swift.demo.http.entity.ScheduleJob;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ScheduleService {

    private static final Logger logger = LoggerFactory.getLogger(ScheduleService.class);

    private static final String DEFAULT_GROUP = "job_group";

    private static final String JOB_KEY = "scheduleJob";

    @Autowired
    private Scheduler scheduler;

    /**
     * 初始化任务
     *
     * @return
     */
    @PostConstruct
    public void initJob() throws SchedulerException {
        ScheduleJob deleteLogJob = new ScheduleJob();
        deleteLogJob.setJobCode(RandomUtils.bsonId());
        deleteLogJob.setJobDesc("定时删除日志");
        deleteLogJob.setCronExpression("0 0 0 * * ?");
        deleteLogJob.setRemoteUrl("http://localhost:8081/job/delete_log");
        Map<String, Object> configMap = Maps.newLinkedHashMap();
        configMap.put("days", 3);
        deleteLogJob.setRemoteConfig(JSON.toJSONString(configMap));
        saveJob(deleteLogJob);
    }

    /**
     * 生成任务序号
     *
     * @return
     */
    private long genJobIndex() {
        return System.currentTimeMillis() - LocalDateTime.of(2017, 1, 1, 0, 0, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    /**
     * 导入任务
     *
     * @param jobList
     * @throws SchedulerException
     */
    public void importJob(List<ScheduleJob> jobList) throws SchedulerException {
        if (CollectionUtils.isEmpty(jobList)) {
            logger.info("导入的任务为空");
            return;
        }
        for (ScheduleJob job : jobList) {
            saveJob(job);
        }
    }

    /**
     * 添加任务
     *
     * @param scheduleJob
     */
    public void saveJob(ScheduleJob scheduleJob) throws SchedulerException {
        logger.info("saveJob {}", scheduleJob);
        if (StringUtils.isEmpty(scheduleJob.getJobCode())) {
            throw new RuntimeException("任务编码不能为空！");
        }
        if (StringUtils.isEmpty(scheduleJob.getRemoteUrl())) {
            throw new RuntimeException("远程地址不能为空！");
        }
        if (StringUtils.isEmpty(scheduleJob.getCronExpression())) {
            throw new RuntimeException("时间表达式不能为空！");
        }
        // 设置序号
        if (scheduleJob.getJobIndex() == null) {
            scheduleJob.setJobIndex(genJobIndex());
        }
        // 任务是否存在
        TriggerKey triggerKey = TriggerKey.triggerKey(scheduleJob.getJobCode(), DEFAULT_GROUP);
        CronTrigger cronTrigger = (CronTrigger) scheduler.getTrigger(triggerKey);
        if (cronTrigger == null) { // 新增
            JobDetail jobDetail = JobBuilder.newJob(RemoteJob.class).withIdentity(scheduleJob.getJobCode(), DEFAULT_GROUP).build();
            jobDetail.getJobDataMap().put(JOB_KEY, scheduleJob);
            // 时间表达式
            CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule(scheduleJob.getCronExpression());
            // 触发器
            cronTrigger = TriggerBuilder.newTrigger().withIdentity(scheduleJob.getJobCode(), DEFAULT_GROUP).withSchedule(cronScheduleBuilder).build();
            scheduler.scheduleJob(jobDetail, cronTrigger);
            // 处理暂停状态
            if (Trigger.TriggerState.PAUSED == Trigger.TriggerState.valueOf(scheduleJob.getJobStatus())) {
                JobKey jobKey = JobKey.jobKey(scheduleJob.getJobCode(), DEFAULT_GROUP);
                scheduler.pauseJob(jobKey);
            }
        } else { // 修改，注意jobDetail.getJobDataMap()中的数据不能修改
            deleteJob(scheduleJob);
            JobDetail jobDetail = JobBuilder.newJob(RemoteJob.class).withIdentity(scheduleJob.getJobCode(), DEFAULT_GROUP).build();
            jobDetail.getJobDataMap().put(JOB_KEY, scheduleJob);
            // 时间表达式
            CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule(scheduleJob.getCronExpression());
            // 触发器
            cronTrigger = TriggerBuilder.newTrigger().withIdentity(scheduleJob.getJobCode(), DEFAULT_GROUP).withSchedule(cronScheduleBuilder).build();
            scheduler.scheduleJob(jobDetail, cronTrigger);
        }
    }

    /**
     * 删除任务
     *
     * @param scheduleJob
     */
    public void deleteJob(ScheduleJob scheduleJob) throws SchedulerException {
        logger.info("deleteJob {}", scheduleJob);
        if (StringUtils.isEmpty(scheduleJob.getJobCode())) {
            throw new RuntimeException("任务编码不能为空！");
        }
        JobKey jobKey = JobKey.jobKey(scheduleJob.getJobCode(), DEFAULT_GROUP);
        if (!scheduler.checkExists(jobKey)) {
            throw new RuntimeException("任务不存在！");
        }
        scheduler.deleteJob(jobKey);
    }

    /**
     * 获取任务列表
     *
     * @return
     */
    public List<ScheduleJob> listJob() throws SchedulerException {
        List<ScheduleJob> jobList = Lists.newArrayList();
        Set<JobKey> jobKeySet = scheduler.getJobKeys(GroupMatcher.anyJobGroup());
        for (JobKey jobKey : jobKeySet) {
            List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
            for (Trigger trigger : triggers) {
                JobDetail jobDetail = scheduler.getJobDetail(jobKey);
                ScheduleJob job = (ScheduleJob) jobDetail.getJobDataMap().get(JOB_KEY);
                Trigger.TriggerState triggerState = scheduler.getTriggerState(trigger.getKey());
                job.setJobStatus(triggerState.name());
                if (trigger instanceof CronTrigger) {
                    String cronExpression = ((CronTrigger) trigger).getCronExpression();
                    job.setCronExpression(cronExpression);
                }
                jobList.add(job);
            }
        }
        logger.info("listJob:{}", JsonUtils.toJson(jobList));
        // 按序号排序
        jobList.sort((o1, o2) -> o2.getJobIndex().compareTo(o1.getJobIndex()));
        logger.info("listJob:{}", JsonUtils.toJson(jobList));
        return jobList;
    }

    /**
     * 执行任务
     *
     * @param scheduleJob
     */
    public void triggerJob(ScheduleJob scheduleJob) throws SchedulerException {
        logger.info("triggerJob {}", scheduleJob);
        if (StringUtils.isEmpty(scheduleJob.getJobCode())) {
            throw new RuntimeException("任务编码不能为空！");
        }
        JobKey jobKey = JobKey.jobKey(scheduleJob.getJobCode(), DEFAULT_GROUP);
        if (!scheduler.checkExists(jobKey)) {
            throw new RuntimeException("任务不存在！");
        }
        scheduler.triggerJob(jobKey);
    }

    /**
     * 暂停任务
     *
     * @param scheduleJob
     */
    public void pauseJob(ScheduleJob scheduleJob) throws SchedulerException {
        logger.info("pauseJob {}", scheduleJob);
        if (StringUtils.isEmpty(scheduleJob.getJobCode())) {
            throw new RuntimeException("任务编码不能为空！");
        }
        JobKey jobKey = JobKey.jobKey(scheduleJob.getJobCode(), DEFAULT_GROUP);
        if (!scheduler.checkExists(jobKey)) {
            throw new RuntimeException("任务不存在！");
        }
        scheduler.pauseJob(jobKey);
    }

    /**
     * 恢复任务
     *
     * @param scheduleJob
     */
    public void resumeJob(ScheduleJob scheduleJob) throws SchedulerException {
        logger.info("resumeJob {}", scheduleJob);
        if (StringUtils.isEmpty(scheduleJob.getJobCode())) {
            throw new RuntimeException("任务编码不能为空！");
        }
        JobKey jobKey = JobKey.jobKey(scheduleJob.getJobCode(), DEFAULT_GROUP);
        if (!scheduler.checkExists(jobKey)) {
            throw new RuntimeException("任务不存在！");
        }
        scheduler.resumeJob(jobKey);
    }

}
