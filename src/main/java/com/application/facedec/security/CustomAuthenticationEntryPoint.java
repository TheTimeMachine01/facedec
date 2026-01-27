package com.application.facedec.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        // Get the exception attribute set by the JWT filter
        Object exception = request.getAttribute("exception");

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("timestamp", new java.util.Date().toString());
        errorDetails.put("status", HttpStatus.UNAUTHORIZED.value());
        errorDetails.put("path", request.getServletPath());

        // Check if the specific exception is an ExpiredJwtException
        if (exception instanceof ExpiredJwtException) {
            errorDetails.put("error", "Unauthorized: Token Expired");
            errorDetails.put("message", "Your session has expired. Please refresh your token.");
        } else {
            errorDetails.put("error", "Unauthorized");
            errorDetails.put("message", "Full authentication is required to access this resource");
        }

        objectMapper.writeValue(response.getWriter(), errorDetails);
    }
}
