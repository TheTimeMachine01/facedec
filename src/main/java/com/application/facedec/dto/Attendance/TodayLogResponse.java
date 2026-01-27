package com.application.facedec.dto.Attendance;

import lombok.Data;

import java.time.LocalDate;

@Data
public class TodayLogResponse {
    private LocalDate date;
    private String inTime;
    private String outTime;
    private String holidayStatus;
}
