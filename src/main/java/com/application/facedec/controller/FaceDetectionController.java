package com.application.facedec.controller;

import com.application.facedec.entity.FaceDetection;
import com.application.facedec.repository.FaceDetectionRepository;
import com.application.facedec.service.FaceDetectionService;
import org.opencv.core.Rect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

//@RestController
//@RequestMapping("/api/detection")
//public class FaceDetectionController {
//    @Autowired
//    private FaceDetectionRepository faceDetectionRepository;
//
//    @GetMapping("/{userId}")
//    public ResponseEntity<List<FaceDetection>> getFaceData(@PathVariable Long userId) {
//        return ResponseEntity.ok(faceDetectionRepository.findByUserId(userId));
//    }
//}


@RestController
@RequestMapping("/api/facedetection")
public class FaceDetectionController {

    @Autowired
    private FaceDetectionService faceDetectionService;

    @PostMapping("/detect")
    public ResponseEntity<List<Rect>> detectFaces(@RequestParam("file") MultipartFile file) {
        try {
            List<Rect> faces = faceDetectionService.detectFaces(file);
            return ResponseEntity.ok(faces);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }
}


