package com.vider.quantum.engine.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProcessInstanceDto {

    protected String processInstanceId;
    protected String rootProcessInstanceId;
    protected boolean completed;

}
