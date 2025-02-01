package com.vider.quantum.engine.delegate.automation;

import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class TriggerAutomation implements JavaDelegate {

    private final RestTemplate restTemplate;

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        String serverUrl = String.valueOf(delegateExecution.getVariable("serverUrl"));
        String clientId = String.valueOf(delegateExecution.getVariable("clientId"));
        List<String> requests = (List<String>) (delegateExecution.getVariable("requests"));
        String automationResult = "";
        String automationUrl = serverUrl + "/" + clientId;
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(automationUrl);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(Map.of("requests", requests));
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    builder.build().toUri(),
                    HttpMethod.valueOf("POST"),
                    entity,
                    String.class);
            automationResult = response.getBody();

        } catch (Exception e) {
            if (e instanceof HttpClientErrorException) {
                HttpClientErrorException htce = (HttpClientErrorException) e;
                automationResult = htce.getResponseBodyAsString();
            } else {
                automationResult = e.getLocalizedMessage();
            }
        }
        delegateExecution.setVariable("automationResult", automationResult);
    }
}
