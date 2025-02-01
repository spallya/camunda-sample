package com.vider.quantum.engine.service.automation;

import com.vider.quantum.engine.entities.vider.TanClientCredentials;
import com.vider.quantum.engine.repository.vider.TanClientCredentialsRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TanClientCredentialsService {

    private final TanClientCredentialsRepository repository;

    public TanClientCredentialsService(TanClientCredentialsRepository repository) {
        this.repository = repository;
    }

    public TanClientCredentials save(TanClientCredentials credentials) {
        return repository.save(credentials);
    }

    public List<TanClientCredentials> findAll() {
        return repository.findAll();
    }

    public Optional<TanClientCredentials> findById(Integer id) {
        return repository.findById(id);
    }

    public void deleteById(Integer id) {
        repository.deleteById(id);
    }

    public List<TanClientCredentials> findAllByOrganizationId(Integer organizationId) {
        return repository.findAllByOrganizationIdAndStatus(organizationId, TanClientCredentials.Status.ENABLE);
    }
}
