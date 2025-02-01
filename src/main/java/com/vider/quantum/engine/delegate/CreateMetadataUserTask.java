package com.vider.quantum.engine.delegate;

import lombok.extern.apachecommons.CommonsLog;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component
public class CreateMetadataUserTask implements JavaDelegate {
    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        delegateExecution.getProcessEngineServices()
                .getRuntimeService()
                .createMessageCorrelation("CreateMetadataUserTask")
                .setVariable("metadataCurrentFieldName",
                        String.valueOf(delegateExecution.getVariable("metadataFieldName")))
                .setVariable("parentProcessInstanceId", delegateExecution.getProcessInstanceId())
                .correlate();
    }
}
