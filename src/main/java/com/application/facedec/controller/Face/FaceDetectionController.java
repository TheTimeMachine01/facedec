package com.application.facedec.controller.Face;

import com.application.facedec.service.FaceDetectionService;
import org.opencv.core.Rect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/face")
public class FaceDetectionController {

    @Autowired
    private FaceDetectionService faceDetectionService;

    @PostMapping("/detect")
    public ResponseEntity<?> detectFaces(@RequestParam("file") MultipartFile file) {
        try {
            List<Rect> faces = faceDetectionService.detectFaces(file);
            return ResponseEntity.ok(faces);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing image.");
        }
    }

    @PostMapping("/match")
    public ResponseEntity<?> matchFace(@RequestParam("detectedFace") MultipartFile detectedFace, RequestParam("userId") Long userId) {
        try {
            String tempDir = System.getProperty("java.io.tmpdir");
            String tempFilePath = tempDir + java.io.File.separator + detectedFace.getOriginalFilename();
            java.nio.file.Files.write(java.nio.file.Paths.get(tempFilePath), detectedFace.getBytes());
            boolean matched = faceDetectionService.matchFace(tempFilePath, userId);
            java.nio.file.Files.deleteIfExists(java.nio.file.Paths.get(tempFilePath));
            return ResponseEntity.ok(matched);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing image.");
        }
    }
}


