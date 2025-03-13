package com.application.facedec.controller.Auth;

import com.application.facedec.dto.LoginRequest;
import com.application.facedec.dto.LoginResponse;
import com.application.facedec.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {

        String token = authService.login(loginRequest);

        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setAccessToken(token);

        return new ResponseEntity<>(loginResponse, HttpStatus.OK);
    }
}
