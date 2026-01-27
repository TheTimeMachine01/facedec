package com.application.facedec.controller.Attendance;

import com.application.facedec.config.SecurityUtils;
import com.application.facedec.dto.Attendance.BalanceRequest;
import com.application.facedec.dto.Rule.RuleUpdate;
import com.application.facedec.entity.Attendance.LeaveBalance;
import com.application.facedec.entity.User.Employee;
import com.application.facedec.service.Attendance.LeaveService;
import com.application.facedec.service.User.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.Optional;

// Assuming Employee and SecurityUtils are defined elsewhere

@RestController
@RequestMapping("/api/leaves")
public class LeaveController {

    @Autowired
    private LeaveService leaveService;

    @Autowired
    private SecurityUtils securityUtils;

    @Autowired
    private EmployeeService employeeService;


    private ResponseEntity<String> checkAuth() {
        if (securityUtils.getAuthenticatedUser() == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Authentication required.");
        }
        return null;
    }

    /**
     * GET endpoint for any authenticated user to check their own leave balance.
     * Endpoint: /api/leaves/balance
     */
    @GetMapping("/balance")
    public ResponseEntity<?> getBalance(
//            @RequestParam(required = false) Integer month,
//            @RequestParam(required = false) Integer year
            @RequestBody BalanceRequest balanceRequest
            ) {
        ResponseEntity<String> authError = checkAuth();
        if (authError != null) return authError;

        Employee currentUser = securityUtils.getAuthenticatedUser();

        LocalDate today = LocalDate.now();
        int targetMonth = balanceRequest.getMonth() != null ? balanceRequest.getMonth() : today.getMonthValue();
        int targetYear = balanceRequest.getYear() != null ? balanceRequest.getYear() : today.getYear();

        try {
            // 1. Change the return type to Optional<LeaveBalance>
            Optional<LeaveBalance> balanceOpt = leaveService.getLeaveBalance(targetMonth, targetYear);

            if (balanceOpt.isEmpty()) {
                // 2. Handle the case where the balance for the month has not been initialized (returns 0)
                // Returning 0 is usually safer than 404 for a balance check.
                return ResponseEntity.ok(0);
            }

            // 3. Get the value safely from the Optional
            LeaveBalance balance = balanceOpt.get();

            // Return only the count for simplicity in a public endpoint
            return ResponseEntity.ok(balance.getRemainingLeaves());
        } catch (Exception e) {
            System.out.println(STR."Error fetching leave balance for user: \{currentUser.getId()}");
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to retrieve leave balance.");
        }
    }

    /**
     * Admin-only endpoint to update an employee's leave balance for a specific month.
     * PUT /api/leave/{employeeId}/balance?month=10&year=2025
     */
    @PutMapping("/{employeeId}/balance")
    public ResponseEntity<?> updateBalance(
            @PathVariable Long employeeId,
            @RequestBody RuleUpdate updateDTO, // Reusing DTO for simplicity
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {


        ResponseEntity<String> authError = checkAuth();
        if (authError != null) return authError;

        Employee currentUser = securityUtils.getAuthenticatedUser();

        if (!securityUtils.hasAdminRole()) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN) // 403
                    .body("Access denied. Only administrators can update leave balances.");
        }

        // Validate target employee
        Optional<Employee> targetEmployeeOpt = employeeService.findById(employeeId);
        if (targetEmployeeOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Target employee not found.");
        }
        Employee targetEmployee = targetEmployeeOpt.get();

        LocalDate today = LocalDate.now();
        int targetMonth = month != null ? month : today.getMonthValue();
        int targetYear = year != null ? year : today.getYear();

        try {
            LeaveBalance updatedBalance = leaveService.updateLeaveBalance(
                    targetEmployee.getId(),
                    updateDTO.getNewInitialAnnualLeaveCount(), // Assumes RuleUpdateDTO has getNewLeaveCount()
                    targetMonth,
                    targetYear
            );
            return ResponseEntity.ok(updatedBalance);

        } catch (SecurityException e) {
            System.out.println(STR."Unauthorized attempt to update leave balance: \{e.getMessage()}");
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN) // 403 Forbidden
                    .body(e.getMessage());
        } catch (Exception e) {
            System.out.println(STR."Error updating leave balance for employee {}: {}\{employeeId}\{e.getMessage()}");
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to update leave balance due to server error.");
        }
    }
}