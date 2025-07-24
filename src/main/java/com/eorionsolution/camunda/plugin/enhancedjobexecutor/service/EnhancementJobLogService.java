package com.eorionsolution.camunda.plugin.enhancedjobexecutor.service;

import com.eorionsolution.camunda.plugin.enhancedjobexecutor.domain.EnhancementJobLogEntity;
import org.camunda.bpm.BpmPlatform;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.commons.utils.EnsureUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnhancementJobLogService {
    private static final Logger log = LoggerFactory.getLogger(EnhancementJobLogService.class);
    private final ProcessEngineConfigurationImpl config;

    public EnhancementJobLogService() {
        config = (ProcessEngineConfigurationImpl) BpmPlatform.getDefaultProcessEngine().getProcessEngineConfiguration();
    }

    public EnhancementJobLogService(ProcessEngine processEngine) {
        config = (ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration();
    }

    public EnhancementJobLogService(ProcessEngineConfigurationImpl config) {
        this.config = config;
    }

    public EnhancementJobLogService(boolean enable) {
        config = null;
    }

    public void save(final EnhancementJobLogEntity entity) {
        Command<Integer> cmd = commandContext -> {
            var sqlSession = commandContext.getDbSqlSession().getSqlSession();
            return sqlSession.insert("EnhancementJobLogMapper.save", entity);
        };
        var result = config.getCommandExecutorTxRequiresNew().execute(cmd);
        log.debug("EnhancementJobLogMapper save result: {}", result);
    }

    public void update(final EnhancementJobLogEntity entity) {
        EnsureUtil.ensureNotNull("EnhancementJobLog#id", entity.getId());
        Command<Integer> cmd = commandContext -> {
            var sqlSession = commandContext.getDbSqlSession().getSqlSession();
            return sqlSession.update("EnhancementJobLogMapper.update", entity);
        };
        var result = config.getCommandExecutorTxRequiresNew().execute(cmd);
        log.debug("EnhancementJobLogMapper update result: {}", result);
    }

    public EnhancementJobLogEntity generateInstance(final JobEntity jobEntity, long timeoutMs) {
        return EnhancementJobLogEntity.getInstance(jobEntity, timeoutMs);
    }

}
