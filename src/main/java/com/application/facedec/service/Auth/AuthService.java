package com.application.facedec.service.Auth;

import com.application.facedec.dto.LoginRequest;
import com.application.facedec.dto.LoginResponse;
import jakarta.security.auth.message.AuthException;

public interface AuthService {

    LoginResponse login(LoginRequest loginRequest);

    LoginResponse refreshToken(String refreshToken) throws AuthException;
}