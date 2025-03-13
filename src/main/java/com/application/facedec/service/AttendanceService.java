package com.application.facedec.service;


import com.application.facedec.entity.Attendance;
import com.application.facedec.entity.Employee;
import com.application.facedec.repository.AttendanceRepository;
import com.application.facedec.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AttendanceService {
    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Attendance> getAttendanceByUserId(Long userId) {
        return attendanceRepository.findByUserId(userId);
    }

    public Attendance addAttendance(Long userId, Attendance attendance) {
        Employee employee = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        attendance.setUser(employee);
        return attendanceRepository.save(attendance);
    }
}

