package com.vider.quantum.engine.util;

import com.vider.quantum.engine.dto.automation.Schedule;

import java.time.DayOfWeek;
import java.util.Map;

public class CronExpressionBuilder {

    public static String createCronExpression(Schedule request) {
        String cronExpression = null;
        int hour = request.get24HourFormat();
        int minute = request.getMinute();
        int seconds = request.getSeconds();
        int intervalMinutes = request.getIntervalMinutes();
        switch (request.getPeriodicity()) {
            case DAILY:
                cronExpression = String.format("%d %d %d * * ?", seconds, minute, hour);
                break;
            case WEEKLY:
                if (request.getDays() != null) {
                    cronExpression = String.format("%d %d %d ? * %s", seconds, minute, hour, request.getDays().name().substring(0, 3).toUpperCase());
                }
                break;
            case MONTHLY:
                if (request.getDaysInstance() != null && !request.getDaysInstance().isEmpty()) {
                    Map.Entry<DayOfWeek, Integer> entry = request.getDaysInstance().entrySet().iterator().next();
                    int dayOfWeekInMonth = entry.getValue();
                    DayOfWeek dayOfWeek = entry.getKey();
                    cronExpression = String.format("%d %d %d ? * ? %d%s", seconds, minute, hour, dayOfWeekInMonth, dayOfWeek.name().substring(0, 3).toUpperCase());
                }
                break;
            case HOURLY:
                if (intervalMinutes > 0) {
                    cronExpression = String.format("%d 0/%d * * * ?", seconds, intervalMinutes);
                } else {
                    cronExpression = String.format("%d %d 0/1 * * ?", seconds, minute);
                }
                break;
            case YEARLY:
                int month = request.getMonth();
                int dayOfMonth = request.getDayOfMonth();
                cronExpression = String.format("%d %d %d %d %d ?", seconds, minute, hour, dayOfMonth, month);
                break;
            default:
                break;
        }

        return cronExpression;
    }
}
