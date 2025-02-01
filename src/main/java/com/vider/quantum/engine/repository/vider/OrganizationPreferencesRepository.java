package com.vider.quantum.engine.repository.vider;

import com.vider.quantum.engine.entities.vider.OrganizationPreferences;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrganizationPreferencesRepository extends JpaRepository<OrganizationPreferences, Integer> {
    Optional<OrganizationPreferences> findByOrganizationId(Integer organizationId);
}
