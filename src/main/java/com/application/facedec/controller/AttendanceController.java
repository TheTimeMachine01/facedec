package com.application.facedec.controller;

import com.application.facedec.config.SecurityUtils;
import com.application.facedec.dto.Attendance.InLogRequest;
import com.application.facedec.entity.Attendance;
import com.application.facedec.entity.Employee;
import com.application.facedec.entity.HolidayStatus;
import com.application.facedec.exceptions.GlobalExceptionHandler;
import com.application.facedec.service.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    @Autowired
    private SecurityUtils securityUtils;

    @Autowired
    private GlobalExceptionHandler geh;

    @Autowired
    private AttendanceService attendanceService;


    /**
     * Endpoint for marking an employee's daily "in" time attendance.
     * This method requires latitude, longitude, and a flag indicating if face matching was successful.
     *
     * @param inLogRequest The latitude coordinate of the user's location.
     * @return A ResponseEntity indicating success or failure of the "in" log.
     * - HttpStatus.CREATED if attendance is logged successfully (first time).
     * - HttpStatus.CONFLICT if "in" time is already marked for today.
     * - HttpStatus.BAD_REQUEST if essential parameters are missing or invalid.
     * - HttpStatus.UNAUTHORIZED if no authenticated user is found.
     * - HttpStatus.INTERNAL_SERVER_ERROR for any other processing errors.
     */
    @PostMapping("/inlog")
    public ResponseEntity<?> inLogAttendance(@RequestBody InLogRequest inLogRequest) {
        Employee currentUser = securityUtils.getAuthenticatedUser();
        if (currentUser == null) {
            return geh.handleError("User not authenticated.", HttpStatus.UNAUTHORIZED);
        }

        double latitude = inLogRequest.getLatitude();
        double longitude = inLogRequest.getLongitude();
        boolean isFaceMatched = inLogRequest.getIsFaceMatched();

        try {
            Attendance markedAttendance = attendanceService.markInTime(currentUser, latitude, longitude, isFaceMatched);

            if (markedAttendance != null) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "In-time attendance logged successfully.");
                response.put("employeeId", String.valueOf(currentUser.getId()));
                response.put("attendanceId", String.valueOf(markedAttendance.getId()));
                response.put("inTime", markedAttendance.getInTime().toString());
                response.put("faceStatus", markedAttendance.getFaceData().name());
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            } else {
                return geh.handleError("Failed to log in-time. It might already be marked for today.", HttpStatus.CONFLICT);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return geh.handleError("Error logging in-time attendance: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    /**
     * Endpoint for marking an employee's daily "out" time attendance.
     * This method requires latitude, longitude (for logout location), and a flag indicating if face matching was successful during logout.
     * It updates an existing "in" record for the current day.
     * @param latitude The latitude coordinate of the user's location at logout.
     * @param longitude The longitude coordinate of the user's location at logout.
     * @param isFaceMatched A boolean indicating if the user's face was successfully matched during logout.
     * @return A ResponseEntity indicating success or failure of the "out" log.
     * - HttpStatus.OK if attendance is logged successfully.
     * - HttpStatus.NOT_FOUND if no "in" record found for today.
     * - HttpStatus.CONFLICT if "out" time is already marked for today.
     * - HttpStatus.BAD_REQUEST if essential parameters are missing or invalid.
     * - HttpStatus.UNAUTHORIZED if no authenticated user is found.
     * - HttpStatus.INTERNAL_SERVER_ERROR for any other processing errors.
     */

    @PostMapping("/outlog")
    public ResponseEntity<?> outLogAttendance(@RequestParam("latitude") Double latitude,
                                              @RequestParam("longitude") Double longitude,
                                              @RequestParam("isFaceMatched") Boolean isFaceMatched) {
        Employee currentUser = securityUtils.getAuthenticatedUser();
        if (currentUser == null) {
            return geh.handleError("User not authenticated.", HttpStatus.UNAUTHORIZED);
        }

        if (latitude == null || longitude == null || isFaceMatched == null) {
            return geh.handleError("Latitude, Longitude, and isFaceMatched are required.", HttpStatus.BAD_REQUEST);
        }

        try {
            Attendance markedAttendance = attendanceService.markOutTime(currentUser, latitude, longitude, isFaceMatched);

            if (markedAttendance != null) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Out-time attendance logged successfully.");
                response.put("employeeId", String.valueOf(currentUser.getId()));
                response.put("attendanceId", String.valueOf(markedAttendance.getId()));
                response.put("outTime", markedAttendance.getOutTime().toString());
                return ResponseEntity.ok(response);
            } else {
                // Determine if it's because no in-log or out-log already exists
                boolean hasInTime = attendanceService.hasMarkedInTimeToday(currentUser);
                boolean hasOutTime = attendanceService.hasMarkedOutTimeToday(currentUser);

                if (!hasInTime) {
                    return geh.handleError("No 'in' time record found for today. Cannot mark 'out' time.", HttpStatus.NOT_FOUND);
                } else if (hasOutTime) {
                    return geh.handleError("Out-time already marked for today.", HttpStatus.CONFLICT);
                } else {
                    return geh.handleError("Failed to log out-time attendance.", HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return geh.handleError(STR."Error logging out-time attendance: \{e.getMessage()}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Endpoint to mark a specific date with a holiday status (e.g., Leave, Absent).
     * This endpoint allows an authenticated user to declare their holiday status for a given date.
     *
     * @param dateString The date in "YYYY-MM-DD" format.
     * @param statusString The desired HolidayStatus (e.g., "L", "A", "CH", "NA").
     * @return A ResponseEntity indicating success or failure.
     * - HttpStatus.OK if holiday status is marked successfully.
     * - HttpStatus.BAD_REQUEST if date or status is invalid.
     * - HttpStatus.UNAUTHORIZED if no authenticated user is found.
     * - HttpStatus.INTERNAL_SERVER_ERROR for any other processing errors.
     */
    @PostMapping("/markHoliday")
    public ResponseEntity<?> markHolidayStatus(@RequestParam("date") String dateString,
                                               @RequestParam("status") String statusString) {
        Employee currentUser = securityUtils.getAuthenticatedUser();
        if (currentUser == null) {
            return geh.handleError("User not authenticated.", HttpStatus.UNAUTHORIZED);
        }

        LocalDate date;
        HolidayStatus status;
        try {
            date = LocalDate.parse(dateString);
            status = HolidayStatus.valueOf(statusString.toUpperCase());
        } catch (IllegalArgumentException e) {
            return geh.handleError("Invalid date format (use YYYY-MM-DD) or invalid holiday status.", HttpStatus.BAD_REQUEST);
        }

        try {
            Attendance updatedAttendance = attendanceService.markHoliday(currentUser, date, status);

            if (updatedAttendance != null) {
                Map<String, String> response = new HashMap<>();
                response.put("message", STR."Holiday status '\{status.name()}' marked successfully for \{dateString}.");
                response.put("employeeId", String.valueOf(currentUser.getId()));
                response.put("date", dateString);
                response.put("holidayStatus", status.name());
                return ResponseEntity.ok(response);
            } else {
                return geh.handleError("Failed to mark holiday status.", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return geh.handleError(STR."Error marking holiday status: \{e.getMessage()}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Endpoint to check if "in" time attendance has already been marked for the current user today.
     * Used by the mobile app to conditionally enable/disable the "Clock In" button.
     *
     * @return A ResponseEntity containing a boolean flag indicating if "in" time is already marked.
     * - true if "in" time is marked, false otherwise.
     * - Returns HttpStatus.OK.
     */
    @GetMapping("/hasInTimeToday")
    public ResponseEntity<Map<String, Boolean>> hasInTimeToday() {
        Employee currentUser = securityUtils.getAuthenticatedUser();
        if (currentUser == null) {
            Map<String, Boolean> response = new HashMap<>();
            response.put("hasInTime", false);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        boolean hasInTime = attendanceService.hasMarkedInTimeToday(currentUser);

        Map<String, Boolean> response = new HashMap<>();
        response.put("hasInTime", hasInTime);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint to check if "out" time attendance has already been marked for the current user today.
     * Used by the mobile app to conditionally enable/disable the "Clock Out" button.
     *
     * @return A ResponseEntity containing a boolean flag indicating if "out" time is already marked.
     * - true if "out" time is marked, false otherwise.
     * - Returns HttpStatus.OK.
     */
    @GetMapping("/hasOutTimeToday")
    public ResponseEntity<Map<String, Boolean>> hasOutTimeToday() {
        Employee currentUser = securityUtils.getAuthenticatedUser();
        if (currentUser == null) {
            Map<String, Boolean> response = new HashMap<>();
            response.put("hasOutTime", false);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        boolean hasOutTime = attendanceService.hasMarkedOutTimeToday(currentUser);

        Map<String, Boolean> response = new HashMap<>();
        response.put("hasOutTime", hasOutTime);
        return ResponseEntity.ok(response);
    }


}
