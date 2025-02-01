package com.vider.quantum.engine.service.automation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vider.quantum.engine.dto.MessageDto;
import com.vider.quantum.engine.dto.automation.AutomationSubmitRequestDto;
import com.vider.quantum.engine.dto.automation.Periodicity;
import com.vider.quantum.engine.dto.automation.Schedule;
import com.vider.quantum.engine.entities.camunda.AutomationRequest;
import com.vider.quantum.engine.entities.vider.*;
import com.vider.quantum.engine.enums.AutomationRequestStatus;
import com.vider.quantum.engine.exception.QuantumException;
import com.vider.quantum.engine.service.AutClientCredentialsService;
import com.vider.quantum.engine.service.GstrCredentialsService;
import com.vider.quantum.engine.service.OrganizationService;
import com.vider.quantum.engine.util.CronExpressionBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@Component
@Slf4j
@RequiredArgsConstructor
public class QuantumAutomationService {

    private final SchedulerService schedulerService;
    private final OrganizationService organizationService;
    private final GstrCredentialsService gstrCredentialsService;
    private final AutomationRequestService automationRequestService;
    private final AutomationMachineService automationMachineService;
    private final AutomationBulkSyncService automationBulkSyncService;
    private final TanClientCredentialsService tanClientCredentialsService;
    private final AutClientCredentialsService autClientCredentialsService;

    public Map<String, String> submitRequest(String userId, List<AutomationSubmitRequestDto> submitRequestDtos) {
        List<AutomationMachine> entities = new ArrayList<>();
        Map<String, String> result = new HashMap<>();
        AtomicReference<String> clientId = new AtomicReference<>("");
        submitRequestDtos.forEach(submitRequestDto -> {
            switch (AutomationMachine.SyncType.valueOf(submitRequestDto.getType())) {
                case INCOMETAX:
                    handleIncomeTaxRequests(userId, submitRequestDto, entities, result);
                    clientId.set(submitRequestDto.getAutoCredentialsId());
                    break;
                case GSTR:
                    handleGstrRequests(userId, submitRequestDto, entities, result);
                    clientId.set(submitRequestDto.getGstrCredentialsId());
                    break;
                case TAN:
                    handleTanRequests(userId, submitRequestDto, entities, result);
                    clientId.set(submitRequestDto.getTanCredentialsId());
                    break;
            }
        });
        automationMachineService.saveAll(entities)
                .forEach(entity -> result.put(clientId.get(),
                        "Successfully created request for type " + entity.getType() + " with id: " + entity.getId())
                );
        return result;
    }

    private void handleTanRequests(String userId, AutomationSubmitRequestDto submitRequestDto, List<AutomationMachine> entities, Map<String, String> result) {
        List<AutomationMachine> requestInStatusAndTanCredentialsId = new ArrayList<>();
        if (StringUtils.isNotEmpty(submitRequestDto.getTanCredentialsId())) {
            requestInStatusAndTanCredentialsId = automationMachineService.findRequestInStatusAndTanCredentialsId(
                    List.of(AutomationRequestStatus.INQUEUE.name(), AutomationRequestStatus.PENDING.name()),
                    Integer.parseInt(submitRequestDto.getTanCredentialsId())
            );
        }
        if (CollectionUtils.isEmpty(requestInStatusAndTanCredentialsId)) {
            try {
                entities.add(AutomationMachine.builder()
                        .tanCredentialsId(StringUtils.isNotEmpty(submitRequestDto.getTanCredentialsId()) ?
                                Integer.parseInt(submitRequestDto.getTanCredentialsId()) : 0)
                        .modules(new ObjectMapper().writeValueAsString(submitRequestDto.getModules()))
                        .userId(Integer.parseInt(userId))
                        .type(AutomationMachine.SyncType.TAN)
                        .status(AutomationRequestStatus.INQUEUE.name())
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build());
            } catch (Exception e) {
                result.put(submitRequestDto.getTanCredentialsId(), e.getMessage());
            }
        } else {
            result.put(submitRequestDto.getTanCredentialsId(),
                    "Already an active request present for type TAN with id: " + requestInStatusAndTanCredentialsId.get(0).getId());
        }
    }

