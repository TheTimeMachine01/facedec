package com.application.facedec.dto.Attendance;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO representing a single day's attendance log for the monthly view.
 * This is similar to TodayLogResponse but better suited for a list context.
 */
public class DailyLog {
    private LocalDate date;
    private String inTime;
    private String outTime;
    private String holidayStatus;

    // Default Constructor
    public DailyLog() {
        // Initialize with default/zero values to ensure consistency
        this.inTime = "00:00:00.000";
        this.outTime = "00:00:00.000";
        this.holidayStatus = "NA";
    }

    // Getters and Setters
    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getInTime() {
        return inTime;
    }

    public void setInTime(String inTime) {
        this.inTime = inTime;
    }

    public String getOutTime() {
        return outTime;
    }

    public void setOutTime(String outTime) {
        this.outTime = outTime;
    }

    public String getHolidayStatus() {
        return holidayStatus;
    }

    public void setHolidayStatus(String holidayStatus) {
        this.holidayStatus = holidayStatus;
    }
}

