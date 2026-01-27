package com.application.facedec.repository.Attendance;

import com.application.facedec.entity.Attendance.LeaveBalance;
import com.application.facedec.entity.User.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, Long> {

    /**
     * Finds the leave balance for a specific employee, month, and year.
     * This is crucial for the new monthly tracking system.
     */
    Optional<LeaveBalance> findByUserAndMonthAndYear(Employee user, int month, int year);
}