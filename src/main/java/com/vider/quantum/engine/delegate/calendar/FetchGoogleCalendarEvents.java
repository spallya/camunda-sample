package com.vider.quantum.engine.delegate.calendar;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Events;
import com.vider.quantum.engine.dto.QuantumCalendarEvent;
import com.vider.quantum.engine.dto.QuantumCalendarEvents;
import com.vider.quantum.engine.exception.QuantumException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class FetchGoogleCalendarEvents implements JavaDelegate {

    private static final String APPLICATION_NAME = "Vider Quantum Google Calendar API";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        Object accessToken = delegateExecution.getVariable("accessToken");
        Object sdate = delegateExecution.getVariable("sdate");
        Object edate = delegateExecution.getVariable("edate");
        Object pageSize = delegateExecution.getVariable("pageSize");
        Object pageToken = delegateExecution.getVariable("pageToken");
        Events eventList;
        try {
            if (accessToken == null || StringUtils.isBlank(String.valueOf(accessToken))) {
                throw new QuantumException("Invalid Access Token");
            }
            GoogleCredential credential = new GoogleCredential().setAccessToken(String.valueOf(accessToken));
            HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            Calendar service = new Calendar.Builder(httpTransport, JSON_FACTORY, credential)
                    .setApplicationName(APPLICATION_NAME).build();
            Calendar.Events events = service.events();
            Calendar.Events.List primaryEventsList = events.list("primary")
                    .setTimeZone("Asia/Kolkata");
            buildSearchQuery(sdate, edate, pageSize, pageToken, primaryEventsList);
            eventList = primaryEventsList.execute();
            QuantumCalendarEvents quantumCalendarEvents = prepareResult(eventList);
            delegateExecution.setVariable("googleCalendarEvents", quantumCalendarEvents);
        } catch (Exception e) {
            log.error(e.getMessage());
            delegateExecution.setVariable("processErroredOut", e.getMessage());
        }
    }

    private void buildSearchQuery(Object sdate, Object edate, Object pageSize, Object pageToken, Calendar.Events.List primaryEventsList) {
        if (sdate != null && StringUtils.isNotBlank(String.valueOf(sdate))) {
            final DateTime startDate = new DateTime(sdate + "T00:00:00");
            primaryEventsList.setTimeMin(startDate);
        }
        if (edate != null && StringUtils.isNotBlank(String.valueOf(edate))) {
            final DateTime endDate = new DateTime(edate + "T23:59:59");
            primaryEventsList.setTimeMax(endDate);
        }
        if (pageToken != null && StringUtils.isNotBlank(String.valueOf(pageToken))) {
            primaryEventsList.setPageToken(String.valueOf(pageToken));
        }
        if (pageSize != null && StringUtils.isNotBlank(String.valueOf(pageSize))) {
            primaryEventsList.setMaxResults(Integer.parseInt(String.valueOf(pageSize)));
        }
    }

    private QuantumCalendarEvents prepareResult(Events eventList) {
        List<QuantumCalendarEvent> events = eventList.getItems().stream()
                .map(event ->
                        QuantumCalendarEvent.builder()
                                .id(event.getId())
                                .summary(event.getSummary())
                                .kind(event.getKind())
                                .end(getObjectAsString(event.getEnd()))
                                .attendees(event.getAttendees())
                                .created(getObjectAsString(event.getCreated()))
                                .start(getObjectAsString(event.getStart()))
                                .description(event.getDescription())
                                .eventType(event.getEventType())
                                .htmlLink(event.getHtmlLink())
                                .location(event.getLocation())
                                .originalStartTime(getObjectAsString(event.getOriginalStartTime()))
                                .build()
                ).collect(Collectors.toList());
        return QuantumCalendarEvents.builder()
                .nextPageToken(eventList.getNextPageToken())
                .source("GOOGLE")
                .events(events)
                .build();
    }

    private String getObjectAsString(Object obj) {
        return obj != null ? obj.toString() : "";
    }
}
