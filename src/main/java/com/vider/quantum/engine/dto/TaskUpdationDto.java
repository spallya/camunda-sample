package com.vider.quantum.engine.dto;

import lombok.Data;

@Data
public class TaskUpdationDto {

    private String processInstanceId;
    private String taskId;
    private String comments;
    private String description;

}
