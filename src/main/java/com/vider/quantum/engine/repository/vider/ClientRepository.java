package com.vider.quantum.engine.repository.vider;

import com.vider.quantum.engine.entities.vider.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClientRepository extends JpaRepository<Client, Integer> {

    List<Client> findByOrganizationIdAndStatus(Integer organizationId, Client.Status status);
}
