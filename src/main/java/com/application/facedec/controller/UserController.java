package com.application.facedec.controller;

import com.application.facedec.dto.LoginRequest;
import com.application.facedec.dto.LoginResponse;
import com.application.facedec.dto.RegistrationRequest;
import com.application.facedec.entity.User;
import com.application.facedec.service.AuthenticationService;
import com.application.facedec.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<User> registerUser(@Valid @RequestBody RegistrationRequest registrationRequest) {
        User newUser = userService.registerUser(registrationRequest);
        return ResponseEntity.ok(newUser);
    }

    @GetMapping("/login")
    public String loginUser(@Valid @RequestBody LoginRequest loginRequest) {
        return authenticationService.loginUser(loginRequest);
    }
}
