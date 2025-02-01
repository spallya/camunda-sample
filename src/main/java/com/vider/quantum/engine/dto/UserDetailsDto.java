package com.vider.quantum.engine.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class UserDetailsDto {

    private String userId;
    private String firstName;
    private String lastName;
    private String email;
    private String imageUrl;
    private String roles;
    private List<String> groups;

}
