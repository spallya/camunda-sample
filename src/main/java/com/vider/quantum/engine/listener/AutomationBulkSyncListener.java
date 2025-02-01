package com.vider.quantum.engine.listener;

import com.vider.quantum.engine.entities.vider.AutomationBulkSync;
import com.vider.quantum.engine.service.automation.SchedulerService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AutomationBulkSyncListener {

    private final SchedulerService schedulerService;

    @PersistenceContext
    private EntityManager entityManager;

    @EventListener
    @Transactional
    public void onPostPersist(AutomationBulkSync entity) {
        entityManager.refresh(entity); // To ensure the entity is fully loaded
        schedulerService.scheduleTask(entity);
    }
}
