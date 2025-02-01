package com.vider.quantum.engine.repository.vider;

import com.vider.quantum.engine.entities.vider.AutomationBulkSync;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AutomationBulkSyncRepository extends JpaRepository<AutomationBulkSync, Integer> {
    Optional<AutomationBulkSync> findByOrgId(int orgId);

    List<AutomationBulkSync> findAllByEnabled(String enabled);

    List<AutomationBulkSync> findAllByOrgId(int orgId);
}
