package com.application.facedec.dto.Attendance;

import lombok.Data;

@Data
public class InLogRequest {
    private double latitude;
    private double longitude;
    private boolean isFaceMatched;
}
