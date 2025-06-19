package com.application.facedec.controller.Auth;

import com.application.facedec.dto.LoginRequest;
import com.application.facedec.dto.LoginResponse;
import com.application.facedec.dto.RefreshTokenRequest;
import com.application.facedec.service.AuthService;
import jakarta.security.auth.message.AuthException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.naming.AuthenticationException;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * Handles user login requests.
     * Authenticates the user and returns an access token and a refresh token.
     *
     * @param loginRequest The request body containing user credentials (email and password).
     * @return ResponseEntity containing LoginResponse with access and refresh tokens,
     * and HTTP status OK (200) on success.
     * Appropriate error status/body will be returned if authentication fails
     * (e.g., due to AuthenticationManager or custom exception handling).
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {

        LoginResponse loginResponse = authService.login(loginRequest);

        return new ResponseEntity<>(loginResponse, HttpStatus.OK);
    }

    /**
     * Handles refresh token requests.
     * Allows a client to obtain a new access token (and a new refresh token for rotation)
     * using a valid existing refresh token.
     *
     * @param refreshTokenRequest The request body containing the refresh token string.
     * @return ResponseEntity containing LoginResponse with the new access and refresh tokens,
     * and HTTP status OK (200) on success.
     * Appropriate error status/body will be returned if the refresh token is invalid,
     * expired, or revoked.
     */
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest) throws AuthException {
        // Delegate the refresh token logic to the AuthService.
        // It will validate the refresh token and issue new tokens.
        LoginResponse loginResponse = authService.refreshToken(refreshTokenRequest.getRefreshToken());

        // Return the LoginResponse with HTTP status OK.
        return new ResponseEntity<>(loginResponse, HttpStatus.OK);
    }


}
