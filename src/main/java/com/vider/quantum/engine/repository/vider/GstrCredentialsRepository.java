package com.vider.quantum.engine.repository.vider;

import com.vider.quantum.engine.entities.vider.GstrCredentials;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GstrCredentialsRepository extends JpaRepository<GstrCredentials, Integer> {

    List<GstrCredentials> findAllByOrganizationId(Integer organizationId);

    List<GstrCredentials> findAllByOrganizationIdAndStatus(Integer organizationId, GstrCredentials.Status status);
}
