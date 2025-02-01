package com.vider.quantum.engine.dto;

import com.google.api.services.calendar.model.EventAttendee;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class QuantumCalendarEvent {

    private String id;
    private String summary;
    private String kind;
    private String location;
    private String description;
    private List<EventAttendee> attendees;
    private String created;
    private String originalStartTime;
    private String start;
    private String end;
    private String eventType;
    private String htmlLink;

}