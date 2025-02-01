package com.vider.quantum.engine.repository.vider;

import com.vider.quantum.engine.entities.vider.AutClientCredentials;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AutClientCredentialsRepository extends JpaRepository<AutClientCredentials, Integer> {
    List<AutClientCredentials> findAllByOrganizationId(Integer organizationId);

    List<AutClientCredentials> findAllByOrganizationIdAndStatus(Integer organizationId, AutClientCredentials.Status status);
}
