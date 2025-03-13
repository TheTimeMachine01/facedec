package com.application.facedec.service;

import com.application.facedec.dto.RegistrationRequest;
import com.application.facedec.entity.Employee;
import com.application.facedec.entity.Role;
import com.application.facedec.exceptions.EmailAlreadyExistException;
import com.application.facedec.repository.RoleRepository;
import com.application.facedec.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Employee registerUser(RegistrationRequest registrationRequest) {

        if (userRepository.existsByEmail(registrationRequest.getEmail())) {
            throw new EmailAlreadyExistException("Employee with this email already exists.");
        }

        Employee employee = new Employee();
        employee.setName(registrationRequest.getName());
        employee.setEmail(registrationRequest.getEmail());
        employee.setPassword(passwordEncoder.encode(registrationRequest.getPassword()));

        // *** Role Authorization ***
        Role userRole = roleRepository.findByName(Role.RoleType.USER.getName());

        if (userRole == null) {
            throw new RuntimeException("USER role not found. Make sure it's initialized.");
        }

        employee.setRoles(Collections.singleton(userRole));

        return userRepository.save(employee);
    }
}