package com.vider.quantum.engine.dto.automation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AutomationSubmitRequestDto {

    private List<String> modules;
    private String autoCredentialsId;
    private String gstrCredentialsId;
    private String tanCredentialsId;
    private String type;
    private int orgId;
    private List<Schedule> schedules;


}
