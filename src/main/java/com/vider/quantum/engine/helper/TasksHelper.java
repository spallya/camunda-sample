package com.vider.quantum.engine.helper;

import com.vider.quantum.engine.dto.TaskCompletionDto;
import com.vider.quantum.engine.dto.TaskDetailsDto;
import com.vider.quantum.engine.dto.TaskSnapshot;
import com.vider.quantum.engine.service.CamundaService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.vider.quantum.engine.constants.QuantumConstants.*;

@Slf4j
public class TasksHelper {

    public static List<TaskDetailsDto> filterSnapshotsTasks(List<TaskDetailsDto> tasks,
                                                            Map<String, Object> processVariables) {
        AtomicReference<Set<String>> snapshotTaskIds = new AtomicReference<>(new HashSet<>());
        if (processVariables.containsKey(TASK_SNAPSHOTS)) {
            if (processVariables.get(TASK_SNAPSHOTS) != null) {
                List<TaskSnapshot> taskSnapshots = (List<TaskSnapshot>) processVariables.get(TASK_SNAPSHOTS);
                if (CollectionUtils.isNotEmpty(tasks) && CollectionUtils.isNotEmpty(taskSnapshots)) {
                    taskSnapshots.forEach(taskSnapshot -> {
                        List<TaskDetailsDto> taskSnapshotTasks = taskSnapshot.getTasks();
                        if (CollectionUtils.isNotEmpty(taskSnapshotTasks)) {
                            taskSnapshotTasks.forEach(task -> {
                                filterUnwantedCharsFromTaskData(task);
                            });
                            snapshotTaskIds.get()
                                    .addAll(taskSnapshotTasks.stream()
                                            .map(TaskDetailsDto::getId)
                                            .collect(Collectors.toSet()));
                        }
                    });
                    return tasks.stream()
                            .filter(task -> !snapshotTaskIds.get().contains(task.getId()))
                            .toList();
                }
            }
        }
        if (CollectionUtils.isNotEmpty(tasks)) {
            tasks.forEach(task -> {
                filterUnwantedCharsFromTaskData(task);
            });
        }
        return tasks;
    }

    private static void filterUnwantedCharsFromTaskData(TaskDetailsDto task) {
        if (StringUtils.isNotEmpty(task.getAssignee())) task.setAssignee(task.getAssignee().replace("|", ""));
        if (StringUtils.isNotEmpty(task.getAssigneeName())) task.setAssigneeName(task.getAssigneeName().replace("|", ""));
        if (StringUtils.isNotEmpty(task.getOwner())) task.setOwner(task.getOwner().replace("|", ""));
        if (StringUtils.isNotEmpty(task.getOwnerName())) task.setOwnerName(task.getOwnerName().replace("|", ""));
    }

    public static void patchTasksData(List<TaskDetailsDto> tasks,
                                      Map<String, Object> processVariables) {
        if (processVariables.containsKey(TASKS_METADATA)) {
            if (processVariables.get(TASKS_METADATA) != null) {
                Map<String, TaskCompletionDto> tasksMetadata = (Map<String, TaskCompletionDto>) processVariables.get(TASKS_METADATA);
                if (CollectionUtils.isNotEmpty(tasks)) {
                    tasks.forEach(task -> {
                        if (tasksMetadata.containsKey(task.getId())) {
                            TaskCompletionDto updateState = tasksMetadata.get(task.getId());
                            task.setComments(updateState.getComments());
                            task.setStatus(updateState.getStatus());
                            task.setLastUpdatedOn(updateState.getLastUpdatedOn());
                        }
                    });
                }
            }
        }
    }

    public static void performAutoRejectionsAndSnapshotsUpdate(TaskCompletionDto taskCompletionDto,
                                                         Map<String, TaskCompletionDto> tasksMetadata,
                                                         List<TaskSnapshot> taskSnapshots,
                                                         String processInstanceId,
                                                         Map<String, Object> variables,
                                                         List<TaskDetailsDto> allTasksForProcess,
                                                         CamundaService camundaService) {
        List<TaskDetailsDto> activeTasks = allTasksForProcess.stream()
                .filter(taskDetailsDto -> !taskDetailsDto.isCompleted())
                .filter(taskDetailsDto -> !taskDetailsDto.getId().equalsIgnoreCase(taskCompletionDto.getTaskId()))
                .toList();
        Map<String, TaskCompletionDto> finalTasksMetadata = tasksMetadata;
        activeTasks.forEach(task -> {
            if (!HOLD_TILL_DOCUMENT_IS_SUBMITTED.equalsIgnoreCase(task.getName())) {
                TaskCompletionDto autoCompletionDto = TaskCompletionDto.builder()
                        .taskId(task.getId())
                        .comments(AUTO_REJECTED_DUE_TO_SIBLING_TASK_REJECTION)
                        .status(AUTO_DECLINED)
                        .userId(AUTO)
                        .lastUpdatedOn(new Date().toString())
                        .build();
                camundaService.completeTask(autoCompletionDto);
                finalTasksMetadata.put(task.getId(), autoCompletionDto);
            }
        });
        tasksMetadata.putAll(finalTasksMetadata);
        variables.put(TASKS_METADATA, tasksMetadata);
        if (variables.containsKey(TASK_SNAPSHOTS)) {
            taskSnapshots = (List<TaskSnapshot>) variables.get(TASK_SNAPSHOTS);
        }
        TaskSnapshot currentSnapshot = captureCurrentSnapshot(allTasksForProcess, tasksMetadata, taskSnapshots);
        currentSnapshot.setCreatedOn(new Date().toString());
        taskSnapshots.add(currentSnapshot);
        variables.put(TASK_SNAPSHOTS, taskSnapshots);
        camundaService.updateProcessVariables(processInstanceId, variables);
        reTriggerApprovalProcessIfRequired(processInstanceId, variables, currentSnapshot, camundaService);
    }

