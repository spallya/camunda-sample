package com.vider.quantum.engine.delegate.automation;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component
public class ViderAutomationWebhook implements JavaDelegate {
    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {

    }
}
