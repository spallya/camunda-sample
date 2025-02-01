package com.vider.quantum.engine.service;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CamundaVariableService {

    public Object getValue(DelegateExecution delegateExecution, String source, String key) {
        Object sourceObject = delegateExecution.getVariable(source);
        if (sourceObject != null) {
            String[] keys = key.split("\\.");
            for(int i = 0; i < keys.length; i++) {
                Map<String, Object> sourceObjectMap = (Map<String, Object>) sourceObject;
                if (i == keys.length - 1) {
                    return sourceObjectMap.get(keys[i]);
                } else {
                    sourceObject = sourceObjectMap.get(keys[i]);
                }
            }
        }
        return "random";
    }

}
