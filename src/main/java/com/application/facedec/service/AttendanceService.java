package com.application.facedec.service;


import com.application.facedec.entity.Attendance;
import com.application.facedec.entity.Employee;
import com.application.facedec.entity.FaceMatchStatus;
import com.application.facedec.entity.HolidayStatus;
import com.application.facedec.repository.AttendanceRepository;
import com.application.facedec.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class AttendanceService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Marks an employee's "in" time attendance for the current day.
     * This method will create a new attendance record if one doesn't exist for today.
     * It includes location data and face matching status.
     *
     * @param employee The Employee entity for whom attendance is to be marked.
     * @param latitude The latitude coordinate of the user's location.
     * @param longitude The longitude coordinate of the user's location.
     * @param isFaceMatched A boolean indicating if the face was successfully matched.
     * @return The created or updated Attendance record if successful, or null if already marked or an error occurred.
     */
    @Transactional
    public Attendance markInTime(Employee employee, Double latitude, Double longitude, Boolean isFaceMatched) {
        LocalDate today = LocalDate.now();

        // Check if "in" time is already marked for today for this employee
        Optional<Attendance> existingAttendance = attendanceRepository.findByUserAndDate(employee, today);

        if (existingAttendance.isPresent() && existingAttendance.get().getInTime() != null) {
            System.out.println(STR."In time already marked for employee \{employee.getId()} on \{today}");
            return null; // In time already marked for today
        }

        Attendance attendance;
        if (existingAttendance.isPresent()) {
            // If a record exists (e.g., for a holiday pre-marked without times), update it
            attendance = existingAttendance.get();
            attendance.setDate(today);
            attendance.setInTime(LocalTime.now());
            attendance.setLatitude(latitude);
            attendance.setLongitude(longitude);
            attendance.setFaceData(isFaceMatched ? FaceMatchStatus.MATCHED : FaceMatchStatus.NOT_MATCHED);
            // Ensure holiday status is NA if it was set to something else and now they are clocking in
            if (attendance.getHolidayStatus() != null && attendance.getHolidayStatus() != HolidayStatus.NA) {
                // If they are clocking in, it's no longer a 'leave' or 'absent' day for this record
                attendance.setHolidayStatus(HolidayStatus.NA);
            }
        } else {
            // Create a new attendance record
            attendance = new Attendance(
                    employee,
                    today,
                    LocalTime.now(),
                    today.getDayOfWeek(),
                    isFaceMatched ? FaceMatchStatus.MATCHED : FaceMatchStatus.NOT_MATCHED,
                    latitude,
                    longitude
            );
            // Default holidayStatus is set to NA by the constructor/columnDefinition
        }

        try {
            return attendanceRepository.save(attendance);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Marks an employee's "out" time attendance for the current day.
     * This method assumes an "in" record already exists for the day.
     *
     * @param employee The Employee entity for whom attendance is to be marked.
     * @param latitude The latitude coordinate of the user's location at logout.
     * @param longitude The longitude coordinate of the user's location at logout.
     * @param isFaceMatched A boolean indicating if the face was successfully matched during logout.
     * @return The updated Attendance record if successful, or null if no "in" record found or an error occurred.
     */
    @Transactional
    public Attendance markOutTime(Employee employee, Double latitude, Double longitude, Boolean isFaceMatched) {
        LocalDate today = LocalDate.now();
        Optional<Attendance> existingAttendance = attendanceRepository.findByUserAndDate(employee, today);

        if (existingAttendance.isEmpty()) {
            System.out.println(STR."No 'in' record found for employee \{employee.getId()} on \{today} to mark 'out' time.");
            return null; // Cannot mark out time if no in time was recorded
        }

        Attendance attendance = existingAttendance.get();

        if (attendance.getOutTime() != null) {
            System.out.println(STR."Out time already marked for employee \{employee.getId()} on \{today}");
            return null; // Out time already marked for today
        }

        attendance.setOutTime(LocalTime.now());
        // You might want to store logout latitude/longitude separately or update the existing ones
        // For simplicity, let's update the existing ones for now.
        // attendance.setLatitude(latitude);
        // attendance.setLongitude(longitude);
        // If you want a separate face match status for out time, add another field to Attendance entity
        // For now, let's assume faceData is for in-time, or you could update it
        // attendance.setFaceData(isFaceMatched ? FaceMatchStatus.MATCHED : FaceMatchStatus.NOT_MATCHED);

        try {
            return attendanceRepository.save(attendance);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Marks a specific date as a holiday, leave, or absent day for an employee.
     * If an attendance record already exists for the date, its holiday status is updated.
     * Otherwise, a new minimal attendance record is created.
     *
     * @param employee The Employee entity for whom the holiday status is being set.
     * @param date The date to be marked.
     * @param status The HolidayStatus (CH, L, A, NA) to apply.
     * @return The created or updated Attendance record if successful, or null if an error occurred.
     */
    @Transactional
    public Attendance markHoliday(Employee employee, LocalDate date, HolidayStatus status) {
        Optional<Attendance> existingAttendance = attendanceRepository.findByUserAndDate(employee, date);

        Attendance attendance;
        if (existingAttendance.isPresent()) {
            attendance = existingAttendance.get();
            // If someone was previously clocked in/out but now it's a holiday, clear times
            if (status != HolidayStatus.NA && (attendance.getInTime() != null || attendance.getOutTime() != null)) {
                attendance.setInTime(null);
                attendance.setOutTime(null);
                attendance.setFaceData(null); // Clear face data as well
                attendance.setLatitude(null); // Clear location data
                attendance.setLongitude(null);
            }
            attendance.setHolidayStatus(status);
        } else {
            // Create a new record just for the holiday status
            attendance = new Attendance(employee, date, date.getDayOfWeek(), status);
        }

        try {
            return attendanceRepository.save(attendance);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Checks if an employee has already marked "in" time attendance for the current day.
     *
     * @param employee The Employee entity to check.
     * @return true if "in" time is marked for today, false otherwise.
     */
    public boolean hasMarkedInTimeToday(Employee employee) {
        LocalDate today = LocalDate.now();
        Optional<Attendance> attendance = attendanceRepository.findByUserAndDate(employee, today);
        return attendance.isPresent() && attendance.get().getInTime() != null;
    }

    /**
     * Checks if an employee has already marked "out" time attendance for the current day.
     *
     * @param employee The Employee entity to check.
     * @return true if "out" time is marked for today, false otherwise.
     */
    public boolean hasMarkedOutTimeToday(Employee employee) {
        LocalDate today = LocalDate.now();
        Optional<Attendance> attendance = attendanceRepository.findByUserAndDate(employee, today);
        return attendance.isPresent() && attendance.get().getOutTime() != null;
    }


}

