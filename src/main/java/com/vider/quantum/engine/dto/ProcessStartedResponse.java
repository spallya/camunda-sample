package com.vider.quantum.engine.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProcessStartedResponse {
    private String processInstanceId;
    private String businessKey;

}
