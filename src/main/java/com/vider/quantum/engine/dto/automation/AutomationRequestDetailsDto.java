package com.vider.quantum.engine.dto.automation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AutomationRequestDetailsDto {

    private String id;
    private String status;
    private String clientId;
    private String processInstanceId;
    private String message;
    private String createdBy;
}

