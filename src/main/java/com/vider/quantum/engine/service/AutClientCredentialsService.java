package com.vider.quantum.engine.service;

import com.vider.quantum.engine.entities.vider.AutClientCredentials;
import com.vider.quantum.engine.repository.vider.AutClientCredentialsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AutClientCredentialsService {

    private final AutClientCredentialsRepository repository;

    public List<AutClientCredentials> findAll() {
        return repository.findAll();
    }

    public Optional<AutClientCredentials> findById(Integer id) {
        return repository.findById(id);
    }

    public AutClientCredentials save(AutClientCredentials credentials) {
        return repository.save(credentials);
    }

    public void deleteById(Integer id) {
        repository.deleteById(id);
    }

    public List<AutClientCredentials> findAllByOrganizationId(Integer organizationId) {
        return repository.findAllByOrganizationIdAndStatus(organizationId, AutClientCredentials.Status.ENABLE);
    }
}
