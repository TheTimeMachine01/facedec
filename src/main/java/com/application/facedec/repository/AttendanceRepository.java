package com.application.facedec.repository;


import com.application.facedec.entity.Attendance;
import com.application.facedec.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    /**
     * Finds an attendance record for a specific employee on a given date.
     * This is crucial for preventing multiple attendance markings for the same day.
     * <p>
     * // @param employeeId The ID of the employee.
     * // @param today The date for which attendance is being checked.
     * // @return An Optional containing the Attendance record if found, or empty otherwise.
     */
//    Optional<Attendance> findByEmployeeIdAndAttendanceDate(Long employeeId, LocalDate attendanceDate);

    Optional<Attendance> findByUserAndDate(Employee employee, LocalDate today);
}
