package com.eorionsolution.camunda.plugin.enhancedjobexecutor;

import com.eorionsolution.camunda.plugin.enhancedjobexecutor.service.EnhancementJobLogService;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.session.Configuration;
import org.camunda.bpm.engine.impl.cfg.AbstractProcessEnginePlugin;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cmd.JobTrackingInterceptor;

import java.util.ArrayList;

public class EnhancedJobExecutorPlugin extends AbstractProcessEnginePlugin {
    private long timeoutMs;
    private boolean enableDBLog;

    public EnhancedJobExecutorPlugin(long timeoutMs, boolean enableDBLog) {
        this.timeoutMs = timeoutMs;
        this.enableDBLog = enableDBLog;
    }

    public EnhancedJobExecutorPlugin() {
    }

    public long getTimeoutMs() {
        return timeoutMs;
    }

    public void setTimeoutMs(long timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public boolean isEnableDBLog() {
        return enableDBLog;
    }

    public void setEnableDBLog(boolean enableDBLog) {
        this.enableDBLog = enableDBLog;
    }

    @Override
    public void preInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
        var customInterceptors = processEngineConfiguration.getCustomPostCommandInterceptorsTxRequired();
        if (customInterceptors == null) {
            customInterceptors = new ArrayList<>();
        }
        if (enableDBLog) {
            customInterceptors.add(new JobTrackingInterceptor(processEngineConfiguration,
                    new EnhancementJobLogService(processEngineConfiguration), timeoutMs));
        } else {
            customInterceptors.add(new JobTrackingInterceptor(processEngineConfiguration, timeoutMs));
        }
        processEngineConfiguration.setCustomPostCommandInterceptorsTxRequired(customInterceptors);
    }

    public void postInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
        Configuration configuration = processEngineConfiguration.getSqlSessionFactory().getConfiguration();
        XMLMapperBuilder xmlParser = new XMLMapperBuilder(EnhancedJobExecutorPlugin.class.getResourceAsStream("/mapper/EnhancementJobLogMapper.xml"), configuration, this.getClass().getCanonicalName(), configuration.getSqlFragments());
        xmlParser.parse();
    }
}
