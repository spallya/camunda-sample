package com.vider.quantum.engine.service.automation;

import com.vider.quantum.engine.entities.camunda.AutomationServerDetails;
import com.vider.quantum.engine.repository.camunda.AutomationServerDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AutomationServerDetailsService {

    @Autowired
    private AutomationServerDetailsRepository automationServerDetailsRepository;

    public List<AutomationServerDetails> getAllServers() {
        return automationServerDetailsRepository.findAll();
    }

    public AutomationServerDetails saveOrUpdate(AutomationServerDetails serverDetails) {
        return automationServerDetailsRepository.save(serverDetails);
    }

    public void saveAll(List<AutomationServerDetails> servers) {
        automationServerDetailsRepository.saveAll(servers);
    }

    public Optional<AutomationServerDetails> getServerById(Long id) {
        return automationServerDetailsRepository.findById(id);
    }

    public Optional<AutomationServerDetails> getServerByServerUrl(String serverUrl) {
        return automationServerDetailsRepository.findServerByServerUrl(serverUrl);
    }
    public void deleteServer(Long id) {
        automationServerDetailsRepository.deleteById(id);
    }

    public List<AutomationServerDetails> findAllActiveUsableServers() {
        return automationServerDetailsRepository.findAllActiveUsableServers();
    }
}
