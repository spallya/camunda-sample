package com.vider.quantum.engine.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class QuantumCalendarEvents {

    private String source;
    private String nextPageToken;
    private List<QuantumCalendarEvent> events;

}