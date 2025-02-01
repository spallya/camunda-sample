package com.vider.quantum.engine.service.automation;

import com.vider.quantum.engine.entities.vider.AutomationMachine;
import com.vider.quantum.engine.repository.vider.AutomationMachineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AutomationMachineService {

    private final AutomationMachineRepository repository;

    public List<AutomationMachine> findAll() {
        return repository.findAll();
    }

    public List<AutomationMachine> findAllByAutoCredentialsId(Integer autoCredentialsId) {
        return repository.findAllByAutoCredentialsId(autoCredentialsId);
    }

    public Optional<AutomationMachine> findById(Integer id) {
        return repository.findById(id);
    }

    public AutomationMachine save(AutomationMachine automationMachine) {
        return repository.save(automationMachine);
    }

    public void deleteById(Integer id) {
        repository.deleteById(id);
    }

    public List<AutomationMachine> saveAll(List<AutomationMachine> requests) {
        return repository.saveAll(requests);
    }

    public List<AutomationMachine> getRequestsByStatus(String status) {
        return repository.findAllByStatus(status);
    }

    public List<AutomationMachine> findRequestInStatusAndAutoCredentialsId(Collection<String> statuses, Integer autoCredentialsId) {
        return repository.findAllByStatusInAndAutoCredentialsId(statuses, autoCredentialsId);
    }

    public List<AutomationMachine> findRequestInStatusAndGstrCredentialsId(Collection<String> statuses, Integer gstrCredentialsId) {
        return repository.findAllByStatusInAndGstrCredentialsId(statuses, gstrCredentialsId);
    }

    public List<AutomationMachine> findRequestInStatusAndTanCredentialsId(List<String> statuses, Integer tanCredentialsId) {
        return repository.findAllByStatusInAndTanCredentialsId(statuses, tanCredentialsId);
    }
}
