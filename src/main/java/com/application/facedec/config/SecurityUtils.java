package com.application.facedec.config;

import com.application.facedec.entity.User.Employee;
import com.application.facedec.service.User.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;


@Component
public class SecurityUtils {

    @Autowired
    private EmployeeService employeeService;

    /**
     * Retrieves the authenticated Employee object from the SecurityContext.
     * @return The authenticated Employee.
     * @throws RuntimeException if no authenticated user is found or email cannot be extracted.
     */
    public Employee getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("No authenticated User Found");
        }

        if (authentication.getPrincipal() instanceof User userDetails) {
            String email = (String) userDetails.getUsername();

            return employeeService.getEmployeeByEmail(email)
                    .orElseThrow(() -> new RuntimeException(STR."Employee not Found with email: \{email}"));
        }

        throw new RuntimeException("Unable to Extract user Details from authentication principal");
    }

    /**
     * Checks if the currently authenticated user has the 'ADMIN' role
     * by checking the Employee's Set<Role> collection.
     * @return true if the user is an admin, false otherwise.
     */
    public boolean hasAdminRole() {
        try {
            Employee employee = getAuthenticatedUser();

            // --- CORRECTED CORE RBAC CHECK ---
            // Check the 'roles' Set on the Employee entity for a Role with the name "ADMIN".
            return employee.getRoles().stream()
                    .anyMatch(role -> "ADMIN".equalsIgnoreCase(role.getName()));

        } catch (RuntimeException e) {
            // If getAuthenticatedUser throws an exception (unauthenticated or user not found),
            // the user is not an Admin.
            return false;
        }
    }
}
