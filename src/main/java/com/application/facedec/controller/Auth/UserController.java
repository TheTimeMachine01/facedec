package com.application.facedec.controller.Auth;

import com.application.facedec.dto.RegistrationRequest;
import com.application.facedec.entity.Employee;
import com.application.facedec.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<Employee> registerUser(@Valid @RequestBody RegistrationRequest registrationRequest) {
        Employee newEmployee = userService.registerUser(registrationRequest);
        return new ResponseEntity<>(newEmployee, HttpStatus.CREATED);
    }
}
