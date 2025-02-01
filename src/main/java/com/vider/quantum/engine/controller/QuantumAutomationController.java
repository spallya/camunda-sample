package com.vider.quantum.engine.controller;

import com.vider.quantum.engine.dto.MessageDto;
import com.vider.quantum.engine.dto.automation.AutomationSubmitRequestDto;
import com.vider.quantum.engine.entities.vider.AutomationBulkSync;
import com.vider.quantum.engine.entities.vider.AutomationMachine;
import com.vider.quantum.engine.service.automation.QuantumAutomationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/vider/quantum/api/automation")
@RequiredArgsConstructor
@Slf4j
public class QuantumAutomationController {

    private final QuantumAutomationService quantumAutomationService;

    @PostMapping
    public ResponseEntity<Map<String, String>> submitRequest(@RequestHeader("X-USER-ID") String userId,
                                                             @RequestBody List<AutomationSubmitRequestDto> submitRequestDtos) {
        return new ResponseEntity<>(quantumAutomationService.submitRequest(userId, submitRequestDtos), HttpStatus.CREATED);
    }

    @PostMapping("/bulk/sync")
    public ResponseEntity<Boolean> bulkSync(@RequestHeader("X-USER-ID") String userId,
                                                             @RequestBody AutomationSubmitRequestDto submitRequestDtos) throws Exception {
        return new ResponseEntity<>(quantumAutomationService.bulkSync(userId, submitRequestDtos), HttpStatus.CREATED);
    }

    @PutMapping("/bulk/sync/{orgId}")
    public ResponseEntity<Boolean> updateBulkSyncRequest(@PathVariable int orgId) throws Exception {
        return new ResponseEntity<>(quantumAutomationService.updateBulkSyncRequest(orgId), HttpStatus.CREATED);
    }

    @DeleteMapping("/bulk/sync/{orgId}")
    public ResponseEntity<Boolean> deleteOrgBulkSyncRequest(@PathVariable int orgId) throws Exception {
        return new ResponseEntity<>(quantumAutomationService.deleteOrgBulkSyncRequest(orgId), HttpStatus.OK);
    }



    @PutMapping("/bulk/sync/{orgId}/{status}/{type}")
    public ResponseEntity<Boolean> enableDisableOrgBulkSyncRequest(@PathVariable int orgId,
                                                                   @PathVariable String status,
                                                                   @PathVariable String type) throws Exception {
        return new ResponseEntity<>(quantumAutomationService.enableDisableOrgBulkSyncRequest(orgId, status, type), HttpStatus.OK);
    }

    @GetMapping("/bulk/sync/{orgId}/{type}")
    public ResponseEntity<AutomationBulkSync> getAutomationBulkSyncRecord(@PathVariable int orgId,
                                                                              @PathVariable String type) throws Exception {
        return new ResponseEntity<>(quantumAutomationService.getAutomationBulkSyncRecord(orgId, type), HttpStatus.OK);
    }

    @GetMapping("/status/{requestId}")
    public ResponseEntity<MessageDto> getRequestStatus(@PathVariable("requestId") String requestId) {
        return new ResponseEntity<>(quantumAutomationService.getRequestStatus(requestId), HttpStatus.OK);
    }

    @GetMapping({"", "/{clientId}"})
    public ResponseEntity<List<AutomationMachine>> getAllRequests(@PathVariable(value = "clientId", required = false) String clientId) {
        return new ResponseEntity<>(quantumAutomationService.getAllRequests(clientId), HttpStatus.OK);
    }

    @DeleteMapping("/{requestId}")
    public ResponseEntity<MessageDto> cancelRequest(@PathVariable("requestId") String requestId,
                                                    @RequestHeader("X-USER-ID") String userId) {
        return new ResponseEntity<>(quantumAutomationService.cancelRequest(requestId, userId), HttpStatus.OK);
    }
}
