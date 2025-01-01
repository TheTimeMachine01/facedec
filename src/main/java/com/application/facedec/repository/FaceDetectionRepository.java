package com.application.facedec.repository;

import com.application.facedec.entity.FaceDetection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FaceDetectionRepository extends JpaRepository<FaceDetection, Long> {
    List<FaceDetection> findByUserId(Long userId);
}
