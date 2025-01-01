package com.application.facedec.service;


import com.application.facedec.entity.Attendance;
import com.application.facedec.entity.User;
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
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        attendance.setUser(user);
        return attendanceRepository.save(attendance);
    }
}

