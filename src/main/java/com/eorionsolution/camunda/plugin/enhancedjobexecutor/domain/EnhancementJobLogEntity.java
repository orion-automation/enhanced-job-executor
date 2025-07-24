package com.eorionsolution.camunda.plugin.enhancedjobexecutor.domain;

import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.UUID;

public class EnhancementJobLogEntity {
    protected String id;
    protected String jobId;
    protected String exceptionMessage;
    protected int jobState;
    protected long timeout;
    protected String jobDefinitionId;
    protected String jobDefinitionType;
    protected String activityId;
    protected String executionId;
    protected String rootProcessInstanceId;
    protected String processInstanceId;
    protected String processDefinitionId;
    protected String processDefinitionKey;
    protected String deploymentId;
    protected String tenantId;
    protected String hostName;
    protected String executionThreadName;
    protected Date startTime;
    protected Date endTime;
    protected long duration;

    public static EnhancementJobLogEntity getInstance(JobEntity jobEntity, long timeout) {
        var instance = new EnhancementJobLogEntity();
        instance.setId(UUID.randomUUID().toString());
        instance.setJobState(1);
        try {
            instance.setHostName(InetAddress.getLocalHost().getHostName());
        } catch (UnknownHostException e) {
            instance.setHostName("UnknownHostName");
        }
        instance.setJobId(jobEntity.getId());
        instance.setJobDefinitionId(jobEntity.getJobDefinitionId());
        instance.setJobDefinitionType(jobEntity.getJobHandlerType());
        instance.setActivityId(jobEntity.getActivityId());
        instance.setExecutionId(jobEntity.getExecutionId());
        instance.setRootProcessInstanceId(jobEntity.getRootProcessInstanceId());
        instance.setProcessInstanceId(jobEntity.getProcessInstanceId());
        instance.setProcessDefinitionId(jobEntity.getProcessDefinitionId());
        instance.setProcessDefinitionKey(jobEntity.getProcessDefinitionKey());
        instance.setDeploymentId(jobEntity.getDeploymentId());
        instance.setTenantId(jobEntity.getTenantId());
        instance.setTimeout(timeout);
        instance.setStartTime(new Date());
        instance.setExecutionThreadName(Thread.currentThread().getName());
        return instance;
    }

    public void success() {
        this.setJobState(2);
        this.setEndTime(new Date());
        this.setDuration(this.getEndTime().getTime() - this.getStartTime().getTime());
    }

    public void failed(String exceptionMessage) {
        this.setJobState(3);
        this.setExceptionMessage(exceptionMessage);
        this.setEndTime(new Date());
        this.setDuration(this.getEndTime().getTime() - this.getStartTime().getTime());
    }

    public boolean isFailed() {
        return this.getJobState() == 3;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getExceptionMessage() {
        return exceptionMessage;
    }

    public void setExceptionMessage(String exceptionMessage) {
        this.exceptionMessage = exceptionMessage;
    }

    public int getJobState() {
        return jobState;
    }

    public void setJobState(int jobState) {
        this.jobState = jobState;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public String getJobDefinitionId() {
        return jobDefinitionId;
    }

    public void setJobDefinitionId(String jobDefinitionId) {
        this.jobDefinitionId = jobDefinitionId;
    }

    public String getJobDefinitionType() {
        return jobDefinitionType;
    }

    public void setJobDefinitionType(String jobDefinitionType) {
        this.jobDefinitionType = jobDefinitionType;
    }

    public String getActivityId() {
        return activityId;
    }

    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }

    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public String getRootProcessInstanceId() {
        return rootProcessInstanceId;
    }

    public void setRootProcessInstanceId(String rootProcessInstanceId) {
        this.rootProcessInstanceId = rootProcessInstanceId;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public String getProcessDefinitionId() {
        return processDefinitionId;
    }

    public void setProcessDefinitionId(String processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
    }

    public String getProcessDefinitionKey() {
        return processDefinitionKey;
    }

    public void setProcessDefinitionKey(String processDefinitionKey) {
        this.processDefinitionKey = processDefinitionKey;
    }

    public String getDeploymentId() {
        return deploymentId;
    }

    public void setDeploymentId(String deploymentId) {
        this.deploymentId = deploymentId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getExecutionThreadName() {
        return executionThreadName;
    }

    public void setExecutionThreadName(String executionThreadName) {
        this.executionThreadName = executionThreadName;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof EnhancementJobLogEntity entity)) return false;
        return Objects.equals(id, entity.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", EnhancementJobLogEntity.class.getSimpleName() + "[", "]")
                .add("id='" + id + "'")
                .add("jobId='" + jobId + "'")
                .add("exceptionMessage='" + exceptionMessage + "'")
                .add("jobState=" + jobState)
                .add("timeout=" + timeout)
                .add("jobDefinitionId='" + jobDefinitionId + "'")
                .add("jobDefinitionType='" + jobDefinitionType + "'")
                .add("activityId='" + activityId + "'")
                .add("executionId='" + executionId + "'")
                .add("rootProcessInstanceId='" + rootProcessInstanceId + "'")
                .add("processInstanceId='" + processInstanceId + "'")
                .add("processDefinitionId='" + processDefinitionId + "'")
                .add("processDefinitionKey='" + processDefinitionKey + "'")
                .add("deploymentId='" + deploymentId + "'")
                .add("tenantId='" + tenantId + "'")
                .add("hostName='" + hostName + "'")
                .add("executionThreadName='" + executionThreadName + "'")
                .add("startTime=" + startTime)
                .add("endTime=" + endTime)
                .add("duration=" + duration)
                .toString();
    }
}
