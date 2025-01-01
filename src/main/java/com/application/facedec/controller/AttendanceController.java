package com.application.facedec.controller;

import com.application.facedec.entity.Attendance;
import com.application.facedec.service.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {
    @Autowired
    private AttendanceService attendanceService;

    @GetMapping("/{userId}")
    public ResponseEntity<List<Attendance>> getAttendance(@PathVariable Long userId) {
        return ResponseEntity.ok(attendanceService.getAttendanceByUserId(userId));
    }

    @PostMapping("/{userId}")
    public ResponseEntity<Attendance> addAttendance(@PathVariable Long userId, @RequestBody Attendance attendance) {
        return ResponseEntity.ok(attendanceService.addAttendance(userId, attendance));
    }
}
