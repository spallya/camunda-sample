package com.vider.quantum.engine.service.automation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vider.quantum.engine.entities.vider.AutomationBulkSync;
import com.vider.quantum.engine.entities.vider.AutomationMachine;
import com.vider.quantum.engine.entities.vider.OrganizationPreferences;
import com.vider.quantum.engine.enums.AutomationRequestStatus;
import com.vider.quantum.engine.service.OrganizationPreferencesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class SchedulerService {

    private final TaskScheduler taskScheduler;
    private final AutomationMachineService automationMachineService;
    private final OrganizationPreferencesService organizationPreferencesService;
    private final Map<Integer, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();


    public void scheduleTask(AutomationBulkSync automationBulkSync) {
        unscheduleTask(automationBulkSync.getId());
        String cronExpression = automationBulkSync.getCronExpression();
        String[] cronExpressions = cronExpression.split("\\|");
        for (String expression : cronExpressions) {
            ScheduledFuture<?> future = taskScheduler.schedule(
                    () -> performTask(automationBulkSync),
                    new CronTrigger(expression)
            );
            scheduledTasks.put(automationBulkSync.getId(), future);
        }
    }

    public void unscheduleTask(Integer id) {
        ScheduledFuture<?> future = scheduledTasks.get(id);
        if (future != null) {
            future.cancel(true);
            scheduledTasks.remove(id);
        }
    }

    private void performTask(AutomationBulkSync task) {
        log.info("Submitting automation request for the bulk sync record: " + task);
        Optional<OrganizationPreferences> organizationPreferencesOptional =  organizationPreferencesService.findByOrganizationId(task.getOrgId());
        OrganizationPreferences organizationPreferences = organizationPreferencesOptional.orElse(new OrganizationPreferences());
        String automationConfig = organizationPreferences.getAutomationConfig();
        Map<String, String> automationCofigMap = new HashMap<>();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            automationCofigMap = objectMapper.readValue(automationConfig, Map.class);

        } catch (Exception e) {
            log.error(e.getMessage());
        }
        List<AutomationMachine> entities = new ArrayList<>();
        String clientIds = task.getClientIds();
        if (StringUtils.isNotEmpty(clientIds) &&
                "yes".equalsIgnoreCase(automationCofigMap.getOrDefault("incomeTax", ""))) {
            String[] autoCredIds = clientIds.split(",");
            for (String autoCredId : autoCredIds) {
                entities.add(AutomationMachine.builder()
                        .autoCredentialsId(Integer.parseInt(autoCredId))
                        .modules(task.getModules())
                        .userId(task.getUserId())
                        .type(AutomationMachine.SyncType.valueOf(task.getType().name()))
                        .status(AutomationRequestStatus.INQUEUE.name())
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build());
            }
        }
        String gstrCredentialsId = task.getGstrCredentialsId();
        if (StringUtils.isNotEmpty(gstrCredentialsId) &&
                "yes".equalsIgnoreCase(automationCofigMap.getOrDefault("gstr", ""))) {
            String[] gstrIds = gstrCredentialsId.split(",");
            for (String gstrId : gstrIds) {
                entities.add(AutomationMachine.builder()
                        .gstrCredentialsId(Integer.parseInt(gstrId))
                        .modules(task.getModules())
                        .userId(task.getUserId())
                        .type(AutomationMachine.SyncType.valueOf(task.getType().name()))
                        .status(AutomationRequestStatus.INQUEUE.name())
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build());
            }
        }
        String tanCredentialsId = task.getTanCredentialsId();
        if (StringUtils.isNotEmpty(tanCredentialsId) &&
                "yes".equalsIgnoreCase(automationCofigMap.getOrDefault("tan", ""))) {
            String[] tanIds = tanCredentialsId.split(",");
            for (String tanId : tanIds) {
                entities.add(AutomationMachine.builder()
                        .tanCredentialsId(Integer.parseInt(tanId))
                        .modules(task.getModules())
                        .userId(task.getUserId())
                        .type(AutomationMachine.SyncType.valueOf(task.getType().name()))
                        .status(AutomationRequestStatus.INQUEUE.name())
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build());
            }
        }
        if (CollectionUtils.isNotEmpty(entities)) {
            automationMachineService.saveAll(entities);
        }
    }

}
