package com.vider.quantum.engine.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class ProcessDetailsDto {
    protected String processInstanceId;
    protected String rootProcessInstanceId;
    protected boolean completed;
    protected Map<String, Object> data;
    protected List<TaskDetailsDto> tasks;

}
