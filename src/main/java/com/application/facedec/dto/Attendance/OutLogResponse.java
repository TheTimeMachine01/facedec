package com.application.facedec.dto.Attendance;

import lombok.Data;

@Data
public class OutLogResponse {
    private String message;
    private Long employeeId;
    private String attendanceId;
    private String outTime;
    private boolean status;
}