    private void handleGstrRequests(String userId, AutomationSubmitRequestDto submitRequestDto, List<AutomationMachine> entities, Map<String, String> result) {
        List<AutomationMachine> requestInStatusAndGstrCredentialsId = new ArrayList<>();
        if (StringUtils.isNotEmpty(submitRequestDto.getGstrCredentialsId())) {
            requestInStatusAndGstrCredentialsId = automationMachineService.findRequestInStatusAndGstrCredentialsId(
                    List.of(AutomationRequestStatus.INQUEUE.name(), AutomationRequestStatus.PENDING.name()),
                    Integer.parseInt(submitRequestDto.getGstrCredentialsId())
            );
        }
        if (CollectionUtils.isEmpty(requestInStatusAndGstrCredentialsId)) {
            try {
                entities.add(AutomationMachine.builder()
                        .gstrCredentialsId(StringUtils.isNotEmpty(submitRequestDto.getGstrCredentialsId()) ?
                                Integer.parseInt(submitRequestDto.getGstrCredentialsId()) : 0)
                        .modules(new ObjectMapper().writeValueAsString(submitRequestDto.getModules()))
                        .userId(Integer.parseInt(userId))
                        .type(AutomationMachine.SyncType.valueOf(submitRequestDto.getType()))
                        .status(AutomationRequestStatus.INQUEUE.name())
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build());
            } catch (Exception e) {
                result.put(submitRequestDto.getGstrCredentialsId(), e.getMessage());
            }
        } else {
            result.put(submitRequestDto.getGstrCredentialsId(),
                    "Already an active request present for type GSTR with id: " + requestInStatusAndGstrCredentialsId.get(0).getId());
        }
    }

    private void handleIncomeTaxRequests(String userId, AutomationSubmitRequestDto submitRequestDto, List<AutomationMachine> entities, Map<String, String> result) {
        List<AutomationMachine> requestInStatusAndAutoCredentialsId = new ArrayList<>();
        if (StringUtils.isNotEmpty(submitRequestDto.getAutoCredentialsId())) {
            requestInStatusAndAutoCredentialsId = automationMachineService.findRequestInStatusAndAutoCredentialsId(
                    List.of(AutomationRequestStatus.INQUEUE.name(), AutomationRequestStatus.PENDING.name()),
                    Integer.parseInt(submitRequestDto.getAutoCredentialsId())
            );
        }
        if (CollectionUtils.isEmpty(requestInStatusAndAutoCredentialsId)) {
            try {
                entities.add(AutomationMachine.builder()
                        .autoCredentialsId(StringUtils.isNotEmpty(submitRequestDto.getAutoCredentialsId()) ?
                                Integer.parseInt(submitRequestDto.getAutoCredentialsId()) : 0)
                        .modules(new ObjectMapper().writeValueAsString(submitRequestDto.getModules()))
                        .userId(Integer.parseInt(userId))
                        .type(AutomationMachine.SyncType.valueOf(submitRequestDto.getType()))
                        .status(AutomationRequestStatus.INQUEUE.name())
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build());
            } catch (Exception e) {
                result.put(submitRequestDto.getAutoCredentialsId(), e.getMessage());
            }
        } else {
            result.put(submitRequestDto.getAutoCredentialsId(),
                    "Already an active request present for type INCOMETAX with id: " + requestInStatusAndAutoCredentialsId.get(0).getId());
        }
    }

    public MessageDto cancelRequest(String requestId, String userId) {
        AtomicReference<String> message = new AtomicReference<>();
        Optional<AutomationRequest> requestById = automationRequestService.getRequestById(requestId);
        if (requestById.isPresent()) {
            AutomationRequest automationRequest = requestById.get();
            if (!AutomationRequestStatus.INQUEUE.name().equalsIgnoreCase(automationRequest.getStatus())) {
                message.set("Automation request can not be cancelled as request is in status: " + automationRequest.getStatus());
            } else {
                automationRequest.setStatus(AutomationRequestStatus.CANCELLED.name());
                automationRequest.setUpdatedBy(userId);
                automationRequest.setMessage("Request is cancelled");
                automationRequestService.saveOrUpdate(automationRequest);
                message.set("Automation request cancelled successfully with id: " + requestId);
            }
        } else {
            throw new QuantumException("Automation request not found with id: " + requestId);
        }
        return MessageDto.builder()
                .message(message.get())
                .build();
    }

