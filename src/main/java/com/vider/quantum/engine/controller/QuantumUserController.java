package com.vider.quantum.engine.controller;

import com.vider.quantum.engine.dto.UserDetailsDto;
import com.vider.quantum.engine.service.QuantumService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/vider/quantum/api")
@RequiredArgsConstructor
public class QuantumUserController {

    private final QuantumService quantumService;

    @PostMapping("/users")
    public ResponseEntity<Boolean> addUser(@RequestBody UserDetailsDto userDetailsDto) {
        return new ResponseEntity<>(quantumService.addUser(userDetailsDto), HttpStatus.CREATED);
    }

    @GetMapping({"/users", "/groups/{groupId}/users"})
    public ResponseEntity<List<UserDetailsDto>> fetchAllUsers(@PathVariable(required = false) String groupId) {
        List<UserDetailsDto> userDetailsDtos = quantumService.fetchAllUsers(groupId);
        return new ResponseEntity<>(userDetailsDtos, HttpStatus.OK);
    }

    @GetMapping({"/groups", "/users/{userId}/groups"})
    public ResponseEntity<List<String>> fetchAllGroups(@PathVariable(required = false) String userId) {
        List<String> allGroups = quantumService.fetchAllGroups(userId);
        return new ResponseEntity<>(allGroups, HttpStatus.OK);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<UserDetailsDto> fetchUser(@PathVariable String userId) {
        UserDetailsDto user = quantumService.fetchUser(userId);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PutMapping("/users/{userId}")
    public ResponseEntity<UserDetailsDto> updateUser(@PathVariable String userId, @RequestBody UserDetailsDto userDetailsDto) {
        UserDetailsDto user = quantumService.updateUser(userId, userDetailsDto);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PutMapping("/users")
    public ResponseEntity<Boolean> updateUsers(@RequestBody List<UserDetailsDto> userDetailsDtos) {
        boolean user = quantumService.updateUsers(userDetailsDtos);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PostMapping("/users/{userId}/groups/{groupId}")
    public ResponseEntity<Boolean> createMembership(@PathVariable String userId, @PathVariable String groupId) {
        return new ResponseEntity<>(quantumService.createMembership(userId, groupId),
                HttpStatus.CREATED);
    }

    @DeleteMapping("/users/{userId}/groups/{groupId}")
    public ResponseEntity<Boolean> deleteMembership(@PathVariable String userId, @PathVariable String groupId) {
        return new ResponseEntity<>(quantumService.deleteMembership(userId, groupId),
                HttpStatus.OK);
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Boolean> deleteUser(@PathVariable String userId) {
        return new ResponseEntity<>(quantumService.deleteUser(userId),
                HttpStatus.OK);
    }

    @DeleteMapping("/groups/{groupId}")
    public ResponseEntity<Boolean> deleteGroup(@PathVariable String groupId) {
        return new ResponseEntity<>(quantumService.deleteGroup(groupId),
                HttpStatus.OK);
    }

    @PutMapping("/groups/{oldGroupId}/{newGroupId}")
    public ResponseEntity<Boolean> replaceGroupIds(@PathVariable String oldGroupId, @PathVariable String newGroupId, @RequestBody List<String> userIds) {
        return new ResponseEntity<>(quantumService.replaceGroupIds(oldGroupId, newGroupId, userIds),
                HttpStatus.OK);
    }
}
