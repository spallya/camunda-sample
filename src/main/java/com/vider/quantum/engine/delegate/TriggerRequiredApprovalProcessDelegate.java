package com.vider.quantum.engine.delegate;

import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TriggerRequiredApprovalProcessDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        String approvalProcessId = String.valueOf(delegateExecution.getVariable("approvalProcessId"));
        delegateExecution.getProcessEngineServices()
                .getRuntimeService()
                .createMessageCorrelation(approvalProcessId)
                .setVariable("parentProcessInstanceId", delegateExecution.getProcessInstanceId())
                .correlate();
    }
}
