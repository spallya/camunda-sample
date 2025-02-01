package com.vider.quantum.engine.service;

import com.vider.quantum.engine.entities.vider.Organization;
import com.vider.quantum.engine.repository.vider.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationRepository repository;

    public List<Organization> findAll() {
        return repository.findAll();
    }

    public Optional<Organization> findById(Integer id) {
        return repository.findById(id);
    }

    public Organization save(Organization organization) {
        return repository.save(organization);
    }

    public void deleteById(Integer id) {
        repository.deleteById(id);
    }
}