    private static void reTriggerApprovalProcessIfRequired(String processInstanceId,
                                                           Map<String, Object> variables,
                                                           TaskSnapshot currentSnapshot,
                                                           CamundaService camundaService) {
        if (variables.containsKey(APPROVAL_PROCESS_ID)) {
            String approvalProcessId = String.valueOf(
                    variables.get(APPROVAL_PROCESS_ID) != null ? variables.get(APPROVAL_PROCESS_ID) : "");
            if (StringUtils.isNotEmpty(approvalProcessId)) {
                Map<String, Object> tempVariables = new HashMap<>();
                tempVariables.put(PARENT_PROCESS_INSTANCE_ID, processInstanceId);
                camundaService.triggerProcessByMessageId(approvalProcessId, tempVariables);
            }
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                log.error(e.getMessage());
            }
            autoAssignOwnerAndAssignee(currentSnapshot, processInstanceId, camundaService);
        }
    }

    private static void autoAssignOwnerAndAssignee(TaskSnapshot currentSnapshot,
                                                   String processInstanceId,
                                                   CamundaService camundaService) {
        List<TaskDetailsDto> snapshotTasks = currentSnapshot.getTasks();
        Map<String, String> taskToOwnerMapping = new HashMap<>();
        Map<String, String> taskToAssigneeMapping = new HashMap<>();
        if (CollectionUtils.isNotEmpty(snapshotTasks)) {
            snapshotTasks.forEach(task -> {
                taskToOwnerMapping.put(task.getName(), task.getOwner());
                taskToAssigneeMapping.put(task.getName(), task.getAssignee());
            });
        }
        List<TaskDetailsDto> allTasksForProcess = camundaService.getAllTasksForProcess(processInstanceId);
        allTasksForProcess.forEach(task -> {
            String owner = null;
            String assignee = null;
            if (taskToOwnerMapping.containsKey(task.getName())) {
                owner = taskToOwnerMapping.get(task.getName());
            }
            if (taskToAssigneeMapping.containsKey(task.getName())) {
                assignee = taskToAssigneeMapping.get(task.getName());
            }
            if (StringUtils.isNotEmpty(owner)) {
                camundaService.assignTaskOwner(task.getId(), owner);
            }
            if (StringUtils.isNotEmpty(assignee)) {
                camundaService.assignTask(task.getId(), assignee);
            }
        });
    }

    private static TaskSnapshot captureCurrentSnapshot(List<TaskDetailsDto> allTasksForProcess,
                                                       Map<String, TaskCompletionDto> tasks,
                                                       List<TaskSnapshot> taskSnapshots) {
        Set<String> alreadyAddedSnapshotTasks = new HashSet<>();
        if (CollectionUtils.isNotEmpty(taskSnapshots)) {
            taskSnapshots.forEach(taskSnapshot -> {
                List<TaskDetailsDto> taskSnapshotTasks = taskSnapshot.getTasks();
                if (CollectionUtils.isNotEmpty(taskSnapshotTasks)) {
                    taskSnapshotTasks.forEach(task -> alreadyAddedSnapshotTasks.add(task.getId()));
                }
            });
        }
        List<TaskDetailsDto> snapshotTasks = allTasksForProcess.stream()
                .filter(task -> !HOLD_TILL_DOCUMENT_IS_SUBMITTED.equalsIgnoreCase(task.getName()))
                .filter(task -> !alreadyAddedSnapshotTasks.contains(task.getId()))
                .toList();
        if (CollectionUtils.isNotEmpty(snapshotTasks)) {
            snapshotTasks.forEach(task -> {
                if (tasks.containsKey(task.getId())) {
                    TaskCompletionDto updatedTaskState = tasks.get(task.getId());
                    task.setStatus(updatedTaskState.getStatus());
                    task.setCompleted(true);
                    task.setComments(updatedTaskState.getComments());
                    task.setLastUpdatedOn(new Date().toString());
                }
            });
        }
        return TaskSnapshot.builder()
                .snapshotId(UUID.randomUUID().toString())
                .tasks(snapshotTasks)
                .build();
    }

    public static void completeHoldProcessTaskIfAllOtherTasksAreCompleted(List<TaskDetailsDto> allTasksForProcess,
                                                                          CamundaService camundaService) {
        if (CollectionUtils.isNotEmpty(allTasksForProcess)) {
            List<TaskDetailsDto> activeTasks = allTasksForProcess.stream()
                    .filter(taskDetailsDto -> !taskDetailsDto.isCompleted())
                    .toList();
            if (CollectionUtils.isNotEmpty(activeTasks) && activeTasks.size() == 1) {
                TaskDetailsDto task = activeTasks.get(0);
                if (HOLD_TILL_DOCUMENT_IS_SUBMITTED.equalsIgnoreCase(task.getName())) {
                    camundaService.completeTask(task.getId());
                }
            }
        }
    }

    public static void populateTasksIfNeeded(Set<String> tasksAlreadyAdded, List<TaskDetailsDto> allTasks, List<TaskDetailsDto> targetTasks) {
        targetTasks.forEach(task -> {
            if (!tasksAlreadyAdded.contains(task.getId())) {
                tasksAlreadyAdded.add(task.getId());
                allTasks.add(task);
            }
        });
    }

}
