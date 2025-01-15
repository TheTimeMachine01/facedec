package com.application.facedec.service;

import com.application.facedec.dto.LoginRequest;
import com.application.facedec.dto.LoginResponse;
import com.application.facedec.security.JwtTokenProvider;
import com.application.facedec.repository.UserRepository;
import com.application.facedec.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;

    public AuthenticationService(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserRepository userRepository;

    public String loginUser(LoginRequest loginRequest) {

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            return  "Auth Sucessful";
        } catch (UsernameNotFoundException e) {
            return "Error in authentication: User not found";
        } catch (Exception e) {
            // Other errors, return generic error message
            return "Error in authentication";
        }
//        // Authenticate the user
//        Authentication authentication = authenticationManager.authenticate(
//                new UsernamePasswordAuthenticationToken(
//                        loginRequest.getEmail(),
//                        loginRequest.getPassword()
//                )
//        );
//
//        System.out.println("I am here;");
//
//        // Fetch the authenticated user from the repository
//        User user = userRepository.findByEmail(loginRequest.getEmail())
//                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
//
//        // Generate JWT Token
//        String token = jwtTokenProvider.generateToken(authentication);
//
//        // Return login response with token
//        return new LoginResponse(user.getName(), user.getEmail(), token);
    }
}