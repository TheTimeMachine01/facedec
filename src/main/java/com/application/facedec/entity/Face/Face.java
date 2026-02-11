package com.application.facedec.entity.Face;


import com.application.facedec.entity.User.Employee;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class Face {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Employee user;

    private Long emp_id;

    @Lob
    @Column(columnDefinition="LONGBLOB")
    private byte[] faceImageData;

    @Version
    private Long version;

    private String faceImageUrl;
    private String imageSize;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Getters and setters
}