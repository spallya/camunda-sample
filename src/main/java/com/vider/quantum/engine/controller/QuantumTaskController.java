package com.vider.quantum.engine.controller;

import com.vider.quantum.engine.dto.StartProcessDto;
import com.vider.quantum.engine.dto.TaskCompletionDto;
import com.vider.quantum.engine.dto.TaskDetailsDto;
import com.vider.quantum.engine.dto.TaskUpdationDto;
import com.vider.quantum.engine.service.QuantumService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/vider/quantum/api")
@RequiredArgsConstructor
public class QuantumTaskController {

    private final QuantumService quantumService;

    @PutMapping("/task/complete")
    public ResponseEntity<Boolean> completeTask(@RequestBody TaskCompletionDto taskCompletionDto) {
        boolean isCompleted = quantumService.completeCamundaTask(taskCompletionDto);
        return new ResponseEntity<>(isCompleted, HttpStatus.OK);
    }

    @PutMapping("/task")
    public ResponseEntity<Boolean> updateTask(@RequestBody TaskUpdationDto taskUpdationDto) {
        boolean isCompleted = quantumService.updateCamundaTask(taskUpdationDto);
        return new ResponseEntity<>(isCompleted, HttpStatus.OK);
    }

    @PutMapping("/task/{taskId}/assign/{userId}")
    public ResponseEntity<Boolean> assignTask(@PathVariable String taskId, @PathVariable String userId) {
        boolean isCompleted = quantumService.assignTask(taskId, userId);
        return new ResponseEntity<>(isCompleted, HttpStatus.OK);
    }

    @PutMapping("/tasks/{processInstanceId}")
    public ResponseEntity<Boolean> updateOwners(@PathVariable String processInstanceId, @RequestBody StartProcessDto startProcessDto) {
        boolean isUpdate = quantumService.updateOwners(startProcessDto, processInstanceId);
        return new ResponseEntity<>(isUpdate, HttpStatus.OK);
    }

    @GetMapping({"/tasks/{userId}", "/tasks/{userId}/{operation}"})
    public ResponseEntity<List<TaskDetailsDto>> fetchAllTaskForUser(@PathVariable String userId,
                                                                    @PathVariable(required = false) String operation) {
        List<TaskDetailsDto> userTasks = quantumService.fetchAllTasksForUser(userId, operation);
        return new ResponseEntity<>(userTasks, HttpStatus.OK);
    }

    @GetMapping("/tasks/status/{status}/{userId}")
    public ResponseEntity<List<TaskDetailsDto>> fetchAllTaskForUserOfStatus(@PathVariable String userId,
                                                                    @PathVariable String status) {
        List<TaskDetailsDto> userTasks = quantumService.fetchAllTaskForUserOfStatus(userId, status);
        return new ResponseEntity<>(userTasks, HttpStatus.OK);
    }

}
