package com.application.facedec.service.User;

import com.application.facedec.entity.User.Employee;
import com.application.facedec.repository.User.UserRepository;
import com.application.facedec.service.Attendance.LeaveService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class EmployeeService {

    @Autowired
    private UserRepository employeeRepository;

    @Autowired
    private LeaveService leaveService;

    /**
     * Handles the registration of a new employee, including security setup
     * and leave balance initialization. This method is transactional.
     * @param employee The Employee object to save.
     * @return The saved Employee object.
     */
    @Transactional
    public Employee registerEmployee(Employee employee) {
        // 1. Save the new employee to the database
        // NOTE: This populates the Employee object's ID field.
        Employee savedEmployee = employeeRepository.save(employee);

        // 2. CRUCIAL STEP: Initialize the monthly leave balance immediately after
        // The savedEmployee object (with ID) is required for the LeaveService.
        leaveService.initializeLeaveBalance(savedEmployee);

        return savedEmployee;
    }

    /**
     * Finds an employee by their email. Required by SecurityUtils for authentication.
     * @param email The email of the employee.
     * @return An Optional containing the Employee, or empty if not found.
     */
    public Optional<Employee> getEmployeeByEmail(String email) {
        return employeeRepository.findByEmail(email);
    }

    /**
     * Finds an employee by their ID. Required by LeaveService for admin updates.
     * @param employeeId The ID of the employee.
     * @return An Optional containing the Employee, or empty if not found.
     */
    public Optional<Employee> findById(Long employeeId) { return employeeRepository.findById(employeeId); }
}
