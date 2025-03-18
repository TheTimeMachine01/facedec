package com.application.facedec.config;

import com.application.facedec.entity.Employee;
import com.application.facedec.service.EmployeeService;
import io.jsonwebtoken.Jwt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class SecurityUtils {

    @Autowired
    private EmployeeService employeeService;

    public Employee getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

//        System.out.println(STR."I am here! \n \{authentication.getPrincipal()} \n");


        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("No authenticated User Found");
        }

        if (authentication.getPrincipal() instanceof User userDetails) {
//            Map<String, Object> claims = (Map<String, Object>) jwt.getHeader().get("claims");


            String email = (String) userDetails.getUsername();

            return employeeService.getEmployeeByEmail(email)
                    .orElseThrow(() -> new RuntimeException(STR."Employee not Found with email: \{email}"));
        }

        throw new RuntimeException("Unable to Extract user Details from authentication principal");
    }
}
