package com.vider.quantum.engine.scheduler;

import com.vider.quantum.engine.entities.vider.AutomationBulkSync;
import com.vider.quantum.engine.entities.vider.AutomationMachine;
import com.vider.quantum.engine.entities.vider.AutomationServer;
import com.vider.quantum.engine.enums.AutomationRequestStatus;
import com.vider.quantum.engine.service.automation.AutomationBulkSyncService;
import com.vider.quantum.engine.service.automation.AutomationMachineService;
import com.vider.quantum.engine.service.automation.AutomationServerService;
import com.vider.quantum.engine.service.automation.SchedulerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "vider.external.required", name = "automation")
@Slf4j
public class AutomationScheduler {

    private final Set<String> alreadyScheduledBulkSyncRequests = new HashSet<>();

    private final SchedulerService schedulerService;
    private final AutomationServerService automationServerService;
    private final AutomationMachineService automationMachineService;
    private final AutomationBulkSyncService automationBulkSyncService;

    @Scheduled(cron = "0 */1 * * * *")
    public void processRequestsInQueue() {
        List<AutomationMachine> requests = automationMachineService.getRequestsByStatus(AutomationRequestStatus.INQUEUE.name());
        List<AutomationServer> availableServers = automationServerService.findAllByServerStatus("available");
        if (CollectionUtils.isNotEmpty(requests) && CollectionUtils.isNotEmpty(availableServers)) {
            int loopLength = Math.min(requests.size(), availableServers.size());
            for (int i = 0; i < loopLength; i++) {
                AutomationMachine automationMachine = requests.get(i);
                AutomationServer automationServer = availableServers.get(i);
                log.info("Assigning automation request with id: " + automationMachine.getId() + " to server with name: " + automationServer.getMachineName());
                automationMachine.setMachineName(automationServer.getMachineName());
                automationMachine.setStatus(AutomationRequestStatus.PENDING.name());
                automationMachine.setUpdatedAt(LocalDateTime.now());
                automationServer.setServerStatus("busy");
                automationServer.setUpdatedAt(LocalDateTime.now());
            }
            automationMachineService.saveAll(requests);
            automationServerService.saveAll(availableServers);
        } else {
            log.info("No automation requests found for scheduling or no server is free to take new request");
        }
    }

    @Scheduled(cron = "0 */1 * * * *")
    public void scheduleBulkSyncRequests() {
        List<AutomationBulkSync> requests = automationBulkSyncService.findAllEnabled();
        if (CollectionUtils.isNotEmpty(requests)) {
            log.info("Found scheduleBulkSyncRequests to be processed count: " + requests.size());
            requests.forEach(automationBulkSync -> {
                try {
                    log.info("processing scheduleBulkSyncRequests for request: " + automationBulkSync.getId());
                    String key = automationBulkSync.getId() + "|" + automationBulkSync.getUpdatedAt();
                    if (!alreadyScheduledBulkSyncRequests.contains(key)) {
                        log.info("Scheduling bulk sync request: " + automationBulkSync);
                        schedulerService.scheduleTask(automationBulkSync);
                        alreadyScheduledBulkSyncRequests.add(key);
                    }
                } catch (Exception ex) {
                    log.error("Exception in scheduleBulkSyncRequests occurred for id: " + automationBulkSync.getId() + ex.getLocalizedMessage());
                }
            });
        } else {
            log.info("No new bulk sync automation requests found for scheduling");
        }
    }
}
