package com.vider.quantum.engine.delegate;

import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class RestApiDelegate implements JavaDelegate {

    private final RestTemplate restTemplate;
    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        Map<String, Object> processVariables = delegateExecution.getVariables();
        String httpMethod = String.valueOf(processVariables.get("httpMethod"));
        String url = String.valueOf(processVariables.get("url"));
        String resultVariable = String.valueOf(processVariables.get("resultVariable"));
        Map<String, Object> queryParams = getMapFromVariable(processVariables,"queryParams");
        Map<String, Object> pathVariables = getMapFromVariable(processVariables,"pathVariables");
        Map<String, Object> headers = getMapFromVariable(processVariables,"headers");
        Object requestBody = processVariables.get("requestBody");
        Object currentIndex = delegateExecution.getVariable("currentIndex");
        HttpHeaders httpHeaders = getHttpHeaders(headers);
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(url);
        if (!CollectionUtils.isEmpty(queryParams)) {
            queryParams.forEach(uriComponentsBuilder::queryParam);
        }
        try {
            ResponseEntity<Object> responseEntity = restTemplate.exchange(uriComponentsBuilder.buildAndExpand(pathVariables).toUri(),
                    HttpMethod.resolve(httpMethod),
                    StringUtils.isEmpty(requestBody) ?
                            new HttpEntity<>(httpHeaders) : new HttpEntity<>(requestBody, httpHeaders),
                    Object.class);
            if (currentIndex == null) {
                delegateExecution.setVariable(resultVariable, responseEntity.getBody());
            } else {
                delegateExecution.setVariable(resultVariable+"ForLoopIndex"+currentIndex, responseEntity.getBody());
                delegateExecution.setVariable(resultVariable+"Current", responseEntity.getBody());
            }

        } catch (Exception ex) {
            if (currentIndex == null) {
                delegateExecution.setVariable(resultVariable+"Error", ex.getMessage());
            } else {
                delegateExecution.setVariable(resultVariable+"ForLoopIndex"+currentIndex+"Error", ex.getMessage());
                delegateExecution.setVariable(resultVariable+"Current", ex.getMessage());
            }


        }
    }

    private static HttpHeaders getHttpHeaders(Map<String, Object> headers) {
        HttpHeaders httpHeaders = new HttpHeaders();
        if (!CollectionUtils.isEmpty(headers)) {
            headers.forEach((key, value) -> httpHeaders.add(key, String.valueOf(value)));
        }
        httpHeaders.add("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36");
        return httpHeaders;
    }

    private Map<String, Object> getMapFromVariable(Map<String, Object> processVariables, String variableName) {
        Object value = processVariables.get(variableName);
        if (value instanceof Map) {
            return (Map<String, Object>) value;
        }
        return new HashMap<>();
    }

}
