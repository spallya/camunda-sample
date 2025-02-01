package com.vider.quantum.engine.delegate;

import jakarta.persistence.EntityManager;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class LoopHelperDelegate implements JavaDelegate {

    private static final String CURRENT_INDEX = "currentIndex";
    private static final String TOTAL_ELEMENTS = "totalElements";
    private static final String LOOP_THROUGH = "loopThrough";
    private static final String INDEX = "index";
    private static final String END_LOOP = "endLoop";
    private static final String N = "N";
    private static final String Y = "Y";
    @Autowired
    private EntityManager entityManager;

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        Object loopThrough = delegateExecution.getVariable(LOOP_THROUGH);
        int loopSize;
        int current;
        if (loopThrough instanceof List) {
            List<Object> loopObjects = (List<Object>) loopThrough;
            Object totalElements = delegateExecution.getVariable(TOTAL_ELEMENTS);
            if (totalElements == null) {
                totalElements = loopObjects.size();
                loopSize = loopObjects.size();
                delegateExecution.setVariable(TOTAL_ELEMENTS, totalElements);
            } else {
                loopSize = (int) totalElements;
            }
            Object currentIndex = delegateExecution.getVariable(CURRENT_INDEX);
            if (currentIndex == null) {
                currentIndex = 0;
                current = 0;
                delegateExecution.setVariable(CURRENT_INDEX, currentIndex);
            } else {
                current = (int) currentIndex;
                current++;
                delegateExecution.setVariable(CURRENT_INDEX, current);
            }
            if (current < loopSize) {
                int finalCurrent = current;
                loopObjects.forEach(obj -> {
                    Map<String, Object> objectMap = (Map<String, Object>) obj;
                    if(finalCurrent == Integer.parseInt(String.valueOf(objectMap.get(INDEX)))) {
                        objectMap.forEach(delegateExecution::setVariable);
                    }
                });
                delegateExecution.setVariable(END_LOOP, N);
            } else {
                delegateExecution.setVariable(END_LOOP, Y);
                delegateExecution.setVariable("loopExexutionSuccess", Y);
            }

        }

    }
}
