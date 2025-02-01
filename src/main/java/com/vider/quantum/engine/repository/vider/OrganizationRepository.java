package com.vider.quantum.engine.repository.vider;

import com.vider.quantum.engine.entities.vider.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Integer> {
}
