package com.vider.quantum.engine.service;

import com.vider.quantum.engine.entities.vider.Client;
import com.vider.quantum.engine.repository.vider.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository repository;

    public List<Client> findAll() {
        return repository.findAll();
    }

    public Optional<Client> findById(Integer id) {
        return repository.findById(id);
    }

    public Client save(Client client) {
        return repository.save(client);
    }

    public void deleteById(Integer id) {
        repository.deleteById(id);
    }

    public List<Client> findByOrganizationIdAndStatus(Integer organizationId, Client.Status status) {
        return repository.findByOrganizationIdAndStatus(organizationId, status);
    }
}
