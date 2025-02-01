package com.vider.quantum.engine.service;

import com.vider.quantum.engine.dto.*;
import com.vider.quantum.engine.exception.QuantumException;
import com.vider.quantum.engine.helper.TasksHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

import static com.vider.quantum.engine.constants.QuantumConstants.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class CamundaService {

    private final TaskService taskService;
    private final RuntimeService runtimeService;
    private final HistoryService historyService;
    private final IdentityService identityService;

    private TaskDetailsDto mapToTaskDto(Task camundaTask) {
        String assigneeName = "";
        String ownerName = "";
        String assigneeRoles = "";
        String assigneeGroups = "";
        String ownerRoles = "";
        String ownerGroups = "";
        String ownerImageUrl = "";
        String assigneeImageUrl = "";
        List<TaskUser> taskOwners = new ArrayList<>();
        if (StringUtils.isNotEmpty(camundaTask.getOwner()) && !camundaTask.getOwner().contains("|")) {
            User user = fetchUser(camundaTask.getOwner());
            ownerName = getUserFullName(user);
            assigneeGroups = getUserGroups(user);
            ownerImageUrl = getUserImageUrl(user);
            ownerRoles = getUserRoles(user);
        } else if (StringUtils.isNotEmpty(camundaTask.getOwner()) && camundaTask.getOwner().contains("|")) {
            String[] ownerIds = camundaTask.getOwner().split("\\|");
            for (String ownerId : ownerIds) {
                User user = fetchUser(ownerId);
                String townerName = getUserFullName(user);
                String tassigneeGroups = getUserGroups(user);
                String townerImageUrl = getUserImageUrl(user);
                String townerRoles = getUserRoles(user);
                taskOwners.add(TaskUser.builder()
                                .ownerImageUrl(townerImageUrl)
                                .ownerRoles(townerRoles)
                                .owner(ownerId)
                                .ownerGroups(tassigneeGroups)
                                .ownerName(townerName)
                        .build());
            }

        }
        if (StringUtils.isNotEmpty(camundaTask.getAssignee())) {
            User user = fetchUser(camundaTask.getAssignee());
            assigneeName = getUserFullName(user);
            assigneeGroups = getUserGroups(user);
            assigneeImageUrl = getUserImageUrl(user);
            assigneeRoles = getUserRoles(user);
        }
        return TaskDetailsDto.builder()
                .id(camundaTask.getId())
                .name(camundaTask.getName())
                .owner(camundaTask.getOwner())
                .ownerName(ownerName)
                .ownerRoles(ownerRoles)
                .ownerGroups(ownerGroups)
                .ownerImageUrl(ownerImageUrl)
                .taskOwners(taskOwners)
                .completed(false)
                .assignee(camundaTask.getAssignee())
                .assigneeName(assigneeName)
                .assigneeRoles(assigneeRoles)
                .assigneeGroups(assigneeGroups)
                .assigneeImageUrl(assigneeImageUrl)
                .description(camundaTask.getDescription())
                .parentTaskId(camundaTask.getParentTaskId())
                .dueDate(camundaTask.getDueDate())
                .followUpDate(camundaTask.getFollowUpDate())
                .createTime(camundaTask.getCreateTime())
                .priority(camundaTask.getPriority())
                .processInstanceId(camundaTask.getProcessInstanceId())
                .build();
    }

    private String getUserGroups(User user) {
        StringJoiner sj = new StringJoiner(", ");
        List<Group> groups = fetchAllGroups(user.getId());
        if (CollectionUtils.isNotEmpty(groups)) {
            groups.forEach(group -> sj.add(group.getId()));
        }
        return sj.toString();
    }

    private static String getUserFullName(User user) {
        String firstName =  getFirstPartOfEncryptedString(user.getFirstName());
        String lastName = getFirstPartOfEncryptedString(user.getLastName());
        return (StringUtils.isNotEmpty(firstName) ? firstName : "")
                + " "
                + (StringUtils.isNotEmpty(lastName) ? lastName : "");
    }

    private static String getFirstPartOfEncryptedString(String encryptedLastName) {
        if (encryptedLastName.contains("|")
                && (encryptedLastName.split("\\|").length > 0)) {
                encryptedLastName = encryptedLastName.split("\\|")[0];

        }
        return encryptedLastName;
    }

    private static String getUserImageUrl(User user) {
        String encryptedLastName = user.getLastName();
        String imageUrl = "";
        if (encryptedLastName.contains("|")) {
            if (encryptedLastName.split("\\|").length > 1) {
                imageUrl = encryptedLastName.split("\\|")[1];
            }
        }
        return StringUtils.isNotEmpty(imageUrl) ? imageUrl : "";
    }

    private static String getUserRoles(User user) {
        String encryptedFirstName = user.getFirstName();
        String roles = "";
        if (encryptedFirstName.contains("|") && (encryptedFirstName.split("\\|").length > 1)) {
                String[] split = encryptedFirstName.split("\\|");
                roles = split[1];
                if (split.length > 2) {
                    for (int i = 2; i < split.length; i++) {
                        roles = roles.trim() + " | " + split[i].trim();
                    }
                }

        }
        return StringUtils.isNotEmpty(roles) ? roles : "";
    }

    private TaskDetailsDto mapToTaskDto(HistoricTaskInstance camundaTask) {
        String assigneeName = "";
        String ownerName = "";
        String assigneeRoles = "";
        String assigneeGroups = "";
        String ownerRoles = "";
        String ownerGroups = "";
        String ownerImageUrl = "";
        String assigneeImageUrl = "";
        List<TaskUser> taskOwners = new ArrayList<>();
        if (StringUtils.isNotEmpty(camundaTask.getOwner()) && !camundaTask.getOwner().contains("|")) {
            User user = fetchUser(camundaTask.getOwner());
            ownerName = getUserFullName(user);
            assigneeGroups = getUserGroups(user);
            ownerImageUrl = getUserImageUrl(user);
            ownerRoles = getUserRoles(user);
        } else if (StringUtils.isNotEmpty(camundaTask.getOwner()) && camundaTask.getOwner().contains("|")) {
            String[] ownerIds = camundaTask.getOwner().split("\\|");
            for (String ownerId : ownerIds) {
                User user = fetchUser(ownerId);
                String townerName = getUserFullName(user);
                String tassigneeGroups = getUserGroups(user);
                String townerImageUrl = getUserImageUrl(user);
                String townerRoles = getUserRoles(user);
                taskOwners.add(TaskUser.builder()
                        .ownerImageUrl(townerImageUrl)
                        .ownerRoles(townerRoles)
                        .owner(ownerId)
                        .ownerGroups(tassigneeGroups)
                        .ownerName(townerName)
                        .build());
            }

        }
        if (StringUtils.isNotEmpty(camundaTask.getAssignee())) {
            User user = fetchUser(camundaTask.getAssignee());
            assigneeName = getUserFullName(user);
            assigneeGroups = getUserGroups(user);
            assigneeRoles = getUserRoles(user);
            assigneeImageUrl = getUserImageUrl(user);
        }
        return TaskDetailsDto.builder()
                .id(camundaTask.getId())
                .name(camundaTask.getName())
                .owner(camundaTask.getOwner())
                .ownerName(ownerName)
                .ownerRoles(ownerRoles)
                .ownerGroups(ownerGroups)
                .ownerImageUrl(ownerImageUrl)
                .taskOwners(taskOwners)
                .completed(true)
                .assignee(camundaTask.getAssignee())
                .assigneeName(assigneeName)
                .assigneeRoles(assigneeRoles)
                .assigneeGroups(assigneeGroups)
                .assigneeImageUrl(assigneeImageUrl)
                .description(camundaTask.getDescription())
                .parentTaskId(camundaTask.getParentTaskId())
                .dueDate(camundaTask.getDueDate())
                .followUpDate(camundaTask.getFollowUpDate())
                .priority(camundaTask.getPriority())
                .processInstanceId(camundaTask.getProcessInstanceId())
                .build();
    }

    public ProcessInstance triggerProcessInstance(StartProcessDto startProcessDto) {
        String owner = startProcessDto.getOwner();
        if (StringUtils.isNotBlank(owner) && !userExists(owner)) {
            throw new QuantumException(USER + owner + DOES_NOT_EXIST_PLEASE_CREATE_USER_IN_QUANTUM);
        }
        Map<String, Object> metaData = startProcessDto.getMetaData();
        if (!metaData.containsKey(APPROVAL_PROCESS_ID)) {
            metaData.put(APPROVAL_PROCESS_ID, " ");
        }
        metaData.put(OWNER_SMALL_CASE, owner);
        return runtimeService.startProcessInstanceByKey(
                startProcessDto.getProcessKey(), metaData);
    }

    public ProcessInstanceDto getProcessInstance(String processInstanceId) {
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId).singleResult();
        if (processInstance == null) {
            HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
                    .processInstanceId(processInstanceId).singleResult();
            return ProcessInstanceDto.builder()
                    .rootProcessInstanceId(historicProcessInstance.getRootProcessInstanceId())
                    .completed(true)
                    .processInstanceId(historicProcessInstance.getRootProcessInstanceId())
                    .build();
        }
        return ProcessInstanceDto.builder()
                .rootProcessInstanceId(processInstance.getRootProcessInstanceId())
                .completed(processInstance.isEnded())
                .processInstanceId(processInstance.getProcessInstanceId())
                .build();
    }

    public boolean processExistsWithVariables(String key, Object value) {
        List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery()
                .variableValueEquals(key, value)
                .orderByProcessInstanceId()
                .desc()
                .list();
        if (CollectionUtils.isEmpty(processInstances)) {
            List<HistoricProcessInstance> historicProcessInstances = historyService.createHistoricProcessInstanceQuery()
                    .variableValueEquals(key, value)
                    .orderByProcessInstanceId()
                    .desc()
                    .list();
            if (CollectionUtils.isEmpty(historicProcessInstances)) {
                throw new QuantumException(CALENDAR_SYNC_WAS_NOT_INITIATED_BY_USER);
            }

            String processInstanceId = historicProcessInstances.get(0).getRootProcessInstanceId();
            Map<String, Object> processVariablesOfProcess = getProcessVariablesOfProcess(processInstanceId);
            if (processVariablesOfProcess.containsKey(PROCESS_ERRORED_OUT)) {
                throw new QuantumException(String.valueOf(processVariablesOfProcess.get(PROCESS_ERRORED_OUT)));
            }
            return false;
        } else {
            String processInstanceId = processInstances.get(0).getProcessInstanceId();
            Map<String, Object> processVariablesOfProcess = getProcessVariablesOfProcess(processInstanceId);
            if (processVariablesOfProcess.containsKey(PROCESS_ERRORED_OUT)) {
                throw new QuantumException(String.valueOf(processVariablesOfProcess.get(PROCESS_ERRORED_OUT)));
            }
            return true;
        }
    }

    public Map<String, Object> getProcessVariablesOfProcess(String processInstanceId) {
        Map<String, Object> processVariables = new HashMap<>();
        runtimeService.createVariableInstanceQuery().processInstanceIdIn(processInstanceId)
                .list().forEach(variableInstance ->
                        processVariables.putIfAbsent(variableInstance.getName(), variableInstance.getValue()));
        return processVariables;
    }

    public Map<String, Object> getProcessVariablesOfHistoricProcess(String processInstanceId) {
        Map<String, Object> processVariables = new HashMap<>();
        historyService.createHistoricVariableInstanceQuery().processInstanceId(processInstanceId)
                .list().forEach(historicVariableInstance ->
                        processVariables.putIfAbsent(historicVariableInstance.getName(), historicVariableInstance.getValue()));
        return processVariables;
    }

    public List<TaskDetailsDto> getAllTasksForProcess(String processInstanceId) {
        Set<String> taskAlreadyAdded = new HashSet<>();
        List<TaskDetailsDto> mainProcessTasks = getProcessTasks(processInstanceId, taskAlreadyAdded);
        List<String> linkedChildProcessesIds = runtimeService.createProcessInstanceQuery()
                .variableValueEquals(PARENT_PROCESS_INSTANCE_ID, processInstanceId)
                .active()
                .list()
                .stream()
                .map(ProcessInstance::getRootProcessInstanceId)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(linkedChildProcessesIds)) {
            linkedChildProcessesIds = historyService.createHistoricProcessInstanceQuery()
                    .variableValueEquals(PARENT_PROCESS_INSTANCE_ID, processInstanceId)
                    .list()
                    .stream()
                    .map(HistoricProcessInstance::getRootProcessInstanceId)
                    .collect(Collectors.toList());
        }
        if (CollectionUtils.isNotEmpty(linkedChildProcessesIds)) {
            linkedChildProcessesIds.forEach(linkedChildProcessId ->
                    mainProcessTasks.addAll(getProcessTasks(linkedChildProcessId, taskAlreadyAdded))
            );
        }
        return mainProcessTasks;
    }

    private List<TaskDetailsDto> getProcessTasks(String processInstanceId, Set<String> taskAlreadyAdded) {
        List<TaskDetailsDto> allTasks = new ArrayList<>();
        List<TaskDetailsDto> activeTasks = getCurrentActiveTasksOfProcess(processInstanceId);
        List<TaskDetailsDto> historicTasks = historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(processInstanceId)
                .list().stream()
                .map(this::mapToTaskDto)
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(activeTasks)) {
            TasksHelper.populateTasksIfNeeded(taskAlreadyAdded, allTasks, activeTasks);
        }
        if (CollectionUtils.isNotEmpty(historicTasks)) {
            TasksHelper.populateTasksIfNeeded(taskAlreadyAdded, allTasks, historicTasks);
        }
        return allTasks;
    }

    private List<TaskDetailsDto> getCurrentActiveTasksOfProcess(String processInstanceId) {
        return taskService.createTaskQuery()
                .processInstanceId(processInstanceId)
                .list().stream()
                .map(this::mapToTaskDto)
                .collect(Collectors.toList());
    }

    public boolean assignTask(String taskId, String userId) {
        if (StringUtils.isNotBlank(userId) && userExists(userId)) {
            Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
            if (task == null) {
                throw new QuantumException(TASK_DOES_NOT_EXIST_WITH_ID + taskId);
            }
            task.setAssignee(userId);
            taskService.saveTask(task);
            return true;
        } else {
            throw new QuantumException(USER_DOES_NOT_EXIST_WITH_ID + userId +
                    PLEASE_FIRST_REGISTER_USER_WITH_QUANTUM_BEFORE_ASSIGNING_TASK);
        }
    }

    private boolean userExists(String userId) {
        String[] userIds = userId.split("\\|");
        for(String id : userIds) {

            User user = identityService.createUserQuery().userId(id).singleResult();
            if (user == null) {
                createUserIfNotExists(UserDetailsDto.builder().userId(id).build());
            }
        }
        return true;
    }

    public boolean completeTask(TaskCompletionDto taskCompletionDto) {
        try {
            Map<String, Object> variables = new HashMap<>();
            Task task = taskService.createTaskQuery().taskId(taskCompletionDto.getTaskId()).singleResult();
            String assignee = task.getAssignee();
            if (processIfMultiOwnerTask(taskCompletionDto, task)) {
                task.setDescription(taskCompletionDto.getStatus());
                taskService.saveTask(task);
                taskService.complete(taskCompletionDto.getTaskId(), variables);
                return true;
            }
            if (StringUtils.isBlank(assignee) && !AUTO.equalsIgnoreCase(taskCompletionDto.getUserId())) {
                throw new QuantumException("Assignee of the task can not be blank if it's not a multi owner task or auto task");
            }
            if (StringUtils.isNotBlank(assignee)
                    && !AUTO.equalsIgnoreCase(taskCompletionDto.getUserId())
                    && !assignee.equalsIgnoreCase(taskCompletionDto.getUserId())) {
                throw new QuantumException(TASK_CAN_ONLY_BE_COMPLETE_BY_ASSIGNED_USER +
                        USER_ASSIGNED + assignee +
                        USER_TRYING_TO_COMPLETE_TASK + taskCompletionDto.getUserId());
            }
            task.setDescription(taskCompletionDto.getStatus());
            taskService.saveTask(task);
            taskService.complete(taskCompletionDto.getTaskId(), variables);
            return true;
        } catch (Exception ex) {
            log.error(ex.getMessage());
            return false;
        }
    }

    private boolean processIfMultiOwnerTask(TaskCompletionDto taskCompletionDto, Task task) {
        String currentUserId = taskCompletionDto.getUserId();
        String ownerIds = task.getOwner();
        String[] splitOwnerIds;
        if (StringUtils.isNotEmpty(ownerIds) && ownerIds.contains("|")) {
            splitOwnerIds = ownerIds.split("\\|");
            for (String ownerId : splitOwnerIds) {
                if (currentUserId.equalsIgnoreCase(ownerId)) {
                    return true;
                }
            }
        }
        return false;
    }


    public void completeTask(String taskId) {
        try {
            taskService.complete(taskId);
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
    }

    public boolean updateCamundaTask(TaskUpdationDto taskUpdationDto) {
        try {
            Task task = taskService.createTaskQuery().taskId(taskUpdationDto.getTaskId()).singleResult();
            if (!StringUtils.isEmpty(taskUpdationDto.getDescription())) {
                task.setDescription(taskUpdationDto.getDescription());
            }
            taskService.saveTask(task);
            return true;
        } catch (Exception ex) {
            log.error(ex.getMessage());
            return false;
        }
    }

    public boolean updateProcessVariables(String processInstanceId, Map<String, Object> updatedProcessVariables) {
        try {
            runtimeService.setVariables(processInstanceId, updatedProcessVariables);
            return true;
        } catch (Exception ex) {
            log.error(ex.getMessage());
            return false;
        }
    }

    public boolean createMembership(String userId, String groupId) {
        if (StringUtils.isNotBlank(userId) && !userExists(userId)) {
            throw new QuantumException(USER_DOES_NOT_EXIST_WITH_ID + userId +
                    PLEASE_FIRST_REGISTER_USER_WITH_QUANTUM_BEFORE_ASSIGNING_IT_TO_GROUP + groupId);
        }
        createGroupIfNotExists(groupId);
        identityService.createMembership(userId, groupId);
        return true;
    }

    public boolean createUserIfNotExists(UserDetailsDto userDetailsDto) {
        User user = identityService.createUserQuery().userId(userDetailsDto.getUserId()).singleResult();
        if (user != null) {
            List<Group> existingGroups = identityService.createGroupQuery().groupMember(user.getId()).list();
            if (CollectionUtils.isNotEmpty(userDetailsDto.getGroups())) {
                if (CollectionUtils.isEmpty(existingGroups)) {
                    existingGroups = new ArrayList<>();
                }
                List<String> finalList = existingGroups.stream()
                        .map(Group::getId)
                        .collect(Collectors.toList());
                userDetailsDto.getGroups().forEach(group -> {
                    if (!finalList.contains(group)) {
                        createMembership(user.getId(), group);
                    }
                });
            }
        } else {
            User newUser = identityService.newUser(userDetailsDto.getUserId());
            String encryptedLastName = userDetailsDto.getLastName() + "|" + userDetailsDto.getImageUrl();
            String encryptedFirstName = userDetailsDto.getFirstName() + "|" + userDetailsDto.getRoles();
            newUser.setEmail(userDetailsDto.getEmail());
            newUser.setFirstName(encryptedFirstName);
            newUser.setLastName(encryptedLastName);
            identityService.saveUser(newUser);
            if (CollectionUtils.isNotEmpty(userDetailsDto.getGroups())) {
                userDetailsDto.getGroups().forEach(group -> createMembership(newUser.getId(), group));
            }
        }
        return true;
    }

    public void updateUser(User user) {
        identityService.saveUser(user);
    }

    private void createGroupIfNotExists(String groupId) {
        Group group = identityService.createGroupQuery().groupId(groupId).singleResult();
        if (group == null) {
            Group newGroup = identityService.newGroup(groupId);
            newGroup.setName(groupId);
            identityService.saveGroup(newGroup);
        }
    }

    public List<User> fetchAllUsers(String groupId) {
        if (StringUtils.isNotBlank(groupId) && groupDoesNotExists(groupId)) {
            throw new QuantumException(GROUP_DOES_NOT_EXIST_WITH_ID + groupId);
        }
        return StringUtils.isBlank(groupId) ?
                identityService.createUserQuery().orderByUserId().asc().list() :
                identityService.createUserQuery().memberOfGroup(groupId).list();
    }

    public List<Group> fetchAllGroups(String userId) {
        return StringUtils.isBlank(userId) ?
                identityService.createGroupQuery().orderByGroupId().asc().list() :
                identityService.createGroupQuery().groupMember(userId).list();
    }

    public boolean deleteMembership(String userId, String groupId) {
        if (StringUtils.isNotBlank(userId) && !userExists(userId)) {
            throw new QuantumException(USER_DOES_NOT_EXIST_WITH_ID + userId);
        }
        if (StringUtils.isNotBlank(groupId) && groupDoesNotExists(groupId)) {
            throw new QuantumException(GROUP_DOES_NOT_EXIST_WITH_ID + groupId);
        }
        identityService.deleteMembership(userId, groupId);
        return true;
    }

    public boolean deleteUser(String userId) {
        if (StringUtils.isNotBlank(userId) && !userExists(userId)) {
            throw new QuantumException(USER_DOES_NOT_EXIST_WITH_ID + userId);
        }
        fetchAllGroups(userId).forEach(group -> deleteMembership(userId, group.getId()));
        identityService.deleteUser(userId);
        return true;
    }

    public boolean deleteGroup(String groupId) {
        if (StringUtils.isNotBlank(groupId) && groupDoesNotExists(groupId)) {
            throw new QuantumException(GROUP_DOES_NOT_EXIST_WITH_ID + groupId);
        }
        fetchAllUsers(groupId).forEach(user -> deleteMembership(user.getId(), groupId));
        identityService.deleteGroup(groupId);
        return true;
    }

    private boolean groupDoesNotExists(String groupId) {
        Group group = identityService.createGroupQuery().groupId(groupId).singleResult();
        return group == null;
    }

    public User fetchUser(String userId) {
        return identityService.createUserQuery().userId(userId).singleResult();
    }

    public List<TaskDetailsDto> fetchAllTasksForUser(String userId, String operation) {
        if (ASSIGNEE.equalsIgnoreCase(operation)) {
            return getTasksAssignedToUser(userId, new HashSet<>());
        }
        if (OWNER.equalsIgnoreCase(operation)) {
            return getTasksOwnedByUser(userId, new HashSet<>());
        }
        Set<String> tasksAlreadyAdded = new HashSet<>();
        List<TaskDetailsDto> tasksAssignedToUser = getTasksAssignedToUser(userId, tasksAlreadyAdded);
        List<TaskDetailsDto> tasksOwnedByUser = getTasksOwnedByUser(userId, tasksAlreadyAdded);
        List<TaskDetailsDto> allTasks = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(tasksAssignedToUser)) {
            allTasks.addAll(tasksAssignedToUser);
        }
        if (CollectionUtils.isNotEmpty(tasksOwnedByUser)) {
            allTasks.addAll(tasksOwnedByUser);
        }
        return allTasks;
    }

    private List<TaskDetailsDto> getTasksOwnedByUser(String userId, Set<String> tasksAlreadyAdded) {
        List<TaskDetailsDto> allTasks = new ArrayList<>();
        List<TaskDetailsDto> activeTasks = getCurrentActiveTasksOfUser(userId);
        List<TaskDetailsDto> historicTasks = getHistoricTasksOfUser(userId);
        if (CollectionUtils.isNotEmpty(activeTasks)) {
            TasksHelper.populateTasksIfNeeded(tasksAlreadyAdded, allTasks, activeTasks);
        }
        if (CollectionUtils.isNotEmpty(historicTasks)) {
            TasksHelper.populateTasksIfNeeded(tasksAlreadyAdded, allTasks, historicTasks);
        }
        return allTasks;
    }

    private List<TaskDetailsDto> getHistoricTasksOfUser(String userId) {
        return historyService.createHistoricTaskInstanceQuery()
                .taskOwner(userId)
                .list().stream()
                .map(this::mapToTaskDto)
                .collect(Collectors.toList());
    }

    private List<TaskDetailsDto> getCurrentActiveTasksOfUser(String userId) {
        return taskService.createTaskQuery()
                .taskOwner(userId)
                .list().stream()
                .map(this::mapToTaskDto)
                .collect(Collectors.toList());
    }

    private List<TaskDetailsDto> getTasksAssignedToUser(String userId, Set<String> tasksAlreadyAdded) {
        List<TaskDetailsDto> allTasks = new ArrayList<>();
        List<TaskDetailsDto> activeTasks = taskService.createTaskQuery()
                .taskAssignee(userId)
                .list().stream()
                .map(this::mapToTaskDto)
                .collect(Collectors.toList());
        List<TaskDetailsDto> historicTasks = historyService.createHistoricTaskInstanceQuery()
                .taskAssignee(userId)
                .list().stream()
                .map(this::mapToTaskDto)
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(activeTasks)) {
            TasksHelper.populateTasksIfNeeded(tasksAlreadyAdded, allTasks, activeTasks);
        }
        if (CollectionUtils.isNotEmpty(historicTasks)) {
            TasksHelper.populateTasksIfNeeded(tasksAlreadyAdded, allTasks, historicTasks);
        }
        return allTasks;
    }

    public void assignTaskOwner(String taskId, String owner) {
        if (StringUtils.isNotBlank(owner) && userExists(owner)) {
            Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
            if (StringUtils.isNotBlank(task.getOwner())) {
                return;
            }
            task.setOwner(owner);
            taskService.saveTask(task);
        } else {
            throw new QuantumException(USER_DOES_NOT_EXIST_WITH_ID + owner +
                    PLEASE_FIRST_REGISTER_USER_WITH_QUANTUM_BEFORE_ASSIGNING_TASK);
        }
    }

    public List<TaskDetailsDto> fetchAllTaskForUserOfStatus(String userId, String status) {
        if (APPROVED.equalsIgnoreCase(status) || DECLINED.equalsIgnoreCase(status)) {
            return getTasksWithStatusWithUser(userId, status.toUpperCase());
        }
        return new ArrayList<>();
    }

    private List<TaskDetailsDto> getTasksWithStatusWithUser(String userId, String status) {
        List<TaskDetailsDto> activeTasks = taskService.createTaskQuery()
                .taskDescription(status)
                .list().stream()
                .map(this::mapToTaskDto)
                .filter(taskDetailsDto -> userId.equalsIgnoreCase(taskDetailsDto.getAssignee()) ||
                        userId.equalsIgnoreCase(taskDetailsDto.getOwner()))
                .collect(Collectors.toList());
        List<TaskDetailsDto> historicTasks = historyService.createHistoricTaskInstanceQuery()
                .taskDescription(status)
                .list().stream()
                .map(this::mapToTaskDto)
                .filter(taskDetailsDto -> userId.equalsIgnoreCase(taskDetailsDto.getAssignee()) ||
                        userId.equalsIgnoreCase(taskDetailsDto.getOwner()))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(activeTasks)) {
            activeTasks = new ArrayList<>();
        }
        if (CollectionUtils.isNotEmpty(historicTasks)) {
            activeTasks.addAll(historicTasks);
        }
        return activeTasks;
    }

    public String getProcessInstanceIdOfTask(String taskId) {
        Task task = taskService.createTaskQuery()
                .taskId(taskId)
                .singleResult();
        if (task != null) {
            return task.getProcessInstanceId();
        }
        return "";
    }

    public void triggerProcessByMessageId(String messageId, Map<String, Object> variables) {
        runtimeService.createMessageCorrelation(messageId)
                .setVariables(variables)
                .correlate();
    }

    public void uploadedSignedDocument(String processInstanceId, MultipartFile file) {

    }
}
