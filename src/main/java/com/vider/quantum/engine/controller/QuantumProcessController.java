package com.vider.quantum.engine.controller;

import com.vider.quantum.engine.constants.QuantumConstants;
import com.vider.quantum.engine.dto.ProcessDetailsDto;
import com.vider.quantum.engine.dto.ProcessStartedResponse;
import com.vider.quantum.engine.dto.StartProcessDto;
import com.vider.quantum.engine.service.QuantumService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/vider/quantum/api")
@RequiredArgsConstructor
public class QuantumProcessController {

    private final QuantumService quantumService;

    @PostMapping("/process")
    public ResponseEntity<ProcessStartedResponse> startProcess(@RequestBody StartProcessDto startProcessDto) {

        ProcessStartedResponse processStartedResponse = quantumService.startProcess(startProcessDto);
        return new ResponseEntity<>(processStartedResponse, HttpStatus.OK);
    }

    @GetMapping("/process/{processInstanceId}")
    public ResponseEntity<ProcessDetailsDto> getProcessDetails(@PathVariable String processInstanceId) {
        return new ResponseEntity<>(quantumService.getProcessDetails(processInstanceId), HttpStatus.OK);
    }

    @PutMapping("/process/{processInstanceId}")
    public ResponseEntity<Boolean> updateProcess(@PathVariable String processInstanceId,
                                                 @RequestBody Map<String, Object> metaData) {
        return new ResponseEntity<>(quantumService.updateProcessVariables(processInstanceId, metaData), HttpStatus.OK);
    }

    @DeleteMapping("/process/{processInstanceId}/variables")
    public ResponseEntity<Boolean> patchDeleteProcessVariables(@PathVariable String processInstanceId,
                                                               @RequestBody Map<String, Object> metaData) {
        return new ResponseEntity<>(quantumService.patchDeleteProcessVariables(processInstanceId, metaData), HttpStatus.OK);
    }

    @DeleteMapping("/process/{processInstanceId}")
    public ResponseEntity<Boolean> softDeleteProcess(@PathVariable String processInstanceId) {
        return new ResponseEntity<>(quantumService.updateProcessVariables(processInstanceId,
                Collections.singletonMap(QuantumConstants.SOFT_DELETED, QuantumConstants.Y)), HttpStatus.OK);
    }

}
