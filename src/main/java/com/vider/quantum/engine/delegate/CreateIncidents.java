package com.vider.quantum.engine.delegate;

import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.runtime.Incident;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreateIncidents implements JavaDelegate {

    private final RuntimeService runtimeService;

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        Incident incident1 = runtimeService.createIncident("metadataFeeder", delegateExecution.getId(), "");
        Incident incident = delegateExecution.createIncident("metadataFeeder", "", "sample incident");
        System.out.println();
    }
}
