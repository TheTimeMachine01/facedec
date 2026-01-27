package com.application.facedec.service.Auth;

import com.application.facedec.config.JwtTokenProvider;
import com.application.facedec.dto.LoginRequest;
import com.application.facedec.dto.LoginResponse;
import com.application.facedec.entity.Auth.RefreshToken;
import com.application.facedec.repository.Auth.RefreshTokenRepository;
import jakarta.security.auth.message.AuthException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-expiration-time}") // <-- Here jwtRefreshExpirationMs is injected
    private long jwtRefreshExpirationMs;

    @Transactional
    public LoginResponse login(LoginRequest loginRequest) {
        // 01 - AuthenticationManager is used to authenticate the user
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                loginRequest.getEmail(),
                loginRequest.getPassword()
        ));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = jwtTokenProvider.generateToken(authentication);

        String refreshTokenString = jwtTokenProvider.generateRefreshTokenString();

        Instant expiryDate = Instant.now().plusMillis(jwtRefreshExpirationMs);

        RefreshToken refreshToken = new RefreshToken(refreshTokenString, expiryDate, loginRequest.getEmail());
        refreshTokenRepository.save(refreshToken);

        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setAccessToken(accessToken);
        loginResponse.setRefreshToken(refreshTokenString);


        return loginResponse;
    }

    @Transactional
    public LoginResponse refreshToken(String refreshToken) throws AuthException {

        RefreshToken existingRefreshToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new AuthException("Refresh token not found or invalid."));

        if (existingRefreshToken.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(existingRefreshToken);
            throw new AuthException("Refresh token has expired. Please log in again.");
        }

        if (existingRefreshToken.isRevoked()) {
            throw new AuthException("Refresh token has been revoked. Please log in again.");
        }

        // Invalidate the old token (delete it or mark as revoked)
        refreshTokenRepository.delete(existingRefreshToken);

        String newAccessToken = jwtTokenProvider.generateTokenFromUsername(existingRefreshToken.getUserEmail());

        // Generate a new Refresh Token for rotation and save it
        String newRefreshTokenString = jwtTokenProvider.generateRefreshTokenString();
        // Here it is used again: to calculate the expiry date for the NEW RefreshToken entity
        Instant newExpiryDate = Instant.now().plusMillis(jwtRefreshExpirationMs); // <--- USED HERE
//        RefreshToken newRefreshToken = new RefreshToken(newRefreshTokenString, newExpiryDate, existingRefreshToken.getUserEmail());

        RefreshToken newRefreshToken = new RefreshToken(newRefreshTokenString, newExpiryDate, existingRefreshToken.getUserEmail());
        refreshTokenRepository.save(newRefreshToken);


        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setAccessToken(newAccessToken);
        loginResponse.setRefreshToken(refreshToken);

        return loginResponse;
    }
}
