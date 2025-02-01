package com.vider.quantum.engine.controller;

import com.vider.quantum.engine.dto.StartProcessDto;
import com.vider.quantum.engine.dto.SyncStatusDto;
import com.vider.quantum.engine.service.QuantumService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/vider/quantum/api/calendar")
@RequiredArgsConstructor
@Slf4j
public class QuantumCalendarController {

    private final QuantumService quantumService;

    @GetMapping("/events")
    public ResponseEntity<HttpStatus> getEvents(@RequestParam(value = "sdate", required = false) String sdate,
                                                @RequestParam(value = "edate", required = false) String edate,
                                                @RequestParam(value = "pageSize", required = false) String pageSize,
                                                @RequestParam(value = "pageToken",
                                                        required = false, defaultValue = "") String pageToken,
                                                @RequestHeader(value = "X-ACCESS-TOKEN") String accessToken,
                                                @RequestHeader(value = "X-DB-USER-ID") String dbUserId,
                                                @RequestHeader(value = "X-DB-ORG-ID") String dbOrgId,
                                                @RequestHeader(value = "X-USER-ID") String userId) {
        Map<String, Object> metadata = new HashMap();
        metadata.put("sdate", sdate);
        metadata.put("edate", edate);
        metadata.put("pageSize", pageSize);
        metadata.put("pageToken", pageToken);
        metadata.put("accessToken", accessToken);
        metadata.put("calendarSyncInitiatedForUser", userId);
        metadata.put("dbUserId",dbUserId);
        metadata.put("dbOrgId",dbOrgId);
        quantumService.startProcess(StartProcessDto.builder()
                        .metaData(metadata)
                        .processKey("syncCalendarEvents")
                .build());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/sync/status/{userId}")
    public ResponseEntity<SyncStatusDto> syncStatus(@PathVariable String userId) {
        SyncStatusDto statusForUser = quantumService.getCalendarSyncStatusForUser(userId);
        return new ResponseEntity<>(statusForUser, HttpStatus.OK);
    }

}