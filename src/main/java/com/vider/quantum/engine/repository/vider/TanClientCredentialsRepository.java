package com.vider.quantum.engine.repository.vider;

import com.vider.quantum.engine.entities.vider.TanClientCredentials;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TanClientCredentialsRepository extends JpaRepository<TanClientCredentials, Integer> {
    List<TanClientCredentials> findAllByOrganizationId(Integer organizationId);

    List<TanClientCredentials> findAllByOrganizationIdAndStatus(Integer organizationId, TanClientCredentials.Status status);
}
