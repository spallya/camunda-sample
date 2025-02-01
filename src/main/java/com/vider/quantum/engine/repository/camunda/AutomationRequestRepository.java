package com.vider.quantum.engine.repository.camunda;

import com.vider.quantum.engine.entities.camunda.AutomationRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AutomationRequestRepository extends JpaRepository<AutomationRequest, String> {

    List<AutomationRequest> findAllByClientId(String clientId);
    List<AutomationRequest> findAllByStatus(String status);
}
