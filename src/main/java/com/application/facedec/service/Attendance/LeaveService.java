package com.application.facedec.service.Attendance;

import com.application.facedec.config.SecurityUtils;
import com.application.facedec.entity.Attendance.LeaveBalance;
import com.application.facedec.entity.Rule.CompanyRule;
import com.application.facedec.entity.User.Employee;
import com.application.facedec.repository.Attendance.LeaveBalanceRepository;
import com.application.facedec.repository.Rule.CompanyRuleRepository;
import com.application.facedec.service.User.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

// Assuming Employee and SecurityUtils are defined elsewhere
// Mock imports for demonstration
// import com.example.model.Employee;
// import com.example.utils.SecurityUtils;
//
// NEW ASSUMPTIONS for Rule Management:
// import com.example.repository.CompanyRuleRepository;
// import com.example.entity.CompanyRule;

@Service
public class LeaveService {

    // Removed the hardcoded INITIAL_LEAVE_COUNT constant.
    @Autowired
    private LeaveBalanceRepository leaveBalanceRepository;

    @Autowired
    private SecurityUtils securityUtils;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private CompanyRuleRepository companyRuleRepository; // NEW: Inject rule repository


    /**
     * Helper method to fetch the current initial leave count from the centralized rules entity.
     * Assumes a standard rule record (e.g., ID 1L) holds the configuration.
     * @return The configured initial leave count.
     * @throws IllegalStateException if the core rule configuration is missing.
     */
    private int getInitialLeaveCountFromRules() {
        // Assuming CompanyRule ID 1L holds the base configuration.
        // ASSUMPTION: CompanyRule entity has a method named getInitialAnnualLeaveCount()
        return companyRuleRepository.findById(1L)
                .map(CompanyRule::getInitialAnnualLeaveCount)
                .orElseThrow(() -> new IllegalStateException("Company rule configuration not found (ID 1L). Cannot initialize leave balance."));
    }

    /**
     * Initializes the leave balance for a new employee account.
     * This method now dynamically fetches the initial value from the CompanyRule entity.
     * @param employee The newly created employee.
     */
    @Transactional
    public void initializeLeaveBalance(Employee employee) {
        LocalDate today = LocalDate.now();
        int currentMonth = today.getMonthValue();
        int currentYear = today.getYear();

        // 1. Check if a balance already exists for the current period (prevents duplicates on re-run)
        Optional<LeaveBalance> existingBalance = leaveBalanceRepository.findByUserAndMonthAndYear(employee, currentMonth, currentYear);

        if (existingBalance.isEmpty()) {
            int initialLeaveCount = getInitialLeaveCountFromRules();

            // 2. FIXED: Use the updated constructor with month and year
            LeaveBalance newBalance = new LeaveBalance(
                    employee,
                    initialLeaveCount,
                    currentMonth, // <-- Required for monthly tracking
                    currentYear   // <-- Required for monthly tracking
            );
            leaveBalanceRepository.save(newBalance);
        }
    }

    /**
     * Retrieves the leave balance for the authenticated user for a specific month and year.
     * @param month The target month (1-12).
     * @param year The target year.
     * @return The LeaveBalance entity if found.
     */
    public Optional<LeaveBalance> getLeaveBalance(int month, int year) {
        Employee currentUser = securityUtils.getAuthenticatedUser();
        // Use the new repository method to find the balance by user, month, and year
        return leaveBalanceRepository.findByUserAndMonthAndYear(currentUser, month, year);
    }

    /**
     * ADMIN-only method to update the remaining leaves for a target employee for a specific month/year.
     * @param employeeId The ID of the employee to update.
     * @param newBalance The new leave count.
     * @param month The month to update.
     * @param year The year to update.
     * @return The updated LeaveBalance.
     * @throws SecurityException if the authenticated user is not an Admin.
     * @throws IllegalArgumentException if the target employee or balance record is not found.
     */
    @Transactional
    public LeaveBalance updateLeaveBalance(Long employeeId, int newBalance, int month, int year) {
        if (!securityUtils.hasAdminRole()) {
            throw new SecurityException("Access denied. Only Admins can update leave balances.");
        }

        // 1. Find the target Employee object using the injected EmployeeService
        Employee targetEmployee = employeeService.findById(employeeId) // Assumes findById returns Optional<Employee>
                .orElseThrow(() -> new IllegalArgumentException(STR."Target employee not found with ID: \{employeeId}"));

        // 2. Find the target record using the fetched Employee object, Month, and Year
        LeaveBalance leaveBalance = leaveBalanceRepository.findByUserAndMonthAndYear(
                        targetEmployee, // FIXED: Use the fetched targetEmployee object
                        month,
                        year
                )
                .orElseThrow(() -> new IllegalArgumentException(STR."Leave balance not found for user \{employeeId} in \{month}/\{year}"));

        // 3. Update the balance
        leaveBalance.setRemainingLeaves(newBalance);

        // 4. Save and return
        return leaveBalanceRepository.save(leaveBalance);
    }
}