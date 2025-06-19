package com.application.facedec.dto.Attendance;

import lombok.Data;

@Data
public class InLogRequest {
    private Double latitude;
    private Double longitude;
    private Boolean isFaceMatched;
}
