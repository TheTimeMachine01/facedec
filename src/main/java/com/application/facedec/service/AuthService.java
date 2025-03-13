package com.application.facedec.service;

import com.application.facedec.dto.LoginRequest;

public interface AuthService {

    String login(LoginRequest loginRequest);
}