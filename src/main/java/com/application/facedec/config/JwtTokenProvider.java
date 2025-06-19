package com.application.facedec.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SignatureException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.UUID;


@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration-time}")
    private long jwtExpirationMs;

    // New: Add property for refresh token expiration (for database storage)
    @Value("${jwt.refresh-expiration-time}")
    private long jwtRefreshExpirationMs;

    public String generateToken(Authentication authentication) {

        String username = authentication.getName();
        Date currentDate = new Date();
        Date expireDate = new Date(currentDate.getTime() + jwtExpirationMs);

        String token = Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(expireDate)
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();

        return token;
    }

    // This is used by AuthService when a refresh token is exchanged for a new access token.
    public String generateTokenFromUsername(String username) {
        Date currentDate = new Date();
        Date expireDate = new Date(currentDate.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .subject(username)
                .issuedAt(currentDate)
                .expiration(expireDate)
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    // This token is NOT a JWT. Its lifecycle (expiration, association, revocation)
    // is managed by the database and AuthService.
    public String generateRefreshTokenString() {
        return UUID.randomUUID().toString(); // Generates a strong, unique ID
    }

    private Key key(){
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    // extract username from JWT token
    public String getUsername(String token){

        return Jwts.parser()
                .verifyWith((SecretKey) key())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    // validate JWT token
    public boolean validateToken(String token){

        try {
            Jwts.parser()
                    .verifyWith((SecretKey) key())
                    .build()
                    .parse(token);
            return true;
        } catch (SignatureException ex) {
            throw ex;
        } catch (MalformedJwtException ex) {
            System.err.println(STR."Invalid JWT token: \{ex.getMessage()}");
        } catch (ExpiredJwtException ex) {
            System.err.println(STR."Expired JWT token: \{ex.getMessage()}");
        } catch (UnsupportedJwtException ex) {
            System.err.println(STR."Unsupported JWT token: \{ex.getMessage()}");
        } catch (IllegalArgumentException ex) {
            System.err.println(STR."JWT claims string is empty: \{ex.getMessage()}");
        }
        return false;
    }

}