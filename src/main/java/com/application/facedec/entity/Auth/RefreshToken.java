package com.application.facedec.entity.Auth;

import jakarta.persistence.*; // Use javax.persistence.* if you're on an older Spring Boot version (e.g., < 3.x)
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant; // For modern date/time handling

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token; // The actual refresh token string

    @Column(nullable = false)
    private Instant expiryDate; // When the refresh token expires

    // You might link this to your User entity, or just store the username/email
    @Column(nullable = false)
    private String userEmail; // Or userId, depending on your User entity

    // Optional: Add a revoked flag for explicit revocation without deletion
    private boolean revoked = false;

    public RefreshToken(String refreshTokenString, Instant expiryDate, @NotBlank(message = "Email is required") @Email(message = "Invalid email format") String email) {
        this.token = refreshTokenString;
        this.expiryDate = expiryDate;
        this.userEmail = email;
    }
}
