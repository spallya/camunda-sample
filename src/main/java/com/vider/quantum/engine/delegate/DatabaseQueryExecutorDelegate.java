package com.vider.quantum.engine.delegate;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.hibernate.query.sql.internal.NativeQueryImpl;
import org.hibernate.transform.AliasToEntityMapResultTransformer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseQueryExecutorDelegate implements JavaDelegate {

    private static final String VIDER_ATOM_SCHEMA = "{viderAtomSchema}";
    private static final String CAMUNDA_BUSINESS_KEY = "{camundaBusinessKey}";
    private static final String FOR_LOOP_INDEX = "ForLoopIndex";
    private static final String CURRENT = "Current";
    private static final String PROCESS_ERRORED_OUT = "processErroredOut";
    private static final String ERROR = "Error";
    private static final String CURRENT_INDEX = "currentIndex";
    private static final String UPSERT = "upsert";
    private static final String RESULT_VARIABLE = "resultVariable";
    private static final String QUERY = "query";

    private final EntityManager entityManager;

    @Value("${vider.atom.schema}")
    String viderAtomSchema;

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        String query = String.valueOf(delegateExecution.getVariable(QUERY));
        String resultVariable = String.valueOf(delegateExecution.getVariable(RESULT_VARIABLE));
        String upsert = String.valueOf(delegateExecution.getVariable(UPSERT));
        Object currentIndex = delegateExecution.getVariable(CURRENT_INDEX);
        if (query.contains(CAMUNDA_BUSINESS_KEY)) {
            query = query.replace(CAMUNDA_BUSINESS_KEY, delegateExecution.getProcessBusinessKey());
        }
        if (query.contains(VIDER_ATOM_SCHEMA)) {
            query = query.replace(VIDER_ATOM_SCHEMA, viderAtomSchema);
        }
        try {
            if ("Y".equalsIgnoreCase(upsert)) {
                int numberOfRecords = getExecuteUpdate(query);
                if (currentIndex == null) {
                    delegateExecution.setVariable(resultVariable, numberOfRecords);
                } else {
                    delegateExecution.setVariable(resultVariable+ FOR_LOOP_INDEX +currentIndex, numberOfRecords);
                    delegateExecution.setVariable(resultVariable+ CURRENT, numberOfRecords);
                }
                return;
            }
            NativeQueryImpl nativeQuery = (NativeQueryImpl) entityManager.createNativeQuery(query);
            nativeQuery.setResultTransformer(AliasToEntityMapResultTransformer.INSTANCE);
            List resultList = nativeQuery.getResultList();
            if (currentIndex == null) {
                delegateExecution.setVariable(resultVariable, resultList);
            } else {
                delegateExecution.setVariable(resultVariable+ FOR_LOOP_INDEX +currentIndex, resultList);
                delegateExecution.setVariable(resultVariable+ CURRENT, resultList);
            }
        } catch (Exception ex) {
            log.error(ex.getMessage());
            delegateExecution.setVariable(PROCESS_ERRORED_OUT, ex.getMessage());
            if (currentIndex == null) {
                delegateExecution.setVariable(resultVariable+ ERROR, ex.getMessage());
            } else {
                delegateExecution.setVariable(resultVariable+ FOR_LOOP_INDEX +currentIndex+ ERROR, ex.getMessage());
                delegateExecution.setVariable(resultVariable+ CURRENT, ex.getMessage());
            }
        }
    }

    @Transactional
    private int getExecuteUpdate(String query) {
        NativeQueryImpl nativeQuery = (NativeQueryImpl) entityManager.createNativeQuery(query);
        int numberOfRecords = nativeQuery.executeUpdate();
        return numberOfRecords;
    }
}