    public MessageDto getRequestStatus(String requestId) {
        AtomicReference<String> message = new AtomicReference<>();
        AtomicReference<String> status = new AtomicReference<>();
        automationMachineService.findById(Integer.parseInt(requestId)).ifPresentOrElse(
                automationRequest -> {
                    status.set(automationRequest.getStatus());
                    message.set(automationRequest.getRemarks());
                },
                () -> {
                    throw new QuantumException("Automation request not found with id: " + requestId);
                }
        );
        return MessageDto.builder()
                .message(message.get())
                .status(status.get())
                .build();
    }

    public List<AutomationMachine> getAllRequests(String clientId) {
        return StringUtils.isNotEmpty(clientId) ? automationMachineService.findAllByAutoCredentialsId(Integer.parseInt(clientId)) : automationMachineService.findAll();
    }

    public Boolean bulkSync(String userId, AutomationSubmitRequestDto submitRequestDto) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        String cronExpression;
        String scheduleJson;
        String autoCredIds = "";
        String gstrIds = "";
        String tanIds = "";
        if (organizationService.findById(submitRequestDto.getOrgId()).isEmpty()) {
            throw new QuantumException("Invalid org id, no organization found with given id: " + submitRequestDto.getOrgId());
        }
        List<AutomationBulkSync> allyOrgId = automationBulkSyncService.findAllByOrgId(submitRequestDto.getOrgId());
        AutomationBulkSync entity = new AutomationBulkSync();
        if (CollectionUtils.isNotEmpty(allyOrgId)) {
            entity = allyOrgId.stream()
                    .filter(e -> e.getType().name().equalsIgnoreCase(submitRequestDto.getType()))
                    .findFirst()
                    .orElse(new AutomationBulkSync());
        }
        if (CollectionUtils.isNotEmpty(submitRequestDto.getSchedules())) {
            scheduleJson = objectMapper.writeValueAsString(submitRequestDto.getSchedules());
            StringJoiner joiner = new StringJoiner("|");
            submitRequestDto.getSchedules().forEach(schedule ->
                    joiner.add(CronExpressionBuilder.createCronExpression(schedule)));
            cronExpression = joiner.toString();
        } else {
            scheduleJson = objectMapper.writeValueAsString(Schedule.builder()
                    .amPm("am")
                    .hour(1)
                    .periodicity(Periodicity.DAILY)
                    .build()
            );
            cronExpression = "0 0 1 * * *";
        }
        autoCredIds = getAutoCredIds(submitRequestDto, autoCredIds);
        gstrIds = getGstrIds(submitRequestDto, gstrIds);
        tanIds = getTanIds(submitRequestDto, tanIds);
        entity.setOrgId(submitRequestDto.getOrgId());
        entity.setSchedule(scheduleJson);
        entity.setCronExpression(cronExpression);
        entity.setClientIds(autoCredIds);
        entity.setEnabled("Y");
        entity.setGstrCredentialsId(gstrIds);
        entity.setTanCredentialsId(tanIds);
        entity.setModules(objectMapper.writeValueAsString(submitRequestDto.getModules()));
        entity.setUserId(Integer.parseInt(userId));
        entity.setType(AutomationBulkSync.SyncType.valueOf(submitRequestDto.getType()));
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        automationBulkSyncService.save(entity);
        schedulerService.scheduleTask(entity);
        return true;
    }

    private String getTanIds(AutomationSubmitRequestDto submitRequestDto, String tanIds) {
        if (AutomationBulkSync.SyncType.TAN.name().equalsIgnoreCase(submitRequestDto.getType())) {
            StringJoiner joiner = new StringJoiner(",");
            List<TanClientCredentials> allByOrganizationId = tanClientCredentialsService.findAllByOrganizationId(submitRequestDto.getOrgId());
            if (CollectionUtils.isNotEmpty(allByOrganizationId)) {
                allByOrganizationId.forEach(tanCredentials -> joiner.add(String.valueOf(tanCredentials.getId())));
                tanIds = joiner.toString();
            }
        }
        return tanIds;
    }

    private String getGstrIds(AutomationSubmitRequestDto submitRequestDto, String gstrIds) {
        if (AutomationBulkSync.SyncType.GSTR.name().equalsIgnoreCase(submitRequestDto.getType())) {
            StringJoiner joiner = new StringJoiner(",");
            List<GstrCredentials> allByOrganizationId = gstrCredentialsService.findAllByOrganizationId(submitRequestDto.getOrgId());
            if (CollectionUtils.isNotEmpty(allByOrganizationId)) {
                allByOrganizationId.forEach(gstrCredentials -> joiner.add(String.valueOf(gstrCredentials.getId())));
                gstrIds = joiner.toString();
            }
        }
        return gstrIds;
    }

    private String getAutoCredIds(AutomationSubmitRequestDto submitRequestDto, String autoCredIds) {
        if (AutomationBulkSync.SyncType.INCOMETAX.name().equalsIgnoreCase(submitRequestDto.getType())) {
            StringJoiner joiner = new StringJoiner(",");
            List<AutClientCredentials> allByOrganizationId = autClientCredentialsService.findAllByOrganizationId(submitRequestDto.getOrgId());
            if (CollectionUtils.isNotEmpty(allByOrganizationId)) {
                allByOrganizationId.forEach(autClientCredentials -> joiner.add(String.valueOf(autClientCredentials.getId())));
                autoCredIds = joiner.toString();
            }
        }
        return autoCredIds;
    }

    public Boolean deleteOrgBulkSyncRequest(int orgId) {
        Optional<AutomationBulkSync> byOrgId = automationBulkSyncService.findByOrgId(orgId);
        byOrgId.ifPresent(entity -> {
            schedulerService.unscheduleTask(entity.getId());
            automationBulkSyncService.deleteById(entity.getId());
        });
        return true;
    }

    public Boolean enableDisableOrgBulkSyncRequest(int orgId, String status, String type) {
        List<AutomationBulkSync> allByOrgId = automationBulkSyncService.findAllByOrgId(orgId);
        if (CollectionUtils.isEmpty(allByOrgId)) {
            throw new QuantumException("No records found for orgid: " + orgId);
        }
        AutomationBulkSync entity = allByOrgId.stream()
                .filter(e -> e.getType().name().equalsIgnoreCase(type))
                .findFirst()
                .orElse(null);
        if (entity != null) {
            if ("enable".equalsIgnoreCase(status)) {
                log.info("Enabling auto bulk syn request with id: " + entity.getId() + " for org: " + orgId);
                entity.setEnabled("Y");
                schedulerService.scheduleTask(entity);
            } else if ("disable".equalsIgnoreCase(status)) {
                log.info("Disabling auto bulk syn request with id: " + entity.getId() + " for org: " + orgId);
                entity.setEnabled("N");
                schedulerService.unscheduleTask(entity.getId());
            }
            automationBulkSyncService.save(entity);
            return true;
        } else {
            throw new QuantumException("No records found for orgid: " + orgId + " and type: " + type);
        }
    }

    public AutomationBulkSync getAutomationBulkSyncRecord(int orgId, String type) {
        List<AutomationBulkSync> allByOrgId = automationBulkSyncService.findAllByOrgId(orgId);
        if (CollectionUtils.isEmpty(allByOrgId)) {
            throw new QuantumException("No records found for orgid: " + orgId);
        }
        AutomationBulkSync entity = allByOrgId.stream()
                .filter(e -> e.getType().name().equalsIgnoreCase(type))
                .findFirst()
                .orElse(null);
        if (entity == null) {
            throw new QuantumException("No records found for orgid: " + orgId + " and type: " + type);
        }
        return entity;
    }

    public Boolean updateBulkSyncRequest(int orgId) {
        List<AutomationBulkSync> allByOrgId = automationBulkSyncService.findAllByOrgId(orgId);
        if (CollectionUtils.isNotEmpty(allByOrgId)) {
            allByOrgId.forEach(request -> {
                String autoCredIds = "";
                String gstrIds = "";
                String tanIds = "";
                AutomationSubmitRequestDto submitRequestDto = AutomationSubmitRequestDto.builder()
                        .orgId(orgId)
                        .type(request.getType().name())
                        .build();
                autoCredIds = getAutoCredIds(submitRequestDto, autoCredIds);
                gstrIds = getGstrIds(submitRequestDto, gstrIds);
                tanIds = getTanIds(submitRequestDto, tanIds);
                request.setClientIds(autoCredIds);
                request.setGstrCredentialsId(gstrIds);
                request.setTanCredentialsId(tanIds);
                request.setUpdatedAt(LocalDateTime.now());
                automationBulkSyncService.save(request);
                schedulerService.unscheduleTask(request.getId());
                schedulerService.scheduleTask(request);
            });
        }
        return null;
    }
}
