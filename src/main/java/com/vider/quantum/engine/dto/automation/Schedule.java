package com.vider.quantum.engine.dto.automation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Schedule {

    private Periodicity periodicity;
    private DayOfWeek days;
    private Map<DayOfWeek, Integer> daysInstance;
    private int hour;
    private int minute;
    private int seconds;
    private String amPm;
    private int dayOfMonth;
    private int month;
    private int intervalMinutes;

    public int get24HourFormat() {
        if ("pm".equalsIgnoreCase(amPm) && hour < 12) {
            return hour + 12;
        } else if ("am".equalsIgnoreCase(amPm) && hour == 12) {
            return 0;
        }
        return hour;
    }
}
