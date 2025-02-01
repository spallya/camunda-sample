package com.vider.quantum.engine.delegate.calendar;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.util.DateTime;
import com.vider.quantum.engine.dto.QuantumCalendarEvent;
import com.vider.quantum.engine.dto.QuantumCalendarEvents;
import com.vider.quantum.engine.exception.QuantumException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Slf4j
public class SetLoopThroughForCalendarEvents implements JavaDelegate {

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        try {
            List<Map<String, Object>> loopThrough = new ArrayList<>();
            Object googleCalendarEvents = delegateExecution.getVariable("googleCalendarEvents");
            if (googleCalendarEvents != null) {
                QuantumCalendarEvents quantumCalendarEvents = (QuantumCalendarEvents) googleCalendarEvents;
                List<QuantumCalendarEvent> calendarEvents = quantumCalendarEvents.getEvents();
                if (CollectionUtils.isNotEmpty(calendarEvents)) {
                    AtomicInteger index = new AtomicInteger();
                    calendarEvents.forEach(event -> {
                        Map<String, Object> objectMap = new HashMap<>();
                        objectMap.put("index", index.getAndIncrement());
                        objectMap.put("title", getFormattedString(event.getSummary()));
                        objectMap.put("location", getFormattedString(event.getLocation()));
                        objectMap.put("notes", " ");
                        objectMap.put("date", getDateTimeAsString(event.getCreated(), true));
                        objectMap.put("startTime", getDateTimeAsString(event.getStart(), false));
                        objectMap.put("endTime", getDateTimeAsString(event.getEnd(), false));
                        loopThrough.add(objectMap);
                    });
                }
            }
            delegateExecution.setVariable("loopThrough", loopThrough);
        } catch (Exception ex) {
            log.error(ex.getMessage());
            delegateExecution.setVariable("loopThrough", new ArrayList<>());
            delegateExecution.setVariable("processErroredOut", ex.getMessage());
        }

    }

    private static String getFormattedString(String str) {
        return StringUtils.isNotBlank(str) ? str.replace("'", "") : " ";
    }

    private String getDateTimeAsString(String timeObj, boolean onlyDate) {
        if (StringUtils.isBlank(timeObj)) {
            return "2099-12-30 00:00:00";
        }
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("E MMM dd HH:mm:ss z uuuu", Locale.ENGLISH);
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
            if (onlyDate) {
                String formattedDateTime = new Date(DateTime.parseRfc3339(timeObj).getValue()).toString();
                return LocalDate.parse(formattedDateTime, inputFormatter).toString();
            }
            Map map = objectMapper.readValue(timeObj, Map.class);
            String dateTime = (String) map.getOrDefault("dateTime", "");
            if (StringUtils.isBlank(dateTime)) {
                dateTime = (String) map.getOrDefault("date", "");
            }
            String formattedDateTime = new Date(DateTime.parseRfc3339(dateTime).getValue()).toString();
            return outputFormatter.format(LocalDateTime.parse(formattedDateTime, inputFormatter));
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new QuantumException(e);
        }
    }
}
