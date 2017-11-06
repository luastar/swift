package com.luastar.swift.demo.http.entity;

import com.luastar.swift.base.json.JsonUtils;
import org.quartz.Trigger;

public class ScheduleJob {

    /**
     * 任务序号
     */
    private Long jobIndex;
    /**
     * 任务jobCode
     */
    private String jobCode;
    /**
     * 任务描述
     */
    private String jobDesc;
    /**
     * 任务状态
     */
    private String jobStatus = Trigger.TriggerState.NORMAL.name();
    /**
     * 任务表达式
     */
    private String cronExpression;
    /**
     * 任务是否允许同时发生
     */
    private boolean concurrent;
    /**
     * 任务远程地址
     */
    private String remoteUrl;
    /**
     * 任务远程参数
     */
    private String remoteConfig;

    public Long getJobIndex() {
        return jobIndex;
    }

    public void setJobIndex(Long jobIndex) {
        this.jobIndex = jobIndex;
    }

    public String getJobCode() {
        return jobCode;
    }

    public void setJobCode(String jobCode) {
        this.jobCode = jobCode;
    }

    public String getJobDesc() {
        return jobDesc;
    }

    public void setJobDesc(String jobDesc) {
        this.jobDesc = jobDesc;
    }

    public String getJobStatus() {
        return jobStatus;
    }

    public void setJobStatus(String jobStatus) {
        this.jobStatus = jobStatus;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public boolean isConcurrent() {
        return concurrent;
    }

    public void setConcurrent(boolean concurrent) {
        this.concurrent = concurrent;
    }

    public String getRemoteUrl() {
        return remoteUrl;
    }

    public void setRemoteUrl(String remoteUrl) {
        this.remoteUrl = remoteUrl;
    }

    public String getRemoteConfig() {
        return remoteConfig;
    }

    public void setRemoteConfig(String remoteConfig) {
        this.remoteConfig = remoteConfig;
    }

    @Override
    public String toString() {
        return JsonUtils.toJson(this);
    }

}
