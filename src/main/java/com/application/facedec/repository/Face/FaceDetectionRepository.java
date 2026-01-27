package com.application.facedec.repository.Face;

import com.application.facedec.entity.Face.Face;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FaceDetectionRepository extends JpaRepository<Face, Long> {
    Face findByUserId(Long userId);

    boolean existsByUserId(Long userId);

    List<Face> findAllByUserId(Long userId);
}
