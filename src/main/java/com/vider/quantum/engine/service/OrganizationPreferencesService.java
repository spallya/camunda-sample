package com.vider.quantum.engine.service;

import com.vider.quantum.engine.entities.vider.OrganizationPreferences;
import com.vider.quantum.engine.repository.vider.OrganizationPreferencesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OrganizationPreferencesService {

    private final OrganizationPreferencesRepository repository;

    @Autowired
    public OrganizationPreferencesService(OrganizationPreferencesRepository repository) {
        this.repository = repository;
    }

    public List<OrganizationPreferences> findAll() {
        return repository.findAll();
    }

    public Optional<OrganizationPreferences> findById(Integer id) {
        return repository.findById(id);
    }

    public OrganizationPreferences save(OrganizationPreferences preferences) {
        return repository.save(preferences);
    }

    public void deleteById(Integer id) {
        repository.deleteById(id);
    }

    public Optional<OrganizationPreferences> findByOrganizationId(Integer organizationId) {
        return repository.findByOrganizationId(organizationId);
    }
}