package com.vider.quantum.engine.service.automation;

import com.vider.quantum.engine.entities.vider.AutomationBulkSync;
import com.vider.quantum.engine.repository.vider.AutomationBulkSyncRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AutomationBulkSyncService {

    private final AutomationBulkSyncRepository repository;

    public List<AutomationBulkSync> findAll() {
        return repository.findAll();
    }

    public List<AutomationBulkSync> findAllEnabled() {
        return repository.findAllByEnabled("Y");
    }

    public Optional<AutomationBulkSync> findById(Integer id) {
        return repository.findById(id);
    }

    public AutomationBulkSync save(AutomationBulkSync entity) {
        return repository.save(entity);
    }

    public void deleteById(Integer id) {
        repository.deleteById(id);
    }

    public List<AutomationBulkSync> findAllByOrgId(int orgId) {
        return repository.findAllByOrgId(orgId);
    }

    public Optional<AutomationBulkSync> findByOrgId(int orgId) {
        return repository.findByOrgId(orgId);
    }
}
