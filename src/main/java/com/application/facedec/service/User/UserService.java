package com.application.facedec.service.User;

import com.application.facedec.dto.RegistrationRequest;
import com.application.facedec.entity.User.Employee;
import com.application.facedec.entity.User.Role;
import com.application.facedec.exceptions.EmailAlreadyExistException;
import com.application.facedec.repository.User.RoleRepository;
import com.application.facedec.repository.User.UserRepository;
import com.application.facedec.service.Attendance.LeaveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private LeaveService leaveService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Employee registerUser(RegistrationRequest registrationRequest) {

        if (userRepository.existsByEmail(registrationRequest.getEmail())) {
            throw new EmailAlreadyExistException("Employee with this email already exists.");
        }

        Employee employee = new Employee();
        employee.setName(registrationRequest.getName());
        employee.setEmail(registrationRequest.getEmail());

        // Employee ID creation and Assigning
        // Department Assigning
        employee.setPassword(passwordEncoder.encode(registrationRequest.getPassword()));

        // *** Role Authorization ***
//        Role userRole = roleRepository.findByName(Role.RoleType.USER.getName());
//
//        if (userRole == null) {
//            throw new RuntimeException("USER role not found. Make sure it's initialized.");
//        }
//
//        employee.setRoles(Collections.singleton(userRole));

        Role defaultRole = roleRepository.findByName(Role.RoleType.USER.getName())
                .orElseThrow(() -> new RuntimeException("Default 'USER' role not found. System configuration error."));

        Set<Role> roles = new HashSet<>();
        roles.add(defaultRole);
        employee.setRoles(roles);

        // 5. Save the employee first to generate the ID
        Employee savedEmployee = userRepository.save(employee);

        leaveService.initializeLeaveBalance(savedEmployee);

        return userRepository.save(employee);
    }
}