package com.vider.quantum.engine.delegate.automation;

import com.vider.quantum.engine.entities.camunda.AutomationRequest;
import com.vider.quantum.engine.entities.camunda.AutomationServerDetails;
import com.vider.quantum.engine.enums.AutomationRequestStatus;
import com.vider.quantum.engine.service.automation.AutomationRequestService;
import com.vider.quantum.engine.service.automation.AutomationServerDetailsService;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UpdateAutomationRequestStatus implements JavaDelegate {

    private final AutomationRequestService automationRequestService;
    private final AutomationServerDetailsService automationServerDetailsService;

    private final String AUTOMATION_SUCCESS = "Automation Completed";

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        String automationRequestId = String.valueOf(delegateExecution.getVariable("automationRequestId"));
        String serverUrl = String.valueOf(delegateExecution.getVariable("serverUrl"));
        String automationResult = String.valueOf(delegateExecution.getVariable("automationResult"));
        Optional<AutomationRequest> requestById = automationRequestService.getRequestById(automationRequestId);
        requestById.ifPresent(request -> {
            if (AUTOMATION_SUCCESS.equalsIgnoreCase(automationResult)) {
                request.setStatus(AutomationRequestStatus.SUCCESS.name());
            } else {
                request.setStatus(AutomationRequestStatus.FAILURE.name());
            }
            request.setProcessInstanceId(delegateExecution.getProcessInstanceId());
            request.setMessage(automationResult);
            request.setUpdatedBy("SYSTEM");
            automationRequestService.saveOrUpdate(request);
        });
        Optional<AutomationServerDetails> serverByServerUrl = automationServerDetailsService.getServerByServerUrl(serverUrl);
        serverByServerUrl.ifPresent(server -> {
            int currentLoad = server.getCurrentLoad();
            if (currentLoad > 0) {
                currentLoad = currentLoad - 1;
            }
            server.setCurrentLoad(currentLoad);
            automationServerDetailsService.saveOrUpdate(server);
        });

    }
}
