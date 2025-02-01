package com.vider.quantum.engine.delegate.calendar;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class CheckCalendarEventAlreadySynced implements JavaDelegate {

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        int numberOfRecords = 0;
        Object variable = delegateExecution.getVariable("duplicateEventDbResultCurrent");
        if (variable instanceof List) {
            for (Object o : (List) variable) {
                if (o instanceof Map) {
                    Map map = (Map) o;
                    numberOfRecords = Integer.parseInt(
                            String.valueOf(
                                    map.getOrDefault("numberOfRecords", "0")));
                }
            }
        }
        delegateExecution.setVariable("eventAlreadySynced", numberOfRecords > 0 ? "Y" : "N");
    }
}
