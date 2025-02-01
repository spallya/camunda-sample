package com.vider.quantum.engine.service.automation;

import com.vider.quantum.engine.entities.camunda.AutomationRequest;
import com.vider.quantum.engine.repository.camunda.AutomationRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AutomationRequestService {

    private final AutomationRequestRepository requestRepository;

    public List<AutomationRequest> getAllRequests() {
        return requestRepository.findAll();
    }

    public AutomationRequest saveOrUpdate(AutomationRequest request) {
        return requestRepository.save(request);
    }

    public void saveAll(List<AutomationRequest> requests) {
        requestRepository.saveAll(requests);
    }

    public Optional<AutomationRequest> getRequestById(String id) {
        return requestRepository.findById(id);
    }

    public void deleteRequest(String id) {
        requestRepository.deleteById(id);
    }

    public List<AutomationRequest> getRequestsByClientId(String clientId) {
        return requestRepository.findAllByClientId(clientId);
    }

    public List<AutomationRequest> getRequestsByStatus(String status) {
        return requestRepository.findAllByStatus(status);
    }
}
