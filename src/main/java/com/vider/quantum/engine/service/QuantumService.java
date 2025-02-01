package com.vider.quantum.engine.service;

import com.vider.quantum.engine.dto.*;
import com.vider.quantum.engine.exception.QuantumException;
import com.vider.quantum.engine.helper.AutoFillResolverHelper;
import com.vider.quantum.engine.helper.TasksHelper;
import com.vider.quantum.engine.resolver.AutoFillResolverType;
import com.vider.quantum.engine.util.VariableDocUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.vider.quantum.engine.constants.QuantumConstants.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class QuantumService {

    private final CamundaService camundaService;
    private static final String TEMPLATE_DOC_METADATA = "templateDocMetadata";

    public ProcessStartedResponse startProcess(StartProcessDto startProcessDto) {
        try {
            ProcessInstance processInstance = camundaService.triggerProcessInstance(startProcessDto);
            if (StringUtils.isNotBlank(startProcessDto.getOwner()) || CollectionUtils.isNotEmpty(startProcessDto.getTaskOwners())) {
                updateOwnerOfCreatedUserTasks(startProcessDto, processInstance.getProcessInstanceId());
            }
            return ProcessStartedResponse.builder()
                    .processInstanceId(processInstance.getProcessInstanceId())
                    .businessKey(processInstance.getBusinessKey())
                    .build();
        } catch (Exception ex) {
            log.error(ex.getMessage());
            throw new QuantumException(ex.getMessage());
        }
    }

    private void updateOwnerOfCreatedUserTasks(StartProcessDto startProcessDto, String processInstanceId) {
        String owner = startProcessDto.getOwner();
        List<String> taskOwners = startProcessDto.getTaskOwners();
        if (CollectionUtils.isNotEmpty(taskOwners)) {
            StringJoiner sj = new StringJoiner("|");
            taskOwners.forEach(sj::add);
            owner = sj.toString();
        }
        ProcessDetailsDto processDetails = getProcessDetails(processInstanceId);
        if (!CollectionUtils.isEmpty(processDetails.getTasks())) {
            String finalOwner = owner;
            processDetails.getTasks().forEach(taskDetailsDto ->
                    camundaService.assignTaskOwner(taskDetailsDto.getId(), finalOwner));
        }
    }

    public ProcessDetailsDto getProcessDetails(String processInstanceId) {
        Map<String, Object> processVariables;
        List<TaskDetailsDto> tasks;
        ProcessInstanceDto processInstance = camundaService.getProcessInstance(processInstanceId);
        if (processInstance.isCompleted()) {
            processVariables = camundaService.getProcessVariablesOfHistoricProcess(processInstanceId);
        } else {
            processVariables = camundaService.getProcessVariablesOfProcess(processInstanceId);
        }
        if (processVariables.containsKey(SOFT_DELETED)
                && Y.equalsIgnoreCase((String) processVariables.get(SOFT_DELETED))) {
            throw new IllegalStateException("Process you are trying to get details of is deleted. " + processInstanceId);
        }
        tasks = camundaService.getAllTasksForProcess(processInstanceId);
        TasksHelper.patchTasksData(tasks, processVariables);
        handleTemplateMetadataDocIfPresent(processVariables);
        return ProcessDetailsDto.builder()
                .data(processVariables)
                .tasks(CollectionUtils.isEmpty(tasks) ?
                        Collections.emptyList() : TasksHelper.filterSnapshotsTasks(tasks, processVariables))
                .rootProcessInstanceId(processInstance.getRootProcessInstanceId())
                .processInstanceId(processInstance.getProcessInstanceId())
                .completed(processInstance.isCompleted())
                .build();

    }

    private static void handleTemplateMetadataDocIfPresent(Map<String, Object> processVariables) {
        if (processVariables.containsKey(TEMPLATE_DOC_METADATA)) {
            TemplateDocMetadataDto templateDocMetadataDto =
                    (TemplateDocMetadataDto) processVariables.get(TEMPLATE_DOC_METADATA);
            Map<String, String> updatedVariablesMap = new HashMap<>();
            templateDocMetadataDto.getSections().forEach(section -> {
                if (CollectionUtils.isNotEmpty(section.getMetadataFields())) {
                    section.getMetadataFields().forEach(field ->
                            updatedVariablesMap.put(field.getFieldName(), field.getFieldValue()));
                }
            });
            templateDocMetadataDto.getSections().forEach(section -> {
                if (CollectionUtils.isNotEmpty(section.getMetadataFields())) {
                    section.getMetadataFields().forEach(field -> {
                        if (updatedVariablesMap.containsKey(field.getFieldName())) {
                            field.setFieldValue(updatedVariablesMap.get(field.getFieldName()));
                        }
                    });
                }
            });
            AutoFillResolverHelper.executeResolvers(
                    List.of(AutoFillResolverType.CONVERSION, AutoFillResolverType.DYNAMIC_OPTIONS),
                    templateDocMetadataDto,
                    updatedVariablesMap);
            VariableDocUtil.updateElementsSequencing(templateDocMetadataDto);
        }
    }

    public boolean assignTask(String taskId, String userId) {
        return camundaService.assignTask(taskId, userId);
    }

    public boolean completeCamundaTask(TaskCompletionDto inputTaskCompletionDto) {
        final boolean[] result = {true};
        if (CollectionUtils.isEmpty(inputTaskCompletionDto.getTaskIds())) {
            inputTaskCompletionDto.setTaskIds(new ArrayList<>());
            inputTaskCompletionDto.getTaskIds().add(inputTaskCompletionDto.getTaskId());
        }
        inputTaskCompletionDto.getTaskIds().forEach(task -> {
            TaskCompletionDto taskCompletionDto = TaskCompletionDto.builder()
                    .comments(inputTaskCompletionDto.getComments())
                    .lastUpdatedOn(inputTaskCompletionDto.getLastUpdatedOn())
                    .taskId(task)
                    .userId(inputTaskCompletionDto.getUserId())
                    .status(inputTaskCompletionDto.getStatus())
                    .build();
            Map<String, TaskCompletionDto> tasks = new HashMap<>();
            List<TaskSnapshot> taskSnapshots = new ArrayList<>();
            String processInstanceId = camundaService.getProcessInstanceIdOfTask(taskCompletionDto.getTaskId());
            Map<String, Object> variables = camundaService.getProcessVariablesOfProcess(processInstanceId);
            if (variables.containsKey(PARENT_PROCESS_INSTANCE_ID)) {
                processInstanceId = String.valueOf(variables.get(PARENT_PROCESS_INSTANCE_ID));
                variables = camundaService.getProcessVariablesOfProcess(processInstanceId);
            }
            boolean completed = camundaService.completeTask(taskCompletionDto);
            if (variables.containsKey(TASKS_METADATA)) {
                tasks = (Map<String, TaskCompletionDto>) variables.get(TASKS_METADATA);
            }
            taskCompletionDto.setLastUpdatedOn(new Date().toString());
            tasks.put(taskCompletionDto.getTaskId(), taskCompletionDto);
            List<TaskDetailsDto> allTasksForProcess = camundaService.getAllTasksForProcess(processInstanceId);
            if (CollectionUtils.isNotEmpty(allTasksForProcess)
                    && DECLINED.equalsIgnoreCase(taskCompletionDto.getStatus())
                    && completed
            ) {
                TasksHelper.performAutoRejectionsAndSnapshotsUpdate(taskCompletionDto, tasks, taskSnapshots, processInstanceId,
                        variables, allTasksForProcess, camundaService);
            } else if (completed) {
                variables.put(TASKS_METADATA, tasks);
                camundaService.updateProcessVariables(processInstanceId, variables);
                TasksHelper.completeHoldProcessTaskIfAllOtherTasksAreCompleted(allTasksForProcess, camundaService);
            }
            result[0] = result[0] && completed;
        });
        return result[0];
    }


    public boolean updateCamundaTask(TaskUpdationDto taskUpdationDto) {
        return camundaService.updateCamundaTask(taskUpdationDto);
    }

    public boolean updateProcessVariablesForVariableDocument(String processInstanceId, Map<String, Object> updatedProcessVariables) {
        return camundaService.updateProcessVariables(processInstanceId, updatedProcessVariables);
    }

    public boolean updateProcessVariables(String processInstanceId, Map<String, Object> updatedProcessVariables) {
        Map<String, Object> finalVariables = new HashMap<>();
        ProcessDetailsDto processDetails = getProcessDetails(processInstanceId);
        Map<String, Object> data = processDetails.getData();
        if (MapUtils.isNotEmpty(data)) {
            updatedProcessVariables.forEach((key, value) -> {
                if (data.containsKey(key)) {
                    Object o = data.get(key);
                    if (o instanceof List && value instanceof List) {
                        List updatedValues = (List) value;
                        List currentValues = (List) o;
                        List collected = (List) updatedValues.stream()
                                .filter(v -> !currentValues.contains(v))
                                .collect(Collectors.toList());
                        currentValues.addAll(collected);
                        finalVariables.put(key, currentValues);
                    } else {
                        finalVariables.put(key, value);
                    }
                } else {
                    finalVariables.put(key, value);
                }
            });
        }

        return camundaService.updateProcessVariables(processInstanceId,
                MapUtils.isNotEmpty(finalVariables) ? finalVariables : updatedProcessVariables);
    }

    public boolean addUser(UserDetailsDto userDetailsDto) {
        List<String> filteredGroups = new ArrayList<>();
        List<String> groups = userDetailsDto.getGroups();
        if (CollectionUtils.isNotEmpty(groups)) {
            groups.forEach(group -> {
                Pattern p = Pattern.compile("[^a-zA-Z0-9]");
                Matcher m = p.matcher(group);
                group = m.replaceAll("");
                group = group.replaceAll(" ", "");
                filteredGroups.add(group);
            });
        }
        if (CollectionUtils.isNotEmpty(filteredGroups)) {
            userDetailsDto.setGroups(filteredGroups);
        }
        return camundaService.createUserIfNotExists(userDetailsDto);
    }

    public boolean deleteUser(String userId) {
        return camundaService.deleteUser(userId);
    }

    public boolean deleteGroup(String groupId) {
        return camundaService.deleteGroup(groupId);
    }

    public boolean createMembership(String userId, String groupId) {
        return camundaService.createMembership(userId, groupId);
    }

    public boolean deleteMembership(String userId, String groupId) {
        return camundaService.deleteMembership(userId, groupId);
    }

    public List<UserDetailsDto> fetchAllUsers(String groupId) {
        List<User> users = camundaService.fetchAllUsers(groupId);
        return users.stream()
                .map(this::mapToUserDto)
                .toList();
    }

    public List<String> fetchAllGroups(String userId) {
        List<Group> groups = camundaService.fetchAllGroups(userId);
        return groups.stream()
                .map(Group::getId)
                .collect(Collectors.toList());
    }

    public UserDetailsDto fetchUser(String userId) {
        User user = camundaService.fetchUser(userId);
        return mapToUserDto(user);
    }

    private UserDetailsDto mapToUserDto(User user) {
        String encryptedFirstName = user.getFirstName();
        String encryptedLastName = user.getLastName();
        String firstName = encryptedFirstName;
        String roles = "";
        String lastName = encryptedLastName;
        String imageUrl = "";
        if (encryptedFirstName.contains("|")) {
            String[] split = encryptedFirstName.split("\\|");
            if (split.length > 0) {
                firstName = split[0];
                if (split.length > 1) {
                    roles = split[1];
                    if (split.length > 2) {
                        for (int i = 2; i < split.length; i++) {
                            roles = roles.trim() + " | " + split[i].trim();
                        }
                    }
                }
            }
        }
        if (encryptedLastName.contains("|")) {
            String[] split = encryptedLastName.split("\\|");
            if (split.length > 0) {
                lastName = split[0];
                if (split.length > 1) {
                    imageUrl = split[1];
                }
            }
        }
        if (StringUtils.isNotEmpty(firstName)) firstName = firstName.replace("|", "");
        if (StringUtils.isNotEmpty(lastName)) lastName = lastName.replace("|", "");
        return UserDetailsDto.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .imageUrl(imageUrl)
                .groups(fetchAllGroups(user.getId()))
                .roles(roles)
                .firstName(firstName)
                .lastName(lastName)
                .build();
    }

    public List<TaskDetailsDto> fetchAllTasksForUser(String userId, String operation) {
        List<TaskDetailsDto> tasks;
        tasks = camundaService.fetchAllTasksForUser(userId, operation);
        return tasks;
    }

    public SyncStatusDto getCalendarSyncStatusForUser(String userId) {
        String message;
        boolean exists = false;
        try {
            exists = camundaService.processExistsWithVariables(CALENDAR_SYNC_INITIATED_FOR_USER, userId);
            if (exists) {
                message = CALENDAR_SYNC_IS_IN_PROGRESS;
            } else {
                message = CALENDAR_SYNC_IS_COMPLETED;
            }
        } catch (Exception ex) {
            log.error(ex.getMessage());
            message = ex.getMessage();
        }
        return SyncStatusDto.builder()
                .completed(!exists)
                .message(message)
                .build();
    }

    public List<TaskDetailsDto> fetchAllTaskForUserOfStatus(String userId, String status) {
        List<TaskDetailsDto> tasks;
        tasks = camundaService.fetchAllTaskForUserOfStatus(userId, status);
        return tasks;
    }

    public Boolean patchDeleteProcessVariables(String processInstanceId, Map<String, Object> updatedProcessVariables) {
        Map<String, Object> finalVariables = new HashMap<>();
        ProcessDetailsDto processDetails = getProcessDetails(processInstanceId);
        Map<String, Object> data = processDetails.getData();
        if (MapUtils.isNotEmpty(data)) {
            updatedProcessVariables.forEach((key, value) -> {
                if (data.containsKey(key)) {
                    Object o = data.get(key);
                    if (o instanceof List && value instanceof List) {
                        List updatedValues = (List) value;
                        List currentValues = (List) o;
                        List collected = (List) updatedValues.stream()
                                .filter(v -> currentValues.contains(v))
                                .collect(Collectors.toList());
                        currentValues.removeAll(collected);
                        finalVariables.put(key, currentValues);
                    } else {
                        finalVariables.put(key, value);
                    }
                } else {
                    finalVariables.put(key, value);
                }
            });
        }
        return camundaService.updateProcessVariables(processInstanceId,
                MapUtils.isNotEmpty(finalVariables) ? finalVariables : updatedProcessVariables);
    }

    public UserDetailsDto updateUser(String userId, UserDetailsDto userDetailsDto) {
        User user = camundaService.fetchUser(userId);
        if (user == null) {
            userDetailsDto.setUserId(userId);
            camundaService.createUserIfNotExists(userDetailsDto);
            user = camundaService.fetchUser(userId);
        }
        user.setEmail(userDetailsDto.getEmail());
        user.setFirstName(userDetailsDto.getFirstName() + "|" + userDetailsDto.getRoles());
        user.setLastName(userDetailsDto.getLastName() + "|" + userDetailsDto.getImageUrl());
        List<Group> existingGroups = camundaService.fetchAllGroups(userId);
        if (CollectionUtils.isNotEmpty(existingGroups)) {
            existingGroups.forEach(group -> camundaService.deleteMembership(userId, group.getId()));
        }
        List<String> updatedGroups = userDetailsDto.getGroups();
        if (CollectionUtils.isNotEmpty(updatedGroups)) {
            updatedGroups.forEach(groupId -> camundaService.createMembership(userId, groupId));
        }
        camundaService.updateUser(user);
        return mapToUserDto(user);
    }

    public boolean updateUsers(List<UserDetailsDto> userDetailsDtos) {
        for (UserDetailsDto user : userDetailsDtos) {
            updateUser(user.getUserId(), user);
        }
        return true;
    }

    public Boolean replaceGroupIds(String oldGroupId, String newGroupId, List<String> userIds) {
        List<UserDetailsDto> userDetailsDtos = fetchAllUsers(oldGroupId);
        for(UserDetailsDto user : userDetailsDtos) {
            if (CollectionUtils.isEmpty(userIds) || userIds.contains(user.getUserId())) {
                deleteMembership(user.getUserId(), oldGroupId);
                createMembership(user.getUserId(), newGroupId);
            }
        }
        return true;
    }

    public boolean updateOwners(StartProcessDto startProcessDto, String processInstanceId) {
        updateOwnerOfCreatedUserTasks(startProcessDto, processInstanceId);
        return true;
    }
}
