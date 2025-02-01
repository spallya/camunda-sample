package com.vider.quantum.engine.service.automation;

import com.vider.quantum.engine.entities.vider.AutomationServer;
import com.vider.quantum.engine.repository.vider.AutomationServerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AutomationServerService {

    private final AutomationServerRepository repository;

    @Autowired
    public AutomationServerService(AutomationServerRepository repository) {
        this.repository = repository;
    }

    public List<AutomationServer> findAll() {
        return repository.findAll();
    }

    public void saveAll(List<AutomationServer> requests) {
        repository.saveAll(requests);
    }

    public Optional<AutomationServer> findById(Integer id) {
        return repository.findById(id);
    }

    public List<AutomationServer> findAllByServerStatus(String serverStatus) {
        return repository.findAllByServerStatus(serverStatus);
    }

    public AutomationServer save(AutomationServer automationServer) {
        return repository.save(automationServer);
    }

    public void deleteById(Integer id) {
        repository.deleteById(id);
    }
}
