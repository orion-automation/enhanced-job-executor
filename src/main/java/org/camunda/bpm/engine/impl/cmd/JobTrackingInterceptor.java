package org.camunda.bpm.engine.impl.cmd;

import com.eorionsolution.camunda.plugin.enhancedjobexecutor.domain.EnhancementJobLogEntity;
import com.eorionsolution.camunda.plugin.enhancedjobexecutor.service.EnhancementJobLogService;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandInterceptor;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.model.bpmn.instance.FlowElement;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.concurrent.*;

public class JobTrackingInterceptor extends CommandInterceptor {
    private final ProcessEngineConfigurationImpl configuration;
    private final boolean dbLogEnabled;
    private final EnhancementJobLogService service;
    private final long timeoutMs;
    public static final String TIMEOUT_PROPERTY_NAME = "timeout";

    public JobTrackingInterceptor(ProcessEngineConfigurationImpl configuration, long timeoutMs) {
        this.configuration = configuration;
        this.timeoutMs = timeoutMs;
        this.dbLogEnabled = false;
        this.service = null;
    }

    public JobTrackingInterceptor(ProcessEngineConfigurationImpl configuration, EnhancementJobLogService service, long timeoutMs) {
        this.configuration = configuration;
        this.dbLogEnabled = true;
        this.timeoutMs = timeoutMs;
        this.service = service;
    }

    private static final Logger log = LoggerFactory.getLogger(JobTrackingInterceptor.class);

    private final ExecutorService timeoutExecutor = Executors.newCachedThreadPool(r -> {
        var t = new Thread(r);
        t.setDaemon(true);
        t.setName("JobTrackingInterceptor");
        return t;
    });

    @Override
    public <T> T execute(Command<T> command) {
        if (command instanceof ExecuteJobsCmd cmd) {
            var timeout = timeoutMs;
            var configure = Context.getCommandContext().getProcessEngineConfiguration();
            var jobId = cmd.jobId;
            var JobIdParts = jobId.split("-");
            var threadName = JobIdParts[0] + JobIdParts[1];
            var startTime = System.currentTimeMillis();
            MDC.put("jobId", jobId);
            var job = configure.getManagementService().createJobQuery().jobId(jobId).singleResult();
            // skip the cleanup job
            if ("history-cleanup".equals(((JobEntity) job).getJobHandlerType())) {
                return next.execute(command);
            }
            var jobDefinition = configure.getManagementService().createJobDefinitionQuery().jobDefinitionId(job.getJobDefinitionId()).singleResult();
            var activityId = jobDefinition.getActivityId();
            if (activityId != null) {
                activityId = activityId.split("#")[0];
                var modelInstance = configure.getRepositoryService().getBpmnModelInstance(job.getProcessDefinitionId());
                FlowElement flowElement = modelInstance.getModelElementById(activityId);
                var extensionElements = flowElement.getExtensionElements();
                if (extensionElements != null) {
                    var timeoutProperty = extensionElements.getElementsQuery()
                            .filterByType(CamundaProperties.class)
                            .list()
                            .stream()
                            .flatMap(x -> x.getCamundaProperties().stream())
                            .filter(prop -> prop.getCamundaName().equals(TIMEOUT_PROPERTY_NAME))
                            .map(prop -> Long.parseLong(prop.getCamundaValue()))
                            .findFirst();
                    if (timeoutProperty.isPresent()) {
                        timeout = timeoutProperty.get();
                    }
                }
            }
            log.debug("Timeout setting of the job starting point:{}", timeout);
            EnhancementJobLogEntity entity = null;
            if (dbLogEnabled) {
                entity = service.generateInstance((JobEntity) job, timeout);
                try {
                    service.save(entity);
                } catch (Exception e) {
                    log.warn("Unable to save enhancement job log entity: {}", entity, e);
                }
            }

            try {
                var executor = configure.getCommandExecutorTxRequired();
                Future<T> future = timeoutExecutor.submit(() ->
                        executor.execute(new Command<T>() {
                            @Override
                            public T execute(CommandContext commandContext) {
                                var currentThread = Thread.currentThread();
                                String originalName = currentThread.getName();
                                currentThread.setName(threadName);
                                try {
                                    log.debug("Starting job: {}", jobId);
                                    // ✅ CommandContext is available here!
                                    var result = next.execute(command); // this can be ExecuteJobsCmd
                                    log.debug("Completed job: {} in {} ms", jobId, (System.currentTimeMillis() - startTime));
                                    return result;
                                } finally {
                                    currentThread.setName(originalName);
                                }
                            }
                        })
                );
                return future.get(timeout, TimeUnit.MILLISECONDS);

            } catch (TimeoutException te) {
                log.error("Job {} timed out after {}ms", jobId, timeoutMs);

                // Optionally: mark job as failed, unlock, notify
                if (job.getExecutionId() != null) {
                    //configure.getManagementService().setJobRetries(job.getExecutionId(), 0);
                    configure.getRuntimeService().createIncident(
                            "failedJob",
                            job.getExecutionId(),
                            job.getId(),
                            "Job timed out after " + timeoutMs + "ms and may be deadlocked"
                    );

                    configure.getManagementService().suspendJobDefinitionById(job.getJobDefinitionId(), true);
                }
                if (dbLogEnabled) {
                    entity.failed(te.getMessage());
                }
                throw new RuntimeException("Job timed out after " + timeoutMs + "ms and may be deadlocked", te);
            } catch (Exception e) {
                log.error("Error while executing job: {}", jobId, e);
                if (dbLogEnabled) {
                    entity.failed(e.getMessage());
                }
                throw new RuntimeException(e);
            } finally {
                if (dbLogEnabled) {
                    if (!entity.isFailed()) {
                        entity.success();
                    }
                    try {
                        service.update(entity);
                    } catch (Exception e) {
                        log.warn("Unable to update enhancement job log entity: {}", entity, e);
                    }
                }
                MDC.clear(); // clean up
            }
        } else {
            // Pass-through for other commands
            return next.execute(command);
        }
    }
}
